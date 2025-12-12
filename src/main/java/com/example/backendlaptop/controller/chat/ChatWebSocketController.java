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
        
        ChatResponse customerMessage = null;
        UUID conversationId = null;
        boolean messageSaved = false;
        
        try {
            // 1. Thử lưu tin nhắn vào database (có thể fail nếu khách hàng chưa tồn tại)
            try {
                customerMessage = chatService.sendMessage(request);
                conversationId = customerMessage.getConversationId();
                messageSaved = true;
                
                // Broadcast tin nhắn khách hàng đến conversation
                if (conversationId != null) {
                    messagingTemplate.convertAndSend("/topic/conversation/" + conversationId, customerMessage);
                    log.debug("✅ [WebSocket] Đã gửi customer message đến conversation: {}", conversationId);
                }
            } catch (Exception saveError) {
                log.warn("⚠️ [WebSocket] Không thể lưu tin nhắn (có thể khách hàng chưa đăng ký): {}", saveError.getMessage());
                // Vẫn tiếp tục xử lý để Gemini có thể trả lời
                // Tạo conversationId tạm thời nếu chưa có
                conversationId = request.getConversationId();
                if (conversationId == null) {
                    conversationId = UUID.randomUUID();
                }
            }
            
            // 2. Nếu tin nhắn từ khách hàng → Ưu tiên Gemini, fallback về ChatbotService
            if (Boolean.TRUE.equals(request.getIsFromCustomer())) {
                log.info("🤖 [WebSocket] Triggering chatbot for customer message");
                
                try {
                    ChatbotResponse botResponse = null;
                    
                    // Ưu tiên 1: Thử Gemini AI trước (cho tất cả tin nhắn)
                    try {
                        log.info("✨ [WebSocket] Attempting Gemini AI response");
                        
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
                                consultationMap
                        );
                        
                        if (botResponse != null) {
                            log.info("✅ [WebSocket] Gemini AI responded successfully");
                        } else {
                            log.warn("⚠️ [WebSocket] Gemini returned null, falling back to ChatbotService");
                        }
                    } catch (Exception geminiError) {
                        log.warn("⚠️ [WebSocket] Gemini AI error: {}, falling back to ChatbotService", geminiError.getMessage());
                        botResponse = null;
                    }
                    
                    // Fallback: nếu Groq fail → dùng ChatbotService (không menu), nếu vẫn fail thì báo mềm
                    if (botResponse == null) {
                        log.warn("🔄 [WebSocket] Groq null → fallback ChatbotService");
                        try {
                            ChatResponse tempCustomer = buildTempCustomerMessage(request, conversationId);
                            botResponse = chatbotService.processCustomerMessage(tempCustomer);
                        } catch (Exception fallbackErr) {
                            log.warn("⚠️ [WebSocket] ChatbotService fallback error: {}", fallbackErr.getMessage());
                        }
                    }
                    if (botResponse == null) {
                        log.warn("🔄 [WebSocket] Both Groq and ChatbotService failed, sending soft fail message (no menu)");
                        botResponse = ChatbotResponse.builder()
                                .responseText("Hiện trợ lý tự động đang tạm gián đoạn. Bạn có thể thử lại sau ít phút hoặc để lại nội dung, chúng tôi sẽ hỗ trợ ngay.")
                                .intentCode("GROQ_SOFT_FAIL")
                                .confidence(java.math.BigDecimal.valueOf(0.4))
                                .shouldSave(true)
                                .shouldEscalate(false)
                                .build();
                    }
                    
                    // Gửi bot response (nếu có)
                    if (botResponse != null && botResponse.getResponseText() != null) {
                        // Tạo ChatResponse cho bot message
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
                        
                        // Không đính kèm quick replies để tránh menu cứng
                        
                        // Delay nhỏ để realistic
                        Thread.sleep(800);
                        
                        // Broadcast bot response
                        String topic = "/topic/conversation/" + conversationId;
                        messagingTemplate.convertAndSend(topic, botMessageResponse);
                        log.info("🤖 [WebSocket] Sent bot response to topic: {}, intent: {}, message: {}", 
                                topic, botResponse.getIntentCode(), 
                                botMessageResponse.getNoiDung() != null ? 
                                    botMessageResponse.getNoiDung().substring(0, Math.min(50, botMessageResponse.getNoiDung().length())) : "null");
                        
                        // Thử lưu bot message vào database (nếu có thể)
                        if (messageSaved && Boolean.TRUE.equals(botResponse.getShouldSave())) {
                            try {
                                ChatRequest botRequest = new ChatRequest();
                                botRequest.setKhachHangId(request.getKhachHangId());
                                botRequest.setNhanVienId(null);
                                botRequest.setNoiDung(botResponse.getResponseText());
                                botRequest.setConversationId(conversationId);
                                botRequest.setMessageType("text");
                                botRequest.setIsFromCustomer(false);
                                chatService.sendMessage(botRequest);
                            } catch (Exception saveBotError) {
                                log.warn("⚠️ [WebSocket] Không thể lưu bot message: {}", saveBotError.getMessage());
                            }
                        }
                        
                        // Nếu cần escalate, gửi notification
                        if (Boolean.TRUE.equals(botResponse.getShouldEscalate())) {
                            messagingTemplate.convertAndSend("/topic/admin/escalations", 
                                java.util.Map.of(
                                    "conversationId", conversationId,
                                    "reason", botResponse.getEscalationReason(),
                                    "timestamp", java.time.Instant.now()
                                )
                            );
                        }
                    }
                } catch (Exception botError) {
                    log.error("❌ [WebSocket] Lỗi khi xử lý chatbot: ", botError);
                    // Không throw, để khách vẫn nhận được tin nhắn của họ
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
            message
        );
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
            message
        );
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

