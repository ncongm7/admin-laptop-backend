package com.example.backendlaptop.service.chat;

import com.example.backendlaptop.dto.chat.*;
import com.example.backendlaptop.entity.*;
import com.example.backendlaptop.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatbotService {

    private final ChatIntentRepository intentRepo;
    private final ChatSessionRepository sessionRepo;
    private final ChatRepository chatRepo;
    private final ChatQuickReplyRepository quickReplyRepo;
    private final ChatAnalyticsRepository analyticsRepo;
    private final ObjectMapper objectMapper;

    // NEW: Conversation Flow Service
    private final ConversationFlowService conversationFlowService;

    // Repositories for requires_data logic
    private final SanPhamRepository sanPhamRepo;
    private final ChiTietSanPhamRepository chiTietSanPhamRepo;
    private final com.example.backendlaptop.repository.banhang.HoaDonRepository hoaDonRepo;
    private final com.example.backendlaptop.repository.KhachHangRepository khachHangRepo;

    // Configuration
    private static final double DEFAULT_CONFIDENCE_THRESHOLD = 0.7;
    private static final int MIN_KEYWORD_MATCHES = 1;
    private static final int MAX_UNCLEAR_MESSAGES_BEFORE_ESCALATION = 2;

    /**
     * Main entry point: Phân tích tin nhắn khách hàng và tạo response tự động
     */
    @Transactional
    public ChatbotResponse processCustomerMessage(ChatResponse customerMessage) {
        log.info("🤖 [Chatbot] Processing message: {}", customerMessage.getNoiDung());

        try {
            // 1. Get or create session
            ChatSession session = getOrCreateSession(
                    customerMessage.getConversationId(),
                    customerMessage.getKhachHangId());

            // 2. Check if bot should handle
            if (Boolean.FALSE.equals(session.getIsBotHandling())) {
                log.info("⏭️ [Chatbot] Session already escalated to human, skipping bot");
                return null;
            }

            // 3. Check if stuck (too many steps)
            if (Boolean.TRUE.equals(session.getIsStuck())) {
                log.warn("⚠️ [Chatbot] Session is stuck, escalating");
                return handleLowConfidence(session, customerMessage);
            }

            // 4. Detect intent with context awareness
            long startTime = System.currentTimeMillis();
            IntentMatch match = detectIntentWithContext(customerMessage.getNoiDung(), session);
            long responseTime = System.currentTimeMillis() - startTime;

            // 5. Save analytics
            saveAnalytics(customerMessage, match, responseTime);

            // 6. Handle based on confidence and business role
            if (match == null || match.getConfidence().doubleValue() < DEFAULT_CONFIDENCE_THRESHOLD) {
                return handleLowConfidence(session, customerMessage);
            }

            // 7. Validate business role
            if (match.getIntent().getBusinessRole() == null) {
                log.warn("⚠️ [Chatbot] Intent {} has no business role", match.getIntentCode());
                return handleLowConfidence(session, customerMessage);
            }

            // 8. 🔥 NEW: Use Flow-based Processing
            return conversationFlowService.processFlow(
                    session,
                    match,
                    customerMessage.getNoiDung(),
                    customerMessage.getKhachHangId());

        } catch (Exception e) {
            log.error("❌ [Chatbot] Error processing message", e);
            return ChatbotResponse.simpleResponse(
                    "Xin lỗi, mình đang gặp sự cố kỹ thuật. Vui lòng thử lại sau hoặc liên hệ nhân viên ạ!",
                    "ERROR",
                    BigDecimal.ZERO);
        }
    }

    /**
     * Intent detection sử dụng keyword matching với fuzzy matching và synonyms
     */
    private IntentMatch detectIntent(String message) {
        if (message == null || message.trim().isEmpty()) {
            return null;
        }

        String normalizedMessage = normalizeVietnamese(message.toLowerCase());
        List<ChatIntent> activeIntents = intentRepo.findByIsActiveTrueOrderByPriorityDesc();

        IntentMatch bestMatch = null;
        double highestScore = 0.0;

        for (ChatIntent intent : activeIntents) {
            double score = calculateIntentScore(normalizedMessage, intent);

            if (score > highestScore) {
                highestScore = score;
                bestMatch = new IntentMatch(intent.getIntentCode(), score, intent);
            }
        }

        log.info("🎯 [Chatbot] Best match: {} with confidence {}",
                bestMatch != null ? bestMatch.getIntentCode() : "NONE",
                bestMatch != null ? bestMatch.getConfidence() : 0);

        return bestMatch;
    }

    /**
     * Intent detection with context awareness
     */
    private IntentMatch detectIntentWithContext(String message, ChatSession session) {
        // First try normal detection
        IntentMatch match = detectIntent(message);

        // If low confidence and we have context, try to use context
        if (match == null || match.getConfidence().doubleValue() < 0.5) {
            String currentIntent = session.getCurrentIntent();
            if (currentIntent != null) {
                // Check if message is a follow-up question
                String normalizedMessage = normalizeVietnamese(message.toLowerCase());

                // Common follow-up patterns
                if (normalizedMessage.matches(".*(bao nhiêu|giá|price|cost|bao nhieu).*")) {
                    // Likely asking about price of previously mentioned product
                    if (currentIntent.equals("PRODUCT_INFO") || currentIntent.equals("PRODUCT_SEARCH")) {
                        log.info("🎯 [Chatbot] Context-aware: Follow-up price question for {}", currentIntent);
                        // Try to find PRODUCT_PRICE intent
                        return intentRepo.findByIntentCodeAndIsActiveTrue("PRODUCT_PRICE")
                                .map(intent -> new IntentMatch("PRODUCT_PRICE", 0.8, intent))
                                .orElse(match);
                    }
                }
            }
        }

        return match;
    }

    /**
     * Tính điểm match giữa message và intent keywords với fuzzy matching và
     * synonyms
     */
    private double calculateIntentScore(String message, ChatIntent intent) {
        try {
            List<String> keywords = objectMapper.readValue(
                    intent.getKeywords(),
                    new TypeReference<List<String>>() {
                    });

            double totalScore = 0.0;
            int totalKeywords = keywords.size();
            double fuzzyThreshold = 0.7; // 70% similarity threshold

            for (String keyword : keywords) {
                // Calculate score with fuzzy matching and synonyms
                double keywordScore = IntentMatchingUtils.calculateFuzzyScoreWithSynonyms(
                        message, keyword, fuzzyThreshold);
                totalScore += keywordScore;
            }

            if (totalScore < MIN_KEYWORD_MATCHES) {
                return 0.0;
            }

            // Base score: average of all keyword scores
            double baseScore = totalScore / totalKeywords;

            // Priority bonus (mỗi priority point thêm 2%)
            double priorityBonus = intent.getPriority() * 0.02;

            // Multiple keyword match bonus
            long strongMatches = keywords.stream()
                    .mapToLong(kw -> IntentMatchingUtils.calculateFuzzyScoreWithSynonyms(message, kw,
                            fuzzyThreshold) >= 0.8 ? 1 : 0)
                    .sum();
            double multipleMatchBonus = strongMatches > 2 ? 0.1 : 0.0;

            double finalScore = Math.min(baseScore + priorityBonus + multipleMatchBonus, 1.0);

            log.debug("📊 [Chatbot] Intent {} score: {} (base: {}, priority: {}, bonus: {})",
                    intent.getIntentCode(), finalScore, baseScore, priorityBonus, multipleMatchBonus);

            return finalScore;

        } catch (Exception e) {
            log.error("❌ [Chatbot] Error parsing keywords for intent {}", intent.getIntentCode(), e);
            return 0.0;
        }
    }

    /**
     * Generate response dựa trên intent với data từ database nếu cần
     */
    private ChatbotResponse generateResponse(IntentMatch match, ChatSession session, ChatResponse originalMessage) {
        ChatIntent intent = match.getIntent();

        String responseText = intent.getAutoResponseTemplate();
        List<QuickReplyDTO> quickReplies = loadQuickReplies(intent.getIntentCode());

        // Nếu requires_data = true, query database để lấy thông tin thực tế
        if (Boolean.TRUE.equals(intent.getRequiresData())) {
            String dataSource = intent.getDataSource();
            String intentCode = intent.getIntentCode();

            try {
                if ("san_pham".equals(dataSource) && intentCode.contains("PRICE")) {
                    // Query product price
                    String productInfo = fetchProductPrice(originalMessage.getNoiDung(), session);
                    if (productInfo != null) {
                        responseText = responseText.replace("{product_info}", productInfo);
                    }
                } else if ("bao_hanh".equals(dataSource) && intentCode.contains("WARRANTY")) {
                    // Query warranty info
                    // TODO: Implement when PhieuBaoHanhRepository is available
                    String warrantyInfo = "Tính năng tra cứu bảo hành đang được phát triển. Vui lòng liên hệ nhân viên để được hỗ trợ.";
                    if (warrantyInfo != null) {
                        responseText = responseText.replace("{warranty_info}", warrantyInfo);
                    }
                } else if ("hoa_don".equals(dataSource) && intentCode.contains("ORDER")) {
                    // Query order status
                    String orderInfo = fetchOrderStatus(originalMessage.getNoiDung(), session);
                    if (orderInfo != null) {
                        responseText = responseText.replace("{order_info}", orderInfo);
                    }
                }
            } catch (Exception e) {
                log.error("Error fetching data for intent {}: {}", intentCode, e.getMessage(), e);
                // Fallback to template without data
            }
        }

        return ChatbotResponse.builder()
                .responseText(responseText)
                .intentCode(match.getIntentCode())
                .confidence(match.getConfidence())
                .quickReplies(quickReplies)
                .shouldSave(true)
                .shouldEscalate(false)
                .build();
    }

    /**
     * Fetch product price from database
     */
    private String fetchProductPrice(String message, ChatSession session) {
        try {
            // Try to extract product name from message or context
            String productName = extractProductName(message, session);

            if (productName == null || productName.trim().isEmpty()) {
                return "Vui lòng cho mình biết tên sản phẩm bạn muốn hỏi giá nhé!";
            }

            // Search for products
            List<SanPham> products = sanPhamRepo.findByTenSanPhamContaining(productName);

            if (products.isEmpty()) {
                return String.format(
                        "Mình không tìm thấy sản phẩm \"%s\". Bạn có thể kiểm tra lại tên sản phẩm không ạ?",
                        productName);
            }

            // Get first active product
            SanPham product = products.stream()
                    .filter(p -> p.getTrangThai() != null && p.getTrangThai() == 1)
                    .findFirst()
                    .orElse(products.get(0));

            // Get price from ChiTietSanPham
            List<ChiTietSanPham> variants = chiTietSanPhamRepo.findBySanPham_Id(product.getId());

            if (variants.isEmpty()) {
                return String.format(
                        "Sản phẩm \"%s\" hiện chưa có thông tin giá. Vui lòng liên hệ nhân viên để biết thêm chi tiết.",
                        product.getTenSanPham());
            }

            // Get min and max price
            long minPrice = variants.stream()
                    .mapToLong(v -> v.getGiaBan() != null ? v.getGiaBan().longValue() : 0)
                    .min()
                    .orElse(0);

            long maxPrice = variants.stream()
                    .mapToLong(v -> v.getGiaBan() != null ? v.getGiaBan().longValue() : 0)
                    .max()
                    .orElse(0);

            if (minPrice == maxPrice) {
                return String.format("Sản phẩm \"%s\" có giá: %s VNĐ",
                        product.getTenSanPham(),
                        formatPrice(minPrice));
            } else {
                return String.format("Sản phẩm \"%s\" có giá từ %s đến %s VNĐ",
                        product.getTenSanPham(),
                        formatPrice(minPrice),
                        formatPrice(maxPrice));
            }

        } catch (Exception e) {
            log.error("Error fetching product price", e);
            return "Xin lỗi, mình không thể tìm thông tin giá sản phẩm lúc này. Vui lòng thử lại sau hoặc liên hệ nhân viên.";
        }
    }

    /**
     * Fetch warranty info from database
     */
    private String fetchWarrantyInfo(UUID khachHangId) {
        try {
            if (khachHangId == null) {
                return "Vui lòng đăng nhập để xem thông tin bảo hành của bạn.";
            }

            // TODO: Implement warranty query when PhieuBaoHanhRepository is available
            // Get warranty records for customer (through serial)
            // List<PhieuBaoHanh> allWarranties = phieuBaoHanhRepo.findAll();
            // List<PhieuBaoHanh> warranties = allWarranties.stream()
            // .filter(w -> w.getIdKhachHang() != null &&
            // w.getIdKhachHang().getId().equals(khachHangId))
            // .collect(Collectors.toList());

            return "Tính năng tra cứu bảo hành đang được phát triển. Vui lòng liên hệ nhân viên để được hỗ trợ.";

        } catch (Exception e) {
            log.error("Error fetching warranty info", e);
            return "Xin lỗi, mình không thể tìm thông tin bảo hành lúc này. Vui lòng thử lại sau.";
        }
    }

    /**
     * Fetch order status from database
     */
    private String fetchOrderStatus(String message, ChatSession session) {
        try {
            // Extract order code from message or context
            String orderCode = extractOrderCode(message, session);

            if (orderCode == null || orderCode.trim().isEmpty()) {
                return "Vui lòng cung cấp mã đơn hàng để mình tra cứu giúp bạn nhé!";
            }

            // Search for order
            Optional<HoaDon> orderOpt = hoaDonRepo.findByMa(orderCode);

            if (orderOpt.isEmpty()) {
                return String.format(
                        "Mình không tìm thấy đơn hàng với mã \"%s\". Bạn vui lòng kiểm tra lại mã đơn hàng nhé!",
                        orderCode);
            }

            HoaDon order = orderOpt.get();
            String status = getOrderStatusText(order.getTrangThai());

            return String.format("Đơn hàng %s:\n• Trạng thái: %s\n• Tổng tiền: %s VNĐ\n• Ngày tạo: %s",
                    order.getMa(),
                    status,
                    formatPrice(order.getTongTienSauGiam() != null ? order.getTongTienSauGiam().longValue() : 0),
                    order.getNgayTao() != null ? order.getNgayTao().toString() : "N/A");

        } catch (Exception e) {
            log.error("Error fetching order status", e);
            return "Xin lỗi, mình không thể tìm thông tin đơn hàng lúc này. Vui lòng thử lại sau.";
        }
    }

    /**
     * Extract product name from message or context
     */
    private String extractProductName(String message, ChatSession session) {
        // Try to get from context first
        try {
            if (session.getContextData() != null && !session.getContextData().isEmpty()) {
                Map<String, Object> context = objectMapper.readValue(
                        session.getContextData(),
                        new TypeReference<Map<String, Object>>() {
                        });
                String lastMessage = (String) context.get("last_message");
                if (lastMessage != null && lastMessage.length() > 5) {
                    // Simple extraction - can be improved
                    return lastMessage;
                }
            }
        } catch (Exception e) {
            log.debug("Failed to extract from context", e);
        }

        // Extract from current message (simple - remove common words)
        String normalized = normalizeVietnamese(message.toLowerCase());
        normalized = normalized.replaceAll("(giá|price|cost|bao nhiêu|bao nhieu|sản phẩm|san pham|sp|laptop)", "")
                .trim();

        return normalized.length() > 2 ? normalized : null;
    }

    /**
     * Extract order code from message or context
     */
    private String extractOrderCode(String message, ChatSession session) {
        // Try to get from context first
        try {
            if (session.getContextData() != null && !session.getContextData().isEmpty()) {
                Map<String, Object> context = objectMapper.readValue(
                        session.getContextData(),
                        new TypeReference<Map<String, Object>>() {
                        });
                String orderCode = (String) context.get("order_code");
                if (orderCode != null && !orderCode.isEmpty()) {
                    return orderCode;
                }
            }
        } catch (Exception e) {
            log.debug("Failed to extract from context", e);
        }

        // Extract from message using regex
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(?:DH|HD|don|đơn|order)[\\s:]*([A-Z0-9]+)",
                java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    /**
     * Format price to Vietnamese format
     */
    private String formatPrice(long price) {
        return String.format("%,d", price).replace(",", ".");
    }

    /**
     * Get order status text
     */
    private String getOrderStatusText(com.example.backendlaptop.model.TrangThaiHoaDon status) {
        if (status == null)
            return "Không xác định";

        switch (status) {
            case CHO_THANH_TOAN:
                return "Chờ thanh toán";
            case DA_THANH_TOAN:
                return "Đã thanh toán";
            case DANG_GIAO:
                return "Đang giao";
            case HOAN_THANH:
                return "Hoàn thành";
            case DA_HUY:
                return "Đã hủy";
            default:
                return status.toString();
        }
    }

    /**
     * Xử lý trường hợp confidence thấp
     */
    private ChatbotResponse handleLowConfidence(ChatSession session, ChatResponse message) {
        int unclearCount = countRecentUnclearMessages(session);

        log.info("❓ [Chatbot] Low confidence, unclear count: {}", unclearCount);

        // Sau 2 tin nhắn không rõ ràng → Escalate to human
        if (unclearCount >= MAX_UNCLEAR_MESSAGES_BEFORE_ESCALATION) {
            escalateToHuman(session, "Không hiểu câu hỏi sau " + unclearCount + " lần");

            return ChatbotResponse.escalateResponse(
                    "Mình chưa hiểu rõ yêu cầu của bạn. 😅\n\n" +
                            "Để được hỗ trợ tốt hơn, mình sẽ kết nối bạn với nhân viên tư vấn nhé! ⏳\n\n" +
                            "Vui lòng đợi trong giây lát...",
                    "Không hiểu sau nhiều lần");
        }

        // Show help menu với quick replies
        List<QuickReplyDTO> mainMenu = getMainMenuQuickReplies();

        return ChatbotResponse.builder()
                .responseText(
                        "Mình có thể giúp bạn về:\n\n" +
                                "🛒 Sản phẩm & Giá cả\n" +
                                "🔧 Bảo hành\n" +
                                "📦 Đơn hàng\n" +
                                "💳 Thanh toán\n" +
                                "🏪 Thông tin cửa hàng\n\n" +
                                "Bạn muốn hỏi về vấn đề gì?")
                .quickReplies(mainMenu)
                .shouldSave(false) // Không lưu help message
                .shouldEscalate(false)
                .build();
    }

    /**
     * Escalate conversation to human
     */
    @Transactional
    public void escalateToHuman(ChatSession session, String reason) {
        log.info("🚨 [Chatbot] Escalating session {} to human. Reason: {}",
                session.getConversationId(), reason);

        session.setIsBotHandling(false);
        session.setIsEscalated(true);
        session.setEscalationReason(reason);
        session.setEscalatedAt(Instant.now());
        sessionRepo.save(session);

        // Send system message
        Chat systemMsg = new Chat();
        systemMsg.setId(UUID.randomUUID());
        systemMsg.setConversationId(session.getConversationId());
        systemMsg.setNoiDung("🔔 Cuộc trò chuyện đã được chuyển đến nhân viên hỗ trợ. Vui lòng đợi trong giây lát...");
        systemMsg.setMessageType("system");
        systemMsg.setIsBotMessage(true);
        systemMsg.setIsFromCustomer(false);
        systemMsg.setNgayPhanHoi(Instant.now());
        chatRepo.save(systemMsg);
    }

    /**
     * Escalate conversation explicitly via API
     */
    @Transactional
    public void escalateConversation(UUID conversationId) {
        sessionRepo.findByConversationId(conversationId)
                .ifPresent(session -> escalateToHuman(session, "User requested support via API"));
    }

    /**
     * Staff takes over conversation from bot
     */
    @Transactional
    public void staffTakeOver(UUID conversationId, UUID nhanVienId) {
        Optional<ChatSession> sessionOpt = sessionRepo.findByConversationId(conversationId);

        if (sessionOpt.isPresent()) {
            ChatSession session = sessionOpt.get();
            session.setIsBotHandling(false);
            session.setIsEscalated(true);
            session.setNhanVienId(nhanVienId);
            session.setEscalationReason("Nhân viên tiếp quản");
            session.setEscalatedAt(Instant.now());
            sessionRepo.save(session);

            log.info("👤 [Chatbot] Staff {} took over conversation {}", nhanVienId, conversationId);

            // Send system message
            Chat systemMsg = new Chat();
            systemMsg.setId(UUID.randomUUID());
            systemMsg.setConversationId(conversationId);
            systemMsg.setNoiDung("👋 Nhân viên đã vào hỗ trợ bạn!");
            systemMsg.setMessageType("system");
            systemMsg.setIsBotMessage(false);
            systemMsg.setIsFromCustomer(false);
            systemMsg.setNgayPhanHoi(Instant.now());
            chatRepo.save(systemMsg);
        }
    }

    /**
     * Handover conversation back to AI Bot
     */
    @Transactional
    public void turnBotBackOn(UUID conversationId) {
        Optional<ChatSession> sessionOpt = sessionRepo.findByConversationId(conversationId);

        if (sessionOpt.isPresent()) {
            ChatSession session = sessionOpt.get();
            session.setIsBotHandling(true);
            session.setEscalationReason(null); // Clear escalation
            sessionRepo.save(session);

            log.info("🤖 [Chatbot] Handed back conversation {} to AI", conversationId);

            // Send system message
            try {
                Chat systemMsg = new Chat();
                systemMsg.setId(UUID.randomUUID());
                systemMsg.setConversationId(conversationId);
                systemMsg.setNoiDung("🤖 Trợ lý ảo đã quay lại hỗ trợ bạn.");
                systemMsg.setMessageType("system");
                systemMsg.setIsBotMessage(true);
                systemMsg.setIsFromCustomer(false);
                systemMsg.setNgayPhanHoi(Instant.now());

                // Fix missing Foreign Key for KhachHang
                if (session.getKhachHangId() != null) {
                    khachHangRepo.findById(session.getKhachHangId())
                            .ifPresent(systemMsg::setKhachHang);
                }

                chatRepo.save(systemMsg);
            } catch (Exception e) {
                log.error("❌ [Chatbot] Error sending system message: {}", e.getMessage());
            }
        }
    }

    /**
     * Check if bot is currently active for this conversation
     */
    public boolean isBotActive(UUID conversationId) {
        return sessionRepo.findByConversationId(conversationId)
                .map(ChatSession::getIsBotHandling)
                .orElse(true); // Default to true if no session
    }

    /**
     * Get or create chat session
     */
    private ChatSession getOrCreateSession(UUID conversationId, UUID khachHangId) {
        return sessionRepo.findByConversationId(conversationId)
                .orElseGet(() -> {
                    ChatSession newSession = new ChatSession();
                    newSession.setId(UUID.randomUUID());
                    newSession.setConversationId(conversationId);
                    newSession.setKhachHangId(khachHangId);
                    newSession.setIsBotHandling(true);
                    newSession.setIsEscalated(false);
                    newSession.setStartedAt(Instant.now());
                    newSession.setLastActivity(Instant.now());

                    log.info("✨ [Chatbot] Created new session for conversation {}", conversationId);
                    return sessionRepo.save(newSession);
                });
    }

    /**
     * Load quick replies for intent
     */
    private List<QuickReplyDTO> loadQuickReplies(String intentCode) {
        return quickReplyRepo.findByIntentCodeAndIsActiveTrueOrderByDisplayOrderAsc(intentCode)
                .stream()
                .map(qr -> QuickReplyDTO.builder()
                        .id(qr.getId())
                        .intentCode(qr.getIntentCode())
                        .replyText(qr.getReplyText())
                        .replyValue(qr.getReplyValue())
                        .replyType(qr.getReplyType())
                        .displayOrder(qr.getDisplayOrder())
                        .icon(qr.getIcon())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Get main menu quick replies (for GREETING or unclear situations)
     */
    private List<QuickReplyDTO> getMainMenuQuickReplies() {
        return loadQuickReplies("GREETING");
    }

    /**
     * Update session context with current intent and extracted data
     */
    private void updateSessionContext(ChatSession session, String intentCode, String message) {
        session.setCurrentIntent(intentCode);

        // Extract context data from message (e.g., product name, order code)
        Map<String, Object> contextData = new LinkedHashMap<>();

        try {
            // Parse existing context if any
            if (session.getContextData() != null && !session.getContextData().isEmpty()) {
                contextData = objectMapper.readValue(
                        session.getContextData(),
                        new TypeReference<Map<String, Object>>() {
                        });
            }
        } catch (Exception e) {
            log.warn("Failed to parse existing context data", e);
        }

        // Extract product name if intent is product-related
        if (intentCode != null && (intentCode.contains("PRODUCT") || intentCode.contains("PRICE"))) {
            // Simple extraction: look for common product patterns
            String normalizedMessage = normalizeVietnamese(message.toLowerCase());
            // This is a simple implementation - can be improved with NLP
            contextData.put("last_intent", intentCode);
            contextData.put("last_message", message);
        }

        // Extract order code if intent is order-related
        if (intentCode != null && intentCode.contains("ORDER")) {
            // Look for order code patterns (e.g., "DH123", "HD123")
            java.util.regex.Pattern orderPattern = java.util.regex.Pattern.compile(
                    "(?:DH|HD|don|đơn|order)[\\s:]*([A-Z0-9]+)",
                    java.util.regex.Pattern.CASE_INSENSITIVE);
            java.util.regex.Matcher matcher = orderPattern.matcher(message);
            if (matcher.find()) {
                contextData.put("order_code", matcher.group(1));
            }
        }

        // Save context
        try {
            session.setContextData(objectMapper.writeValueAsString(contextData));
        } catch (Exception e) {
            log.error("Failed to serialize context data", e);
        }
    }

    /**
     * Get contextual response using session context
     */
    private ChatbotResponse getContextualResponse(String message, ChatSession session) {
        String currentIntent = session.getCurrentIntent();
        String contextDataStr = session.getContextData();

        if (currentIntent == null || contextDataStr == null || contextDataStr.isEmpty()) {
            return null; // No context available
        }

        try {
            Map<String, Object> contextData = objectMapper.readValue(
                    contextDataStr,
                    new TypeReference<Map<String, Object>>() {
                    });

            String normalizedMessage = normalizeVietnamese(message.toLowerCase());

            // Handle follow-up questions based on context
            if (normalizedMessage.matches(".*(bao nhiêu|giá|price|cost|bao nhieu).*")) {
                // Follow-up price question
                if (currentIntent.equals("PRODUCT_INFO") || currentIntent.equals("PRODUCT_SEARCH")) {
                    String lastMessage = (String) contextData.get("last_message");
                    if (lastMessage != null) {
                        // Try to extract product name from last message
                        // This is simplified - in production, use proper NLP
                        log.info("🎯 [Chatbot] Context-aware: Price follow-up for product mentioned in: {}",
                                lastMessage);
                        // Return response with product price query
                        return null; // Will be handled by requires_data logic
                    }
                }
            }

            // Handle order status follow-up
            if (normalizedMessage.matches(".*(trạng thái|status|tình trạng|đã đến|chưa đến).*")) {
                String orderCode = (String) contextData.get("order_code");
                if (orderCode != null) {
                    log.info("🎯 [Chatbot] Context-aware: Status follow-up for order: {}", orderCode);
                    // Will be handled by requires_data logic
                    return null;
                }
            }

        } catch (Exception e) {
            log.error("Failed to parse context data for contextual response", e);
        }

        return null;
    }

    /**
     * Count recent unclear messages in session
     */
    private int countRecentUnclearMessages(ChatSession session) {
        Instant cutoff = Instant.now().minus(10, ChronoUnit.MINUTES);

        Pageable pageable = Pageable.unpaged();
        return (int) chatRepo.findByConversationIdOrderByNgayPhanHoiAsc(session.getConversationId(), pageable)
                .getContent()
                .stream()
                .filter(msg -> msg.getCreatedAt() != null && msg.getCreatedAt().isAfter(cutoff))
                .filter(msg -> Boolean.TRUE.equals(msg.getRequiresHumanReview()))
                .count();
    }

    /**
     * Save analytics
     */
    private void saveAnalytics(ChatResponse message, IntentMatch match, long responseTimeMs) {
        try {
            ChatAnalytics analytics = new ChatAnalytics();
            analytics.setConversationId(message.getConversationId());
            analytics.setMessageId(message.getId());
            analytics.setIntentDetected(match != null ? match.getIntentCode() : null);
            analytics.setConfidenceScore(match != null ? match.getConfidence() : BigDecimal.ZERO);
            analytics.setWasAutoResponded(
                    match != null && match.getConfidence().doubleValue() >= DEFAULT_CONFIDENCE_THRESHOLD);
            analytics.setResponseTimeMs((int) responseTimeMs);
            analytics.setCreatedAt(Instant.now());

            analyticsRepo.save(analytics);
        } catch (Exception e) {
            log.error("❌ [Chatbot] Error saving analytics", e);
        }
    }

    /**
     * Normalize Vietnamese text (remove accents, lowercase)
     */
    private String normalizeVietnamese(String text) {
        if (text == null)
            return "";

        // Remove accents
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        // Replace đ → d
        normalized = normalized.replace("đ", "d").replace("Đ", "d");

        // Remove extra spaces
        normalized = normalized.trim().replaceAll("\\s+", " ");

        return normalized.toLowerCase();
    }
}
