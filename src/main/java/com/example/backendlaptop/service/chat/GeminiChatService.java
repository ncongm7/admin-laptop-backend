package com.example.backendlaptop.service.chat;

import com.example.backendlaptop.dto.chat.ChatbotResponse;
import com.example.backendlaptop.dto.chat.GroqChatRequest;
import com.example.backendlaptop.dto.chat.GroqChatResponse;
import com.example.backendlaptop.entity.ChiTietSanPham;
import com.example.backendlaptop.entity.HoaDon;
import com.example.backendlaptop.entity.Chat;
import com.example.backendlaptop.entity.SanPham;
import com.example.backendlaptop.repository.ChiTietSanPhamRepository;
import com.example.backendlaptop.repository.SanPhamRepository;
import com.example.backendlaptop.repository.banhang.HoaDonRepository;
import com.example.backendlaptop.repository.ChatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class GeminiChatService {

    private final SanPhamRepository sanPhamRepo;
    private final ChiTietSanPhamRepository chiTietSanPhamRepo;
    private final HoaDonRepository hoaDonRepo;
    private final ChatRepository chatRepo;
    private final RestTemplate restTemplate;

    @Value("${groq.api.key:}")
    private String groqApiKey;

    @Value("${groq.api.model:llama-3.1-8b-instant}")
    private String groqModel;

    @Value("${groq.api.url:https://api.groq.com/openai/v1/chat/completions}")
    private String groqApiUrl;

    @Value("${groq.api.enabled:true}")
    private boolean groqEnabled;

    // Cache product context (refresh every 10 minutes)
    private String cachedProductContext;
    private long contextCacheTime = 0;
    private static final long CACHE_DURATION_MS = 10 * 60 * 1000;

    /**
     * Main method: Tư vấn sản phẩm với Gemini API (Updated with Context & History)
     */
    public ChatbotResponse consultWithGemini(String userMessage, UUID khachHangId, UUID conversationId,
            Map<String, Object> consultationData) {
        if (!groqEnabled || groqApiKey == null || groqApiKey.isEmpty()) {
            log.warn("Groq API is disabled or API key not configured.");
            return null; // Will trigger fallback
        }

        try {
            log.info("🤖 [Groq] Processing request for customer: {}", khachHangId);

            // 1. Build product context (Catalog)
            String productContext = buildProductContext();

            // 2. Data Context (Enrichment from DB based on user message)
            String dataContext = detectAndEnrichContext(userMessage, khachHangId);

            // 3. Conversation History
            String historyContext = getConversationHistory(conversationId);

            // 4. Build prompt
            String prompt = buildDetailedPrompt(userMessage, consultationData, productContext, dataContext, historyContext);

            // 5. Call Groq API
            GroqChatResponse groqResponse = callGroqAPI(prompt);

            if (groqResponse == null || groqResponse.getText() == null) {
                log.warn("Groq API returned null or empty response. Falling back.");
                return null;
            }

            String responseText = groqResponse.getText();
            log.info("✅ [Groq] Received response (length: {})", responseText.length());

            // 6. Extract product recommendations
            List<SanPham> recommendedProducts = extractRecommendedProducts(responseText);

            // 7. Build ChatbotResponse
            return buildResponseWithProducts(responseText, recommendedProducts);

        } catch (Exception e) {
            log.error("❌ [Groq] Error processing Groq consultation: {}", e.getMessage(), e);
            return null; // Fallback to ChatbotService
        }
    }

    /**
     * Detect intent and enrich context from Database (Orders, Products)
     */
    private String detectAndEnrichContext(String userMessage, UUID khachHangId) {
        if (userMessage == null || userMessage.isEmpty()) return "";
        StringBuilder context = new StringBuilder();
        
        // --- 1. Detect Order Inquiry ---
        // Look for potential Order IDs or Keywords "đơn hàng", "tra cứu"
        boolean isOrderInquiry = userMessage.toLowerCase().contains("đơn hàng") || 
                                 userMessage.toLowerCase().contains("tra cứu") ||
                                 userMessage.toLowerCase().contains("vận chuyển");

        // Try to find specific Order Code (e.g., HD12345 or just numbers)
        Pattern codePattern = Pattern.compile("(?i)(HD\\d+|\\b[A-Z0-9-]{6,}\\b)");
        Matcher matcher = codePattern.matcher(userMessage);
        
        if (matcher.find()) {
            String code = matcher.group(1);
            log.info("🔍 [Groq] Detected potential code: {}", code);
            
            // Try explicit lookup in HoaDon
            Optional<HoaDon> hoaDon = hoaDonRepo.findByMa(code);
            if (hoaDon.isPresent()) {
                appendOrderInfo(context, hoaDon.get());
            } else {
                // Try searching in ChiTietSanPham (Warranty/Product check)
                try {
                    Page<ChiTietSanPham> products = chiTietSanPhamRepo.search(code, null, null, null, null, null, null, null, null, PageRequest.of(0, 1));
                    if (products.hasContent()) {
                        ChiTietSanPham ctsp = products.getContent().get(0);
                        context.append("📌 [Hệ thống] Thông tin sản phẩm mã '").append(code).append("':\n");
                        context.append("- Tên: ").append(ctsp.getSanPham().getTenSanPham()).append("\n");
                        context.append("- Giá: ").append(formatPrice(ctsp.getGiaBan().longValue())).append(" VNĐ\n");
                        context.append("- Tình trạng: ").append(ctsp.getTrangThai() == 1 ? "Còn hàng" : "Hết hàng").append("\n");
                        context.append("- Cấu hình: ").append(ctsp.getCpu().getTenCpu()).append(", ").append(ctsp.getRam().getTenRam()).append("\n");
                    }
                } catch (Exception ignored) {}
            }
        } else if (isOrderInquiry && khachHangId != null) {
            // User asked about "my order" but didn't provide code -> Get latest order
            try {
                Page<HoaDon> recentOrders = hoaDonRepo.findByIdKhachHang_IdOrderByNgayTaoDesc(khachHangId, PageRequest.of(0, 1));
                if (recentOrders.hasContent()) {
                    HoaDon latestOrder = recentOrders.getContent().get(0);
                    appendOrderInfo(context, latestOrder);
                } else {
                    context.append("📌 [Hệ thống] Khách hàng chưa có đơn hàng nào trong hệ thống.\n");
                }
            } catch (Exception e) {
                log.warn("Error fetching recent orders: {}", e.getMessage());
            }
        }

        return context.toString();
    }

    private void appendOrderInfo(StringBuilder context, HoaDon hd) {
        context.append("📌 [Hệ thống] Thông tin đơn hàng ").append(hd.getMa()).append(":\n");
        context.append("- Ngày đặt: ").append(hd.getNgayTao()).append("\n");
        context.append("- Tổng tiền: ").append(formatPrice(hd.getTongTien().longValue())).append(" VNĐ\n");
        context.append("- Trạng thái: ").append(hd.getTrangThai() != null ? hd.getTrangThai().toString() : "Chưa xác định").append("\n");
        context.append("- Khách hàng: ").append(hd.getTenKhachHang()).append("\n");
        context.append("- Địa chỉ: ").append(hd.getDiaChi()).append("\n");
    }

    /**
     * Get recent conversation history
     */
    private String getConversationHistory(UUID conversationId) {
        if (conversationId == null) return "";
        try {
            // Get last 6 messages
            Page<Chat> page = chatRepo.findLastMessageByConversationId(conversationId, PageRequest.of(0, 6));
            List<Chat> history = new ArrayList<>(page.getContent());
            Collections.reverse(history); // Chronological order

            StringBuilder sb = new StringBuilder();
            sb.append("Lịch sử chat gần đây:\n");
            for (Chat chat : history) {
                String role = chat.getIsFromCustomer() ? "Khách" : "Bot";
                sb.append(role).append(": ").append(chat.getNoiDung()).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            log.warn("Failed to fetch chat history: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Build product context string from database
     */
    public String buildProductContext() {
        long now = System.currentTimeMillis();
        if (cachedProductContext != null && (now - contextCacheTime) < CACHE_DURATION_MS) {
            return cachedProductContext;
        }

        try {
            List<SanPham> products = sanPhamRepo.findByTrangThai(1);
            if (products.size() > 10) {
                products = products.subList(0, 10);
            }

            StringBuilder context = new StringBuilder();
            context.append("Danh sách Top Laptop đang bán:\n");
            
            for (SanPham product : products) {
                List<ChiTietSanPham> variants = chiTietSanPhamRepo.findBySanPham_Id(product.getId());
                if (variants.isEmpty()) continue;
                
                ChiTietSanPham v = variants.get(0);
                context.append(String.format("- %s (ID: %s) - %s - CPU %s, RAM %s - Giá: %s VNĐ\n", 
                        product.getTenSanPham(), 
                        product.getId(),
                        v.getMauSac() != null ? v.getMauSac().getTenMau() : "",
                        v.getCpu() != null ? v.getCpu().getTenCpu() : "",
                        v.getRam() != null ? v.getRam().getTenRam() : "",
                        v.getGiaBan() != null ? formatPrice(v.getGiaBan().longValue()) : "Liên hệ"));
            }
            
            cachedProductContext = context.toString();
            contextCacheTime = now;
            return cachedProductContext;
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Build consultation prompt for Gemini
     */
    private String buildDetailedPrompt(String userMessage, Map<String, Object> consultationData,
            String productContext, String dataContext, String historyContext) {
        StringBuilder prompt = new StringBuilder();

        // --- SYSTEM INSTRUCTION ---
        prompt.append("Vai trò: Trợ lý AI chuyên nghiệp của Dell Laptop Store.\n");
        prompt.append("Nhiệm vụ: Hỗ trợ khách hàng mua máy, tra cứu đơn hàng, bảo hành.\n");
        prompt.append("Nguyên tắc:\n");
        prompt.append("1. Trả lời NGẮN GỌN, đi thẳng vào vấn đề.\n");
        prompt.append("2. Nếu có 'Thông tin thực tế từ hệ thống' (bên dưới), hãy dùng nó để trả lời ngay lập tức. Đừng hỏi lại khách.\n");
        prompt.append("3. Nếu khách đưa mã đơn hàng/sản phẩm, hãy phân tích và báo cáo trạng thái.\n");
        prompt.append("4. Không chào hỏi sáo rỗng (như 'Chào bạn, bạn hỏi hay quá').\n");
        prompt.append("5. Giọng điệu: Thân thiện, chuyên nghiệp, như nhân viên bán hàng gioi.\n\n");

        // --- CONTEXT ---
        if (!dataContext.isEmpty()) {
            prompt.append("### THÔNG TIN THỰC TẾ TỪ HỆ THỐNG (QUAN TRỌNG) ###\n");
            prompt.append(dataContext).append("\n\n");
        }

        if (!productContext.isEmpty()) {
            prompt.append("### DANH MỤC SẢN PHẨM ###\n");
            prompt.append(productContext).append("\n\n");
        }

        if (!historyContext.isEmpty()) {
            prompt.append("### LỊCH SỬ CHAT ###\n");
            prompt.append(historyContext).append("\n\n");
        }

        // --- USER INPUT ---
        prompt.append("### CÂU HỎI HIỆN TẠI ###\n");
        prompt.append("Khách: ").append(userMessage).append("\n");
        
        prompt.append("\n(Hãy trả lời câu hỏi của khách dựa trên các thông tin trên):");

        return prompt.toString();
    }

    private GroqChatResponse callGroqAPI(String prompt) {
        int maxRetries = 2;
        int attempt = 0;
        long backoff = 1000;

        while (attempt < maxRetries) {
            try {
                attempt++;
                String systemPrompt = "Bạn là nhân viên tư vấn Dell Store. Trả lời ngắn gọn, tiếng Việt.";

                GroqChatRequest request = GroqChatRequest.builder()
                        .model(groqModel)
                        .messages(List.of(
                                GroqChatRequest.Message.builder().role("system").content(systemPrompt).build(),
                                GroqChatRequest.Message.builder().role("user").content(prompt).build()))
                        .temperature(0.5) 
                        .maxTokens(600)
                        .build();

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setBearerAuth(groqApiKey);

                HttpEntity<GroqChatRequest> httpEntity = new HttpEntity<>(request, headers);

                ResponseEntity<GroqChatResponse> response = restTemplate.postForEntity(
                        groqApiUrl, httpEntity, GroqChatResponse.class);

                if (response.getStatusCode().is2xxSuccessful()) {
                    return response.getBody();
                } else if (response.getStatusCode().is5xxServerError()) {
                    Thread.sleep(backoff);
                    backoff *= 2;
                } else {
                    return null;
                }

            } catch (Exception e) {
                log.error("Groq API error attempt {}: {}", attempt, e.getMessage());
                try { Thread.sleep(backoff); } catch (InterruptedException ignored) {}
            }
        }
        return null;
    }

    private List<SanPham> extractRecommendedProducts(String responseText) {
        List<SanPham> products = new ArrayList<>();
        try {
            Pattern idPattern = Pattern.compile("ID:\\s*([a-f0-9-]{36})", Pattern.CASE_INSENSITIVE);
            Matcher matcher = idPattern.matcher(responseText);
            Set<UUID> foundIds = new HashSet<>();
            while (matcher.find()) {
                try {
                    foundIds.add(UUID.fromString(matcher.group(1)));
                } catch (Exception ignored) {}
            }
            for (UUID productId : foundIds) {
                sanPhamRepo.findById(productId).ifPresent(products::add);
            }
        } catch (Exception ignored) {}
        return products;
    }

    private ChatbotResponse buildResponseWithProducts(String responseText, List<SanPham> recommendedProducts) {
        return ChatbotResponse.builder()
                .responseText(responseText)
                .intentCode("GROQ_CONSULTATION")
                .confidence(BigDecimal.valueOf(0.95))
                .shouldSave(true)
                .shouldEscalate(false)
                .build();
    }

    private String formatPrice(long price) {
        return String.format("%,d", price).replace(",", ".");
    }
}
