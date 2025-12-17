package com.example.backendlaptop.controller.chat;

import com.example.backendlaptop.dto.chat.ChatRequest;
import com.example.backendlaptop.dto.chat.ChatResponse;
import com.example.backendlaptop.dto.chat.ChatbotResponse;
import com.example.backendlaptop.dto.chat.ConsultationDataDTO;
import com.example.backendlaptop.dto.chat.ConversationResponse;
import com.example.backendlaptop.exception.*;
import com.example.backendlaptop.model.response.ResponseObject;
import com.example.backendlaptop.entity.ChiTietSanPham;
import com.example.backendlaptop.entity.SanPham;
import com.example.backendlaptop.repository.ChiTietSanPhamRepository;
import com.example.backendlaptop.repository.SanPhamRepository;
import com.example.backendlaptop.service.chat.ChatService;
import com.example.backendlaptop.service.chat.ChatbotService;
import com.example.backendlaptop.service.chat.GeminiChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatService chatService;
    private final GeminiChatService geminiChatService;
    private final ChatbotService chatbotService;
    private final SanPhamRepository sanPhamRepository;
    private final ChiTietSanPhamRepository chiTietSanPhamRepository;

    /**
     * Gửi tin nhắn mới
     */
    @PostMapping("/send")
    public ResponseEntity<ResponseObject<ChatResponse>> sendMessage(@Valid @RequestBody ChatRequest request) {
        log.info("📨 Nhận request gửi tin nhắn: khachHangId={}, nhanVienId={}, isFromCustomer={}",
                request.getKhachHangId(), request.getNhanVienId(), request.getIsFromCustomer());

        // Validate message length
        if (request.getNoiDung() != null && request.getNoiDung().length() > 5000) {
            throw new ChatMessageTooLongException(5000, request.getNoiDung().length());
        }

        ChatResponse response = chatService.sendMessage(request);
        return ResponseEntity.ok(new ResponseObject<>(response, "Gửi tin nhắn thành công"));
    }

    /**
     * Lấy danh sách tin nhắn trong một conversation
     */
    @GetMapping("/conversation/{conversationId}/messages")
    public ResponseEntity<ResponseObject<List<ChatResponse>>> getMessages(
            @PathVariable UUID conversationId) {
        List<ChatResponse> messages = chatService.getMessagesByConversationId(conversationId);
        return ResponseEntity.ok(new ResponseObject<>(messages, "Lấy danh sách tin nhắn thành công"));
    }

    /**
     * Lấy danh sách conversation của khách hàng
     */
    @GetMapping("/customer/{khachHangId}/conversations")
    public ResponseEntity<ResponseObject<List<ConversationResponse>>> getCustomerConversations(
            @PathVariable UUID khachHangId) {
        List<ConversationResponse> conversations = chatService.getConversationsByKhachHang(khachHangId);
        return ResponseEntity.ok(new ResponseObject<>(conversations, "Lấy danh sách cuộc trò chuyện thành công"));
    }

    /**
     * Lấy danh sách conversation của nhân viên
     */
    @GetMapping("/staff/{nhanVienId}/conversations")
    public ResponseEntity<ResponseObject<List<ConversationResponse>>> getStaffConversations(
            @PathVariable UUID nhanVienId) {
        List<ConversationResponse> conversations = chatService.getConversationsByNhanVien(nhanVienId);
        return ResponseEntity.ok(new ResponseObject<>(conversations, "Lấy danh sách cuộc trò chuyện thành công"));
    }

    /**
     * Lấy tất cả conversation (cho admin)
     */
    @GetMapping("/conversations")
    public ResponseEntity<ResponseObject<List<ConversationResponse>>> getAllConversations() {
        List<ConversationResponse> conversations = chatService.getAllConversations();
        return ResponseEntity.ok(new ResponseObject<>(conversations, "Lấy danh sách cuộc trò chuyện thành công"));
    }

    /**
     * Đánh dấu tin nhắn đã đọc
     */
    @PutMapping("/conversation/{conversationId}/read")
    public ResponseEntity<ResponseObject<Void>> markAsRead(
            @PathVariable UUID conversationId,
            @RequestParam Boolean isFromCustomer) {
        chatService.markAsRead(conversationId, isFromCustomer);
        return ResponseEntity.ok(new ResponseObject<>(null, "Đã đánh dấu đọc"));
    }

    /**
     * Yêu cầu hỗ trợ từ nhân viên (Escalate)
     */
    @PostMapping("/escalate/{conversationId}")
    public ResponseEntity<ResponseObject<Void>> escalateConversation(@PathVariable UUID conversationId) {
        chatbotService.escalateConversation(conversationId);
        return ResponseEntity.ok(new ResponseObject<>(null, "Đã chuyển cuộc trò chuyện cho nhân viên"));
    }

    /**
     * Bật lại Chatbot (Handover back to AI)
     */
    @PostMapping("/turn-bot-on/{conversationId}")
    public ResponseEntity<ResponseObject<Void>> turnBotOn(@PathVariable UUID conversationId) {
        chatbotService.turnBotBackOn(conversationId);
        return ResponseEntity.ok(new ResponseObject<>(null, "Đã bật lại trợ lý ảo cho cuộc trò chuyện này"));
    }

    /**
     * Đếm số tin nhắn chưa đọc của khách hàng
     */
    @GetMapping("/customer/{khachHangId}/unread-count")
    public ResponseEntity<ResponseObject<Long>> getUnreadCountByCustomer(
            @PathVariable UUID khachHangId) {
        Long count = chatService.countUnreadByKhachHang(khachHangId);
        return ResponseEntity.ok(new ResponseObject<>(count, "Lấy số tin nhắn chưa đọc thành công"));
    }

    /**
     * Đếm số tin nhắn chưa đọc của nhân viên
     */
    @GetMapping("/staff/{nhanVienId}/unread-count")
    public ResponseEntity<ResponseObject<Long>> getUnreadCountByStaff(
            @PathVariable UUID nhanVienId) {
        Long count = chatService.countUnreadByNhanVien(nhanVienId);
        return ResponseEntity.ok(new ResponseObject<>(count, "Lấy số tin nhắn chưa đọc thành công"));
    }

    /**
     * Tìm hoặc tạo conversation giữa khách hàng và nhân viên
     */
    @GetMapping("/find-conversation")
    public ResponseEntity<ResponseObject<UUID>> findOrCreateConversation(
            @RequestParam UUID khachHangId,
            @RequestParam(required = false) UUID nhanVienId) {
        try {
            UUID conversationId = chatService.findOrCreateConversation(khachHangId, nhanVienId);
            return ResponseEntity.ok(new ResponseObject<>(conversationId, "Tìm conversation thành công"));
        } catch (Exception e) {
            // Log lỗi và trả về null (sẽ tạo conversation mới khi gửi tin nhắn)
            System.err.println("Lỗi khi tìm conversation: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(new ResponseObject<>(null, "Chưa có conversation, sẽ tạo mới khi gửi tin nhắn"));
        }
    }

    // Exception handlers
    @ExceptionHandler(ChatRateLimitExceededException.class)
    public ResponseEntity<ResponseObject<Object>> handleRateLimitExceeded(ChatRateLimitExceededException e) {
        log.warn("Rate limit exceeded: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(new ResponseObject<Object>(false, null, e.getUserMessage()));
    }

    @ExceptionHandler(ChatMessageTooLongException.class)
    public ResponseEntity<ResponseObject<Object>> handleMessageTooLong(ChatMessageTooLongException e) {
        log.warn("Message too long: {} characters (max: {})", e.getActualLength(), e.getMaxLength());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ResponseObject<Object>(false, null, e.getUserMessage()));
    }

    @ExceptionHandler(ChatConversationNotFoundException.class)
    public ResponseEntity<ResponseObject<Object>> handleConversationNotFound(ChatConversationNotFoundException e) {
        log.warn("Conversation not found: {}", e.getConversationId());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ResponseObject<Object>(false, null, e.getUserMessage()));
    }

    @ExceptionHandler(ChatException.class)
    public ResponseEntity<ResponseObject<Object>> handleChatException(ChatException e) {
        log.error("Chat error: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ResponseObject<Object>(false, null, e.getUserMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseObject<Object>> handleGenericException(Exception e) {
        log.error("Unexpected error in chat controller: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseObject<Object>(false, null, "Đã xảy ra lỗi. Vui lòng thử lại sau."));
    }

    /**
     * Consultation endpoint - Tư vấn sản phẩm với Gemini AI
     */
    @PostMapping("/consult")
    public ResponseEntity<ResponseObject<ChatbotResponse>> consult(
            @Valid @RequestBody ConsultationDataDTO consultationData) {
        log.info("🤖 [Chat] Consultation request received: purpose={}, budget={}, features={}",
                consultationData.getPurposes(), consultationData.getBudget(), consultationData.getFeatures());

        try {
            // Convert DTO to Map for GeminiChatService
            java.util.Map<String, Object> consultationMap = new java.util.HashMap<>();
            consultationMap.put("purposes", consultationData.getPurposes());
            consultationMap.put("budget", consultationData.getBudget());
            consultationMap.put("features", consultationData.getFeatures());
            consultationMap.put("userMessage", consultationData.getUserMessage());

            // Call Gemini service
            ChatbotResponse response = geminiChatService.consultWithGemini(
                    consultationData.getUserMessage() != null ? consultationData.getUserMessage()
                            : "Tư vấn chọn laptop",
                    null, // khachHangId can be null for anonymous consultation
                    consultationMap);

            // Fallback to database-based recommendations if Gemini fails
            if (response == null) {
                log.warn("⚠️ [Chat] Gemini failed, using database-based fallback recommendations");
                response = buildFallbackResponseWithProducts(consultationData);
            }

            if (response == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ResponseObject<>(false, null, "Không thể tư vấn lúc này. Vui lòng thử lại sau."));
            }

            return ResponseEntity.ok(new ResponseObject<>(response, "Tư vấn thành công"));

        } catch (Exception e) {
            log.error("❌ [Chat] Error in consultation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseObject<>(false, null, "Đã xảy ra lỗi khi tư vấn. Vui lòng thử lại sau."));
        }
    }

    /**
     * Build fallback response with product recommendations from database
     */
    private ChatbotResponse buildFallbackResponseWithProducts(ConsultationDataDTO consultationData) {
        StringBuilder message = new StringBuilder();
        message.append("Xin chào! Dựa trên thông tin bạn đã cung cấp, tôi đã tìm được một số sản phẩm phù hợp:\n\n");

        try {
            // Query products based on consultation data
            Long budget = consultationData.getBudget();
            List<String> features = consultationData.getFeatures();

            // Get all active products
            List<SanPham> allProducts = sanPhamRepository.findByTrangThai(1);

            // Filter products by budget and features
            List<SanPham> recommendedProducts = filterProductsByCriteria(allProducts, budget, features);

            // Limit to top 5 products
            if (recommendedProducts.size() > 5) {
                recommendedProducts = recommendedProducts.subList(0, 5);
            }

            if (recommendedProducts.isEmpty()) {
                message.append("Hiện tại không tìm thấy sản phẩm phù hợp với yêu cầu của bạn. ");
                message.append("Vui lòng liên hệ nhân viên tư vấn để được hỗ trợ tốt nhất.\n\n");
            } else {
                int index = 1;
                for (SanPham product : recommendedProducts) {
                    List<ChiTietSanPham> variants = chiTietSanPhamRepository.findBySanPham_Id(product.getId());
                    if (variants.isEmpty())
                        continue;

                    ChiTietSanPham variant = variants.get(0);
                    BigDecimal minPrice = variants.stream()
                            .map(ChiTietSanPham::getGiaBan)
                            .filter(java.util.Objects::nonNull)
                            .min(BigDecimal::compareTo)
                            .orElse(BigDecimal.ZERO);

                    message.append(String.format("%d. %s\n", index, product.getTenSanPham()));
                    message.append(String.format("   - Giá từ: %s VNĐ\n", formatPrice(minPrice.longValue())));

                    if (variant.getCpu() != null) {
                        message.append(String.format("   - CPU: %s\n", variant.getCpu().getTenCpu()));
                    }
                    if (variant.getRam() != null) {
                        message.append(String.format("   - RAM: %s\n", variant.getRam().getTenRam()));
                    }
                    if (variant.getOCung() != null) {
                        message.append(String.format("   - Ổ cứng: %s\n", variant.getOCung().getDungLuong()));
                    }
                    message.append("\n");
                    index++;
                }

                message.append("Bạn có thể xem chi tiết và đặt hàng các sản phẩm trên trên website. ");
                message.append("Nếu cần tư vấn thêm, vui lòng liên hệ nhân viên của chúng tôi.\n\n");
            }

        } catch (Exception e) {
            log.error("❌ [Chat] Error building fallback response: {}", e.getMessage(), e);
            message.append(
                    "Đã xảy ra lỗi khi tìm kiếm sản phẩm. Vui lòng thử lại sau hoặc liên hệ nhân viên tư vấn.\n\n");
        }

        message.append("Cảm ơn bạn đã quan tâm!");

        return ChatbotResponse.builder()
                .responseText(message.toString())
                .intentCode("CONSULTATION_FALLBACK")
                .confidence(BigDecimal.valueOf(0.7))
                .shouldSave(true)
                .shouldEscalate(false)
                .build();
    }

    /**
     * Filter products based on budget and features
     */
    private List<SanPham> filterProductsByCriteria(List<SanPham> products, Long budget, List<String> features) {
        List<SanPham> filtered = new java.util.ArrayList<>();

        for (SanPham product : products) {
            List<ChiTietSanPham> variants = chiTietSanPhamRepository.findBySanPham_Id(product.getId());
            if (variants.isEmpty())
                continue;

            // Check budget
            if (budget != null && budget > 0) {
                BigDecimal minPrice = variants.stream()
                        .map(ChiTietSanPham::getGiaBan)
                        .filter(java.util.Objects::nonNull)
                        .min(BigDecimal::compareTo)
                        .orElse(BigDecimal.ZERO);

                // Filter: price should be within budget (allow 20% over budget)
                BigDecimal budgetBigDecimal = BigDecimal.valueOf(budget);
                BigDecimal maxBudget = budgetBigDecimal.multiply(BigDecimal.valueOf(1.2));
                if (minPrice.compareTo(maxBudget) > 0) {
                    continue;
                }
            }

            // Check features (if specified)
            if (features != null && !features.isEmpty()) {
                boolean matchesFeature = false;
                for (ChiTietSanPham variant : variants) {
                    String variantInfo = buildVariantInfoString(variant).toLowerCase();
                    for (String feature : features) {
                        if (variantInfo.contains(feature.toLowerCase())) {
                            matchesFeature = true;
                            break;
                        }
                    }
                    if (matchesFeature)
                        break;
                }
                if (!matchesFeature && !features.isEmpty()) {
                    continue; // Skip if no feature matches
                }
            }

            filtered.add(product);
        }

        // Sort by price (ascending)
        filtered.sort((p1, p2) -> {
            List<ChiTietSanPham> v1 = chiTietSanPhamRepository.findBySanPham_Id(p1.getId());
            List<ChiTietSanPham> v2 = chiTietSanPhamRepository.findBySanPham_Id(p2.getId());
            BigDecimal price1 = v1.stream()
                    .map(ChiTietSanPham::getGiaBan)
                    .filter(java.util.Objects::nonNull)
                    .min(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);
            BigDecimal price2 = v2.stream()
                    .map(ChiTietSanPham::getGiaBan)
                    .filter(java.util.Objects::nonNull)
                    .min(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);
            return price1.compareTo(price2);
        });

        return filtered;
    }

    private String buildVariantInfoString(ChiTietSanPham variant) {
        StringBuilder info = new StringBuilder();
        if (variant.getCpu() != null)
            info.append(variant.getCpu().getTenCpu()).append(" ");
        if (variant.getRam() != null)
            info.append(variant.getRam().getTenRam()).append(" ");
        if (variant.getGpu() != null)
            info.append(variant.getGpu().getTenGpu()).append(" ");
        if (variant.getOCung() != null)
            info.append(variant.getOCung().getDungLuong()).append(" ");
        return info.toString();
    }

    private String formatPrice(long price) {
        return String.format("%,d", price).replace(",", ".");
    }
}
