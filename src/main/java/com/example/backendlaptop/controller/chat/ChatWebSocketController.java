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

        ChatResponse savedMessage = null;
        UUID conversationId = request.getConversationId();

        try {
            // 1. Validate và Lưu tin nhắn vào database
            try {
                // Nếu là nhân viên, đảm bảo có ID nhân viên
                if (Boolean.FALSE.equals(request.getIsFromCustomer()) && request.getNhanVienId() == null) {
                    log.error("❌ [WebSocket] Tin nhắn từ nhân viên thiếu ID nhân viên");
                    // Có thể gửi error message lại cho client nếu cần
                    return;
                }

                savedMessage = chatService.sendMessage(request);
                conversationId = savedMessage.getConversationId();

                // Broadcast tin nhắn đã lưu đến conversation topic
                if (conversationId != null) {
                    messagingTemplate.convertAndSend("/topic/conversation/" + conversationId, savedMessage);
                    log.debug("✅ [WebSocket] Đã broadcast tin nhắn đến: {}", conversationId);

                    // Nếu nhân viên gửi -> Take over conversation
                    if (Boolean.FALSE.equals(request.getIsFromCustomer())) {
                        chatbotService.staffTakeOver(conversationId, request.getNhanVienId());
                    }
                }
            } catch (Exception saveError) {
                log.error("❌ [WebSocket] Lỗi khi lưu tin nhắn: {}", saveError.getMessage());
                // Nếu lỗi lưu DB nhưng vẫn muốn gửi tạm (tùy nghiệp vụ), hiện tại code cũ cố
                // gắng xử lý tiếp
                // Nhưng với tin nhắn nhân viên/khách hàng quan trọng, nên fail-fast hoặc báo
                // lỗi
                // Tuy nhiên để giữ behavior cũ cho customer (fallback AI), ta tách luồng

                if (Boolean.FALSE.equals(request.getIsFromCustomer())) {
                    // Nếu là staff mà lỗi lưu DB -> dừng, không gửi ảo
                    return;
                }

                // Nếu là customer -> vẫn tiếp tục để chạy AI
                // Tạo conversationId tạm thời nếu chưa có
                if (conversationId == null) {
                    conversationId = request.getConversationId();
                    if (conversationId == null) {
                        conversationId = UUID.randomUUID();
                    }
                }
                // Build message tạm để AI xử lý
                savedMessage = buildTempCustomerMessage(request, conversationId);
            }

            // 2. Logic xử lý AI Chatbot (Chỉ cho tin nhắn từ khách hàng)
            if (Boolean.TRUE.equals(request.getIsFromCustomer())) {
                processChatbotResponse(request, conversationId, savedMessage != null);
            }

        } catch (Exception e) {
            log.error("❌ [WebSocket] Lỗi không mong muốn: ", e);
        }
    }

    private void processChatbotResponse(ChatRequest request, UUID conversationId, boolean messageSaved) {
        log.info("🤖 [WebSocket] Triggering chatbot for customer message");

        // 0. STOP: Nếu Bot không được active (human đang chat), return ngay
        if (!chatbotService.isBotActive(conversationId)) {
            log.info("🛑 [WebSocket] Bot is disabled for conversation {}, skipping AI response.", conversationId);
            return;
        }

        try {
            ChatbotResponse botResponse = null;

            // Ưu tiên 1: Thử Gemini AI trước (cho tất cả tin nhắn)
            try {
                // Build consultation map
                java.util.Map<String, Object> consultationMap = new java.util.HashMap<>();
                if (request.getConsultationData() != null) {
                    consultationMap.put("purposes", request.getConsultationData().get("purposes"));
                    consultationMap.put("budget", request.getConsultationData().get("budget"));
                    consultationMap.put("features", request.getConsultationData().get("features"));
                    consultationMap.put("userMessage", request.getNoiDung());
                } else {
                    consultationMap.put("purposes", java.util.Collections.emptyList());
                    consultationMap.put("budget", null);
                    consultationMap.put("features", java.util.Collections.emptyList());
                    consultationMap.put("userMessage", request.getNoiDung());
                }

                botResponse = geminiChatService.consultWithGemini(
                        request.getNoiDung(),
                        request.getKhachHangId(),
                        consultationMap);
            } catch (Exception geminiError) {
                log.warn("⚠️ [WebSocket] Gemini AI error: {}", geminiError.getMessage());
            }

            // Fallback: ChatbotService
            if (botResponse == null) {
                try {
                    ChatResponse tempCustomer = buildTempCustomerMessage(request, conversationId);
                    botResponse = chatbotService.processCustomerMessage(tempCustomer);
                } catch (Exception fallbackErr) {
                    log.warn("⚠️ [WebSocket] ChatbotService fallback error: {}", fallbackErr.getMessage());
                }
            }

            // Soft fail message - REMOVED to allow silence (e.g. when escalated)
            // if (botResponse == null) {
            // botResponse = ChatbotResponse.builder()
            // .responseText(
            // "Trợ lý tự động\nHiện trợ lý tự động đang tạm gián đoạn. Bạn có thể thử lại
            // sau ít phút hoặc để lại nội dung, chúng tôi sẽ hỗ trợ ngay.")
            // .intentCode("GROQ_SOFT_FAIL")
            // .confidence(java.math.BigDecimal.valueOf(0.4))
            // .shouldSave(true)
            // .shouldEscalate(false)
            // .build();
            // }

            // Gửi bot response
            if (botResponse != null && botResponse.getResponseText() != null) {
                ChatResponse botMessageResponse = new ChatResponse();
                botMessageResponse.setId(UUID.randomUUID());
                botMessageResponse.setConversationId(conversationId);
                botMessageResponse.setNoiDung(botResponse.getResponseText());
                botMessageResponse.setMessageType("text");
                botMessageResponse.setIsFromCustomer(false);
                botMessageResponse.setIsBotMessage(true);
                botMessageResponse.setBotConfidence(botResponse.getConfidence());
                botMessageResponse.setIntentDetected(botResponse.getIntentCode());
                botMessageResponse.setCreatedAt(java.time.Instant.now());

                // Delay nhỏ để realistic
                Thread.sleep(800);

                // Broadcast bot response
                messagingTemplate.convertAndSend("/topic/conversation/" + conversationId, botMessageResponse);

                // Thử lưu bot message vào database
                if (messageSaved && Boolean.TRUE.equals(botResponse.getShouldSave())) {
                    try {
                        ChatRequest botRequest = new ChatRequest();
                        botRequest.setKhachHangId(request.getKhachHangId());
                        botRequest.setNoiDung(botResponse.getResponseText());
                        botRequest.setConversationId(conversationId);
                        botRequest.setMessageType("text");
                        botRequest.setIsFromCustomer(false);
                        chatService.sendMessage(botRequest);
                    } catch (Exception saveBotError) {
                        log.warn("⚠️ [WebSocket] Không thể lưu bot message: {}", saveBotError.getMessage());
                    }
                }

                // Escalation
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
