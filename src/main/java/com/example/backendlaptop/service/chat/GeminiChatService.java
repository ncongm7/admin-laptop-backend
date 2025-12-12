package com.example.backendlaptop.service.chat;

import com.example.backendlaptop.dto.chat.ChatbotResponse;
import com.example.backendlaptop.dto.chat.GroqChatRequest;
import com.example.backendlaptop.dto.chat.GroqChatResponse;
import com.example.backendlaptop.entity.ChiTietSanPham;
import com.example.backendlaptop.entity.SanPham;
import com.example.backendlaptop.repository.ChiTietSanPhamRepository;
import com.example.backendlaptop.repository.SanPhamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
@RequiredArgsConstructor
public class GeminiChatService {

    private final SanPhamRepository sanPhamRepo;
    private final ChiTietSanPhamRepository chiTietSanPhamRepo;
    private final RestTemplate restTemplate;

    @Value("${groq.api.key:}")
    private String groqApiKey;

    @Value("${groq.api.model:llama-3.1-8b-instant}")
    private String groqModel;

    @Value("${groq.api.url:https://api.groq.com/openai/v1/chat/completions}")
    private String groqApiUrl;

    @Value("${groq.api.enabled:true}")
    private boolean groqEnabled;

    // Cache product context (refresh every 5 minutes)
    private String cachedProductContext;
    private long contextCacheTime = 0;
    private static final long CACHE_DURATION_MS = 5 * 60 * 1000; // 5 minutes

    /**
     * Main method: Tư vấn sản phẩm với Gemini API
     */
    public ChatbotResponse consultWithGemini(String userMessage, UUID khachHangId, Map<String, Object> consultationData) {
        if (!groqEnabled || groqApiKey == null || groqApiKey.isEmpty()) {
            log.warn("Groq API is disabled or API key not configured. Falling back to ChatbotService.");
            return null; // Will trigger fallback
        }

        try {
            log.info("🤖 [Groq] Processing consultation request for customer: {}", khachHangId);

            // 1. Build product context from database
            String productContext = buildProductContext();

            // 2. Build consultation prompt
            String prompt = buildConsultationPrompt(userMessage, consultationData, productContext);

            // 3. Call Groq API
            GroqChatResponse groqResponse = callGroqAPI(prompt);

            if (groqResponse == null || groqResponse.getText() == null) {
                log.warn("Groq API returned null or empty response. Falling back to ChatbotService.");
                return null;
            }

            String responseText = groqResponse.getText();
            log.info("✅ [Groq] Received response (length: {})", responseText.length());

            // 4. Extract product recommendations from response
            List<SanPham> recommendedProducts = extractRecommendedProducts(responseText);

            // 5. Build ChatbotResponse with products
            return buildResponseWithProducts(responseText, recommendedProducts);

        } catch (RestClientException e) {
            log.error("❌ [Groq] HTTP error calling Groq API: {}", e.getMessage(), e);
            return null; // Fallback to ChatbotService
        } catch (Exception e) {
            log.error("❌ [Groq] Error processing Groq consultation: {}", e.getMessage(), e);
            return null; // Fallback to ChatbotService
        }
    }

    /**
     * Build product context string from database
     */
    public String buildProductContext() {
        // Check cache
        long now = System.currentTimeMillis();
        if (cachedProductContext != null && (now - contextCacheTime) < CACHE_DURATION_MS) {
            log.debug("📦 [Groq] Using cached product context");
            return cachedProductContext;
        }

        log.info("📦 [Groq] Building product context from database...");

        try {
            // Get all active products
            List<SanPham> products = sanPhamRepo.findByTrangThai(1);
            
            // Limit to top 50 to avoid prompt too long
            if (products.size() > 50) {
                products = products.subList(0, 50);
            }

            StringBuilder context = new StringBuilder();
            context.append("Danh sách sản phẩm laptop hiện có trong cửa hàng:\n\n");

            int index = 1;
            for (SanPham product : products) {
                // Get product variants
                List<ChiTietSanPham> variants = chiTietSanPhamRepo.findBySanPham_Id(product.getId());
                
                if (variants.isEmpty()) {
                    continue; // Skip products without variants
                }

                // Get first variant for specs
                ChiTietSanPham variant = variants.get(0);

                context.append(String.format("%d. %s (ID: %s)\n", index, product.getTenSanPham(), product.getId()));

                // CPU
                if (variant.getCpu() != null) {
                    context.append(String.format("   - CPU: %s\n", variant.getCpu().getTenCpu()));
                }

                // RAM
                if (variant.getRam() != null) {
                    context.append(String.format("   - RAM: %s\n", variant.getRam().getTenRam()));
                }

                // Ổ cứng
                if (variant.getOCung() != null) {
                    context.append(String.format("   - Ổ cứng: %s\n", variant.getOCung().getDungLuong()));
                }

                // GPU
                if (variant.getGpu() != null) {
                    context.append(String.format("   - GPU: %s\n", variant.getGpu().getTenGpu()));
                }

                // Màn hình
                if (variant.getLoaiManHinh() != null) {
                    context.append(String.format("   - Màn hình: %s\n", variant.getLoaiManHinh().getKichThuoc()));
                }

                // Pin
                if (variant.getPin() != null) {
                    context.append(String.format("   - Pin: %s\n", variant.getPin().getDungLuongPin()));
                }

                // Giá (min và max từ all variants)
                BigDecimal minPrice = variants.stream()
                        .map(ChiTietSanPham::getGiaBan)
                        .filter(Objects::nonNull)
                        .min(BigDecimal::compareTo)
                        .orElse(BigDecimal.ZERO);

                BigDecimal maxPrice = variants.stream()
                        .map(ChiTietSanPham::getGiaBan)
                        .filter(Objects::nonNull)
                        .max(BigDecimal::compareTo)
                        .orElse(BigDecimal.ZERO);

                if (minPrice.compareTo(BigDecimal.ZERO) > 0 || maxPrice.compareTo(BigDecimal.ZERO) > 0) {
                    if (minPrice.equals(maxPrice)) {
                        context.append(String.format("   - Giá: %s VNĐ\n", formatPrice(minPrice.longValue())));
                    } else {
                        context.append(String.format("   - Giá: %s - %s VNĐ\n", 
                                formatPrice(minPrice.longValue()), 
                                formatPrice(maxPrice.longValue())));
                    }
                } else {
                    context.append("   - Giá: Liên hệ\n");
                }

                context.append("\n");
                index++;
            }

            // Cache the context
            cachedProductContext = context.toString();
            contextCacheTime = now;

            log.info("✅ [Groq] Built product context with {} products", products.size());
            return cachedProductContext;

        } catch (Exception e) {
            log.error("❌ [Groq] Error building product context: {}", e.getMessage(), e);
            return "Không thể tải thông tin sản phẩm từ database.";
        }
    }

    /**
     * Build consultation prompt for Gemini
     */
    private String buildConsultationPrompt(String userMessage, Map<String, Object> consultationData, String productContext) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Bạn là nhân viên tư vấn laptop tại cửa hàng Dell. ");
        prompt.append("Hãy trò chuyện với khách hàng một cách tự nhiên, thân thiện như đang nói chuyện trực tiếp, không cứng nhắc như chatbot. ");
        prompt.append("Sử dụng ngôn ngữ gần gũi, nhiệt tình, nhưng vẫn chuyên nghiệp. ");
        prompt.append("Trả lời ngắn gọn, dễ hiểu, không dùng format list hay menu trừ khi khách hàng yêu cầu.\n\n");

        // User message
        if (userMessage != null && !userMessage.trim().isEmpty()) {
            prompt.append("Câu hỏi/yêu cầu của khách hàng: ").append(userMessage).append("\n\n");
        }

        // Check if this is a consultation request or normal chat
        boolean isConsultation = consultationData != null && 
                                 (consultationData.get("budget") != null || 
                                  (consultationData.get("purposes") != null && !((List<?>) consultationData.get("purposes")).isEmpty()) ||
                                  (consultationData.get("features") != null && !((List<?>) consultationData.get("features")).isEmpty()));

        if (isConsultation) {
            // Consultation data
            prompt.append("Thông tin đã thu thập từ khách hàng:\n");

            // Purpose
            Object purposesObj = consultationData.get("purposes");
            if (purposesObj != null) {
                if (purposesObj instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<String> purposes = (List<String>) purposesObj;
                    if (!purposes.isEmpty()) {
                        prompt.append("- Mục đích sử dụng: ").append(String.join(", ", purposes)).append("\n");
                    }
                } else {
                    prompt.append("- Mục đích sử dụng: ").append(purposesObj.toString()).append("\n");
                }
            }

            // Budget
            Object budgetObj = consultationData.get("budget");
            if (budgetObj != null) {
                long budget = budgetObj instanceof Number ? ((Number) budgetObj).longValue() : Long.parseLong(budgetObj.toString());
                prompt.append("- Ngân sách: ").append(formatPrice(budget)).append(" VNĐ\n");
            }

            // Features
            Object featuresObj = consultationData.get("features");
            if (featuresObj != null) {
                if (featuresObj instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<String> features = (List<String>) featuresObj;
                    if (!features.isEmpty()) {
                        prompt.append("- Tính năng quan trọng: ").append(String.join(", ", features)).append("\n");
                    }
                } else {
                    prompt.append("- Tính năng quan trọng: ").append(featuresObj.toString()).append("\n");
                }
            }

            prompt.append("\n");
        }

        // Product context
        prompt.append(productContext);
        prompt.append("\n");

        // Instructions - khác nhau cho consultation và normal chat
        if (isConsultation) {
            prompt.append("Hãy tư vấn 3-5 sản phẩm phù hợp nhất dựa trên yêu cầu của khách hàng một cách tự nhiên. ");
            prompt.append("Trong câu trả lời, hãy đề cập đến Product ID (ID: xxx) của từng sản phẩm được đề xuất một cách tự nhiên trong đoạn văn. ");
            prompt.append("Giải thích lý do tại sao sản phẩm đó phù hợp như đang nói chuyện với bạn bè. ");
            prompt.append("Đề xuất 1 sản phẩm tốt nhất và giải thích chi tiết. ");
            prompt.append("KHÔNG dùng format list hay bullet points, hãy viết như một đoạn văn tự nhiên.\n\n");
        } else {
            prompt.append("Hãy trả lời câu hỏi của khách hàng một cách tự nhiên, như đang nói chuyện với bạn bè. ");
            prompt.append("Nếu khách hàng chào hỏi, hãy chào lại một cách thân thiện và hỏi xem bạn có thể giúp gì. ");
            prompt.append("Nếu khách hàng hỏi về sản phẩm, hãy đề xuất sản phẩm phù hợp từ danh sách trên một cách tự nhiên, không liệt kê như menu. ");
            prompt.append("Nếu khách hàng hỏi về thông tin chung (giờ mở cửa, địa chỉ, chính sách, v.v.), hãy trả lời dựa trên kiến thức của bạn. ");
            prompt.append("Luôn trò chuyện tự nhiên, không dùng format list hay bullet points trừ khi thực sự cần thiết. ");
            prompt.append("Hãy như một người bạn đang tư vấn, không phải một chatbot cứng nhắc.\n\n");
        }
        
        prompt.append("QUAN TRỌNG: Trả lời bằng tiếng Việt, tự nhiên như đang nói chuyện trực tiếp. ");
        prompt.append("KHÔNG dùng format list, menu, hay bullet points. ");
        prompt.append("Hãy viết như một đoạn văn tự nhiên, thân thiện.");

        return prompt.toString();
    }

    /**
     * Call Groq API (OpenAI-compatible)
     */
    private GroqChatResponse callGroqAPI(String prompt) {
        try {
            String systemPrompt = "Bạn là nhân viên tư vấn laptop tại cửa hàng Dell. "
                    + "Hãy trả lời ngắn gọn, tự nhiên, tiếng Việt, không dùng bullet trừ khi thật cần thiết. "
                    + "Ưu tiên gợi ý sản phẩm phù hợp từ context.";

            GroqChatRequest request = GroqChatRequest.builder()
                    .model(groqModel)
                    .messages(List.of(
                            GroqChatRequest.Message.builder().role("system").content(systemPrompt).build(),
                            GroqChatRequest.Message.builder().role("user").content(prompt).build()
                    ))
                    .temperature(0.6)
                    .maxTokens(800)
                    .build();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(groqApiKey);

            HttpEntity<GroqChatRequest> httpEntity = new HttpEntity<>(request, headers);

            log.debug("📤 [Groq] Calling Groq API: {}", groqApiUrl);
            ResponseEntity<GroqChatResponse> response = restTemplate.postForEntity(
                    groqApiUrl, httpEntity, GroqChatResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("✅ [Groq] API call successful");
                return response.getBody();
            } else {
                log.warn("⚠️ [Groq] API returned non-2xx status: {}", response.getStatusCode());
                return null;
            }

        } catch (RestClientException e) {
            log.error("❌ [Groq] Error calling Groq API: {}", e.getMessage(), e);
            // Don't throw, return null to trigger fallback
            return null;
        }
    }

    /**
     * Extract product recommendations from Gemini response text
     * Tìm các Product ID trong response (format: "ID: xxx")
     */
    private List<SanPham> extractRecommendedProducts(String responseText) {
        List<SanPham> products = new ArrayList<>();

        try {
            // Pattern to find "ID: xxx" in response
            Pattern idPattern = Pattern.compile("ID:\\s*([a-f0-9-]{36})", Pattern.CASE_INSENSITIVE);
            Matcher matcher = idPattern.matcher(responseText);

            Set<UUID> foundIds = new HashSet<>();
            while (matcher.find()) {
                try {
                    UUID productId = UUID.fromString(matcher.group(1));
                    foundIds.add(productId);
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid UUID format in response: {}", matcher.group(1));
                }
            }

            // Fetch products from database
            for (UUID productId : foundIds) {
                sanPhamRepo.findById(productId).ifPresent(products::add);
            }

            log.info("📦 [Groq] Extracted {} product recommendations", products.size());

        } catch (Exception e) {
            log.error("❌ [Gemini] Error extracting products from response: {}", e.getMessage(), e);
        }

        return products;
    }

    /**
     * Build ChatbotResponse with products
     */
    private ChatbotResponse buildResponseWithProducts(String responseText, List<SanPham> recommendedProducts) {
        ChatbotResponse.ChatbotResponseBuilder builder = ChatbotResponse.builder()
                .responseText(responseText)
                .intentCode("GROQ_CONSULTATION")
                .confidence(BigDecimal.valueOf(0.9)) // High confidence for Groq
                .shouldSave(true)
                .shouldEscalate(false);

        // Không thêm quick replies để trò chuyện tự nhiên hơn
        // Chỉ thêm nếu khách hàng yêu cầu xem sản phẩm cụ thể
        // Quick replies sẽ làm response trông như menu, không tự nhiên

        return builder.build();
    }

    /**
     * Format price to Vietnamese format
     */
    private String formatPrice(long price) {
        return String.format("%,d", price).replace(",", ".");
    }
}

