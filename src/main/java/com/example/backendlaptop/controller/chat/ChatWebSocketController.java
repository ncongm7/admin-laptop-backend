package com.example.backendlaptop.controller.chat;

import com.example.backendlaptop.dto.chat.ChatRequest;
import com.example.backendlaptop.dto.chat.ChatResponse;
import com.example.backendlaptop.dto.chat.ChatbotResponse;
import com.example.backendlaptop.service.chat.ChatService;
import com.example.backendlaptop.service.chat.ChatbotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.UUID;

/**
 * WebSocket Controller cho real-time chat với AI Chatbot
 * Sử dụng STOMP protocol
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatService chatService;
    private final ChatbotService chatbotService;
    private final com.example.backendlaptop.service.chat.GeminiChatService geminiChatService;
    private final SimpMessagingTemplate messagingTemplate;
    private final com.example.backendlaptop.repository.ChatSessionRepository chatSessionRepo;

    /**
     * Xử lý tin nhắn mới từ client với AI Chatbot integration
     * Client gửi đến: /app/chat.send
     * Server broadcast đến: /topic/conversation/{conversationId}
     * 
     * LƯU Ý: KHÔNG dùng @SendTo vì sẽ gửi duplicate. Chỉ dùng messagingTemplate.
     */
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatRequest request) {
        log.info("📨 [WebSocket] Nhận tin nhắn: {}", request);

        ChatResponse customerMessage = null;
        UUID conversationId = null;
        boolean messageSaved = false;

        try {
            // 1. Thử lưu tin nhắn vào database (có thể fail nếu khách hàng chưa tồn tại)
            try {
                customerMessage = chatService.sendMessage(request);
                conversationId = customerMessage.getConversationId();
                messageSaved = true;

                // Broadcast tin nhắn (cả customer và staff) đến conversation
                if (conversationId != null) {
                    String topic = "/topic/conversation/" + conversationId;
                    messagingTemplate.convertAndSend(topic, customerMessage);
                    String messageType = Boolean.TRUE.equals(request.getIsFromCustomer()) ? "customer" : "staff";
                    log.info("✅ [WebSocket] Broadcasted {} message to topic: {}, messageId: {}, content: {}",
                            messageType, topic, customerMessage.getId(),
                            customerMessage.getNoiDung() != null 
                                ? customerMessage.getNoiDung().substring(0, Math.min(50, customerMessage.getNoiDung().length()))
                                : "null");
                } else {
                    log.warn(" [WebSocket] Cannot broadcast message: conversationId is null");
                }
            } catch (Exception saveError) {
                log.warn("[WebSocket] Không thể lưu tin nhắn (có thể khách hàng chưa đăng ký): {}",
                        saveError.getMessage());
                // Vẫn tiếp tục xử lý để Gemini có thể trả lời
                // Tạo conversationId tạm thời nếu chưa có
                conversationId = request.getConversationId();
                if (conversationId == null) {
                    conversationId = UUID.randomUUID();
                }
            }

            // 2. Nếu tin nhắn từ khách hàng → Ưu tiên Gemini, fallback về ChatbotService
            if (Boolean.TRUE.equals(request.getIsFromCustomer())) {
                // Check if escalated to human (with error handling for missing database columns)
                boolean isEscalated = false;
                if (conversationId != null) {
                    try {
                        java.util.Optional<com.example.backendlaptop.entity.ChatSession> sessionOpt = chatSessionRepo.findByConversationId(conversationId);
                        if (sessionOpt.isPresent() && Boolean.TRUE.equals(sessionOpt.get().getIsEscalated())) {
                            isEscalated = true;
                            log.info(" [WebSocket] Conversation {} is escalated to human. Chatbot response skipped.", conversationId);
                        }
                    } catch (Exception dbError) {
                        // Database schema might be missing columns (e.g., current_state)
                        // Log warning but continue - bot should still respond
                        log.warn(" [WebSocket] Error checking escalation status (database schema might need update): {}. Continuing with bot response.", dbError.getMessage());
                        // Continue - don't block bot response due to schema issues
                    }
                }
                
                if (isEscalated) {
                    return;
                }
                
                log.info("🤖 [WebSocket] Triggering chatbot for customer message: {}", request.getNoiDung());

                try {
                    ChatbotResponse botResponse = null;

                    // Ưu tiên 1: Thử Gemini AI trước (cho tất cả tin nhắn)
                    try {
                        log.info("[WebSocket] Attempting Gemini AI response");

                        // Build consultation map từ tin nhắn thông thường
                        java.util.Map<String, Object> consultationMap = new java.util.HashMap<>();
                        if (request.getConsultationData() != null) {
                            // Có consultation data → dùng trực tiếp
                            consultationMap.put("purposes", request.getConsultationData().get("purposes"));
                            consultationMap.put("budget", request.getConsultationData().get("budget"));
                            consultationMap.put("features", request.getConsultationData().get("features"));
                            consultationMap.put("userMessage", request.getNoiDung());
                        } else {
                            // Tin nhắn thông thường → để Gemini tự phân tích
                            consultationMap.put("purposes", java.util.Collections.emptyList());
                            consultationMap.put("budget", null);
                            consultationMap.put("features", java.util.Collections.emptyList());
                            consultationMap.put("userMessage", request.getNoiDung());
                        }

                        botResponse = geminiChatService.consultWithGemini(
                                request.getNoiDung(),
                                request.getKhachHangId(),
                                conversationId,
                                consultationMap);

                        if (botResponse != null) {
                            log.info(" [WebSocket] Gemini AI responded successfully");
                        } else {
                            log.warn(" [WebSocket] Gemini returned null, falling back to ChatbotService");
                        }
                    } catch (Exception geminiError) {
                        log.warn(" [WebSocket] Gemini AI error: {}, falling back to ChatbotService",
                                geminiError.getMessage());
                        botResponse = null;
                    }

                    // Fallback: nếu Groq fail → dùng ChatbotService (không menu), nếu vẫn fail thì
                    // báo mềm
                    if (botResponse == null) {
                        log.warn(" [WebSocket] Groq null → fallback ChatbotService");
                        try {
                            ChatResponse tempCustomer = buildTempCustomerMessage(request, conversationId);
                            botResponse = chatbotService.processCustomerMessage(tempCustomer);
                        } catch (Exception fallbackErr) {
                            log.warn(" [WebSocket] ChatbotService fallback error: {}", fallbackErr.getMessage());
                        }
                    }
                    if (botResponse == null) {
                        log.warn(
                                " [WebSocket] Both Groq and ChatbotService failed, using guaranteed fallback with menu");

                        // GUARANTEED FALLBACK - Never fails
                        java.util.List<com.example.backendlaptop.dto.chat.QuickReplyDTO> fallbackMenu = java.util.Arrays
                                .asList(
                                        com.example.backendlaptop.dto.chat.QuickReplyDTO.builder()
                                                .replyText("🛒 Tư vấn chọn laptop")
                                                .replyValue("Tư vấn chọn laptop phù hợp với nhu cầu của tôi")
                                                .replyType("intent_trigger")
                                                .icon("bi bi-laptop")
                                                .build(),
                                        com.example.backendlaptop.dto.chat.QuickReplyDTO.builder()
                                                .replyText("💰 Xem bảng giá")
                                                .replyValue("Cho tôi xem bảng giá laptop")
                                                .replyType("intent_trigger")
                                                .icon("bi bi-cash")
                                                .build(),
                                        com.example.backendlaptop.dto.chat.QuickReplyDTO.builder()
                                                .replyText("🛡️ Chính sách bảo hành")
                                                .replyValue("Thông tin về chính sách bảo hành")
                                                .replyType("intent_trigger")
                                                .icon("bi bi-shield-check")
                                                .build(),
                                        com.example.backendlaptop.dto.chat.QuickReplyDTO.builder()
                                                .replyText("👤 Kết nối nhân viên")
                                                .replyValue("Tôi muốn nói chuyện với nhân viên")
                                                .replyType("escalate")
                                                .icon("bi bi-person-headset")
                                                .build());

                        botResponse = ChatbotResponse.builder()
                                .responseText("Xin lỗi vì sự bất tiện! 😊\n\n" +
                                        "Hệ thống AI đang tạm thời quá tải, nhưng mình vẫn sẵn sàng hỗ trợ bạn.\n\n" +
                                        "Bạn quan tâm đến vấn đề gì?")
                                .intentCode("GUARANTEED_FALLBACK")
                                .confidence(java.math.BigDecimal.valueOf(1.0))
                                .shouldSave(false)
                                .shouldEscalate(false)
                                .quickReplies(fallbackMenu)
                                .build();
                    }

                    // Gửi bot response (nếu có) - ĐẢM BẢO LUÔN CÓ RESPONSE
                    if (botResponse != null && botResponse.getResponseText() != null) {
                        // Tạo ChatResponse cho bot message
                        ChatResponse botMessageResponse = new ChatResponse();
                        botMessageResponse.setId(UUID.randomUUID());
                        botMessageResponse.setConversationId(conversationId);
                        botMessageResponse.setNoiDung(botResponse.getResponseText());
                        botMessageResponse.setMessageType("text");
                        botMessageResponse.setIsFromCustomer(false);
                        botMessageResponse.setIsBotMessage(true); // QUAN TRỌNG: Đánh dấu là bot message
                        botMessageResponse.setBotConfidence(botResponse.getConfidence());
                        botMessageResponse.setIntentDetected(botResponse.getIntentCode());
                        botMessageResponse.setNgayPhanHoi(java.time.Instant.now());
                        botMessageResponse.setCreatedAt(java.time.Instant.now());

                        // Attach quick replies if available
                        if (botResponse.getQuickReplies() != null && !botResponse.getQuickReplies().isEmpty()) {
                            botMessageResponse.setQuickReplies(botResponse.getQuickReplies());
                            log.info("📋 [WebSocket] Bot response includes {} quick replies", botResponse.getQuickReplies().size());
                        }

                        // Delay nhỏ để realistic
                        try {
                            Thread.sleep(800);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }

                        // Broadcast bot response
                        String topic = "/topic/conversation/" + conversationId;
                        messagingTemplate.convertAndSend(topic, botMessageResponse);
                        log.info("🤖 [WebSocket] ✅ Sent bot response to topic: {}, intent: {}, isBotMessage: true, message: {}",
                                topic, botResponse.getIntentCode(),
                                botMessageResponse.getNoiDung() != null ? botMessageResponse.getNoiDung().substring(0,
                                        Math.min(50, botMessageResponse.getNoiDung().length())) : "null");

                        // ✅ QUAN TRỌNG: Luôn lưu bot message vào database để có lịch sử
                        if (messageSaved) {
                            try {
                                ChatRequest botRequest = new ChatRequest();
                                botRequest.setKhachHangId(request.getKhachHangId());
                                botRequest.setNhanVienId(null);
                                botRequest.setNoiDung(botResponse.getResponseText());
                                botRequest.setConversationId(conversationId);
                                botRequest.setMessageType("text");
                                botRequest.setIsFromCustomer(false);
                                botRequest.setIsBotMessage(true); // QUAN TRỌNG: Đánh dấu là bot message
                                botRequest.setBotConfidence(botResponse.getConfidence()); // Lưu confidence
                                botRequest.setIntentDetected(botResponse.getIntentCode()); // Lưu intent
                                ChatResponse savedBotMessage = chatService.sendMessage(botRequest);
                                
                                // Update botMessageResponse với ID thật từ database
                                if (savedBotMessage != null && savedBotMessage.getId() != null) {
                                    botMessageResponse.setId(savedBotMessage.getId());
                                    log.info("💾 [WebSocket] Bot message saved to database with ID: {}, intent: {}", savedBotMessage.getId(), botResponse.getIntentCode());
                                }
                            } catch (Exception saveBotError) {
                                log.warn("⚠️ [WebSocket] Không thể lưu bot message vào database: {}. Message đã được broadcast nhưng không có trong lịch sử.", saveBotError.getMessage());
                            }
                        } else {
                            log.warn("⚠️ [WebSocket] Không thể lưu bot message vì customer message chưa được lưu (conversationId: {})", conversationId);
                        }
                    } else {
                        log.error("❌ [WebSocket] Bot response is NULL or empty! This should not happen due to guaranteed fallback.");

                        // Nếu cần escalate, gửi notification
                        if (Boolean.TRUE.equals(botResponse.getShouldEscalate())) {
                            messagingTemplate.convertAndSend("/topic/admin/escalations",
                                    java.util.Map.of(
                                            "conversationId", conversationId,
                                            "reason", botResponse.getEscalationReason(),
                                            "timestamp", java.time.Instant.now()));
                        }
                    }
                } catch (Exception botError) {
                    log.error("❌ [WebSocket] Lỗi khi xử lý chatbot: ", botError);
                    
                    // GUARANTEED FALLBACK: Ngay cả khi có lỗi, vẫn gửi response
                    try {
                        log.warn("🔄 [WebSocket] Using guaranteed fallback due to error");
                        java.util.List<com.example.backendlaptop.dto.chat.QuickReplyDTO> fallbackMenu = java.util.Arrays.asList(
                                com.example.backendlaptop.dto.chat.QuickReplyDTO.builder()
                                        .replyText("🛒 Tư vấn chọn laptop")
                                        .replyValue("Tư vấn chọn laptop phù hợp với nhu cầu của tôi")
                                        .replyType("intent_trigger")
                                        .icon("bi bi-laptop")
                                        .build(),
                                com.example.backendlaptop.dto.chat.QuickReplyDTO.builder()
                                        .replyText("💰 Xem bảng giá")
                                        .replyValue("Cho tôi xem bảng giá laptop")
                                        .replyType("intent_trigger")
                                        .icon("bi bi-cash")
                                        .build(),
                                com.example.backendlaptop.dto.chat.QuickReplyDTO.builder()
                                        .replyText("🛡️ Chính sách bảo hành")
                                        .replyValue("Thông tin về chính sách bảo hành")
                                        .replyType("intent_trigger")
                                        .icon("bi bi-shield-check")
                                        .build(),
                                com.example.backendlaptop.dto.chat.QuickReplyDTO.builder()
                                        .replyText("👤 Kết nối nhân viên")
                                        .replyValue("Tôi muốn nói chuyện với nhân viên")
                                        .replyType("escalate")
                                        .icon("bi bi-person-headset")
                                        .build());

                        ChatResponse fallbackResponse = new ChatResponse();
                        fallbackResponse.setId(UUID.randomUUID());
                        fallbackResponse.setConversationId(conversationId);
                        fallbackResponse.setNoiDung("Xin chào! Mình là trợ lý AI của Dell Store. Mình có thể giúp gì cho bạn?\n\n" +
                                "Bạn có thể chọn một trong các tùy chọn bên dưới hoặc gửi câu hỏi trực tiếp cho mình.");
                        fallbackResponse.setMessageType("text");
                        fallbackResponse.setIsFromCustomer(false);
                        fallbackResponse.setIsBotMessage(true);
                        fallbackResponse.setBotConfidence(java.math.BigDecimal.valueOf(1.0));
                        fallbackResponse.setIntentDetected("GUARANTEED_FALLBACK");
                        fallbackResponse.setNgayPhanHoi(java.time.Instant.now());
                        fallbackResponse.setCreatedAt(java.time.Instant.now());
                        fallbackResponse.setQuickReplies(fallbackMenu);

                        String topic = "/topic/conversation/" + conversationId;
                        messagingTemplate.convertAndSend(topic, fallbackResponse);
                        log.info("🤖 [WebSocket] ✅ Sent guaranteed fallback response to topic: {}", topic);
                        
                        // ✅ Lưu guaranteed fallback message vào database
                        if (messageSaved) {
                            try {
                                ChatRequest fallbackRequest = new ChatRequest();
                                fallbackRequest.setKhachHangId(request.getKhachHangId());
                                fallbackRequest.setNhanVienId(null);
                                fallbackRequest.setNoiDung(fallbackResponse.getNoiDung());
                                fallbackRequest.setConversationId(conversationId);
                                fallbackRequest.setMessageType("text");
                                fallbackRequest.setIsFromCustomer(false);
                                fallbackRequest.setIsBotMessage(true);
                                fallbackRequest.setBotConfidence(java.math.BigDecimal.valueOf(1.0));
                                fallbackRequest.setIntentDetected("GUARANTEED_FALLBACK");
                                ChatResponse savedFallback = chatService.sendMessage(fallbackRequest);
                                if (savedFallback != null && savedFallback.getId() != null) {
                                    fallbackResponse.setId(savedFallback.getId());
                                    log.info("💾 [WebSocket] Guaranteed fallback message saved to database with ID: {}", savedFallback.getId());
                                }
                            } catch (Exception saveFallbackError) {
                                log.warn("⚠️ [WebSocket] Không thể lưu guaranteed fallback message: {}", saveFallbackError.getMessage());
                            }
                        }
                    } catch (Exception fallbackError) {
                        log.error("❌ [WebSocket] Even guaranteed fallback failed: ", fallbackError);
                    }
                }
            } else {
                // 3. Tin nhắn từ nhân viên → KHÔNG trigger chatbot, chỉ save và broadcast
                log.info("👤 [WebSocket] Staff message, no chatbot involved");

                // Staff message đã được lưu và broadcast ở bước 1
                // Đảm bảo message đã được broadcast (nếu chưa thì broadcast lại)
                if (conversationId != null && customerMessage != null) {
                    String topic = "/topic/conversation/" + conversationId;
                    // Broadcast lại để đảm bảo customer nhận được (nếu chưa nhận ở bước 1)
                    messagingTemplate.convertAndSend(topic, customerMessage);
                    log.info("✅ [WebSocket] Re-broadcasted staff message to topic: {}, messageId: {}, content: {}",
                            topic, customerMessage.getId(),
                            customerMessage.getNoiDung() != null
                                    ? customerMessage.getNoiDung().substring(0, Math.min(50, customerMessage.getNoiDung().length()))
                                    : "null");
                } else {
                    log.warn("⚠️ [WebSocket] Staff message not broadcasted: conversationId={}, customerMessage={}",
                            conversationId, customerMessage != null ? "exists" : "null");
                }
            }

        } catch (Exception e) {
            log.error("❌ [WebSocket] Lỗi khi xử lý tin nhắn: ", e);
            // Không throw exception để không làm crash WebSocket connection
            // Chỉ log lỗi và gửi error message đến client nếu cần
        }
    }

    /**
     * Xây dựng ChatResponse tạm cho fallback ChatbotService khi không lưu được DB
     */
    private ChatResponse buildTempCustomerMessage(ChatRequest request, UUID conversationId) {
        ChatResponse temp = new ChatResponse();
        temp.setId(UUID.randomUUID());
        temp.setConversationId(conversationId);
        temp.setKhachHangId(request.getKhachHangId());
        temp.setNoiDung(request.getNoiDung());
        temp.setIsFromCustomer(true);
        temp.setNgayPhanHoi(java.time.Instant.now());
        temp.setMessageType(request.getMessageType());
        return temp;
    }

    /**
     * Xử lý typing indicator
     * Client gửi đến: /app/chat.typing
     */
    @MessageMapping("/chat.typing")
    public void handleTyping(@Payload TypingMessage message) {
        log.debug("⌨️ Typing từ user: {}", message.getUserId());

        // Broadcast typing indicator đến conversation
        messagingTemplate.convertAndSend(
                "/topic/conversation/" + message.getConversationId() + "/typing",
                message);
    }

    /**
     * Xử lý mark as read
     * Client gửi đến: /app/chat.read
     */
    @MessageMapping("/chat.read")
    public void handleRead(@Payload ReadMessage message) {
        log.debug("✅ Mark as read: conversationId={}, isFromCustomer={}",
                message.getConversationId(), message.getIsFromCustomer());

        chatService.markAsRead(message.getConversationId(), message.getIsFromCustomer());

        // Broadcast read status đến conversation
        messagingTemplate.convertAndSend(
                "/topic/conversation/" + message.getConversationId() + "/read",
                message);
    }

    // Inner classes cho typing và read messages
    @lombok.Data
    public static class TypingMessage {
        private UUID conversationId;
        private UUID userId;
        private String userName;
        private Boolean isTyping;
    }

    @lombok.Data
    public static class ReadMessage {
        private UUID conversationId;
        private Boolean isFromCustomer;
    }
}
