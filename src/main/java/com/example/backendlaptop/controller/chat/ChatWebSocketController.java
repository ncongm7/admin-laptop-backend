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
    private final SimpMessagingTemplate messagingTemplate;
    private final com.example.backendlaptop.service.chat.RateLimitService rateLimitService;

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
        
        try {
            // Rate limiting is handled in ChatService.sendMessage()
            // 1. Lưu tin nhắn khách hàng vào database
            ChatResponse customerMessage = chatService.sendMessage(request);
            
            // 2. Broadcast tin nhắn khách hàng đến conversation
            UUID conversationId = customerMessage.getConversationId();
            if (conversationId != null) {
                messagingTemplate.convertAndSend("/topic/conversation/" + conversationId, customerMessage);
                log.debug("✅ [WebSocket] Đã gửi customer message đến conversation: {}", conversationId);
            }
            
            // 3. Nếu tin nhắn từ khách hàng → Trigger chatbot
            if (Boolean.TRUE.equals(request.getIsFromCustomer())) {
                log.info("🤖 [WebSocket] Triggering chatbot for customer message");
                
                try {
                    ChatbotResponse botResponse = chatbotService.processCustomerMessage(customerMessage);
                    
                    if (botResponse != null && Boolean.TRUE.equals(botResponse.getShouldSave())) {
                        // Tạo tin nhắn bot
                        ChatRequest botRequest = new ChatRequest();
                        botRequest.setKhachHangId(request.getKhachHangId());
                        botRequest.setNhanVienId(null); // Bot không phải nhân viên
                        botRequest.setNoiDung(botResponse.getResponseText());
                        botRequest.setConversationId(conversationId);
                        botRequest.setMessageType("text");
                        botRequest.setIsFromCustomer(false);
                        
                        // Lưu tin nhắn bot (với delay nhỏ để realistic)
                        Thread.sleep(800); // Simulate typing delay
                        
                        ChatResponse botMessageResponse = chatService.sendMessage(botRequest);
                        
                        // Mark as bot message
                        botMessageResponse.setIsBotMessage(true);
                        botMessageResponse.setBotConfidence(botResponse.getConfidence());
                        botMessageResponse.setIntentDetected(botResponse.getIntentCode());
                        
                        // Add quick replies
                        if (botResponse.getQuickReplies() != null && !botResponse.getQuickReplies().isEmpty()) {
                            botMessageResponse.setQuickReplies(botResponse.getQuickReplies());
                        }
                        
                        // Broadcast bot response
                        messagingTemplate.convertAndSend("/topic/conversation/" + conversationId, botMessageResponse);
                        log.info("🤖 [WebSocket] Sent bot response with intent: {}", botResponse.getIntentCode());
                        
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
            throw new RuntimeException("Lỗi khi gửi tin nhắn", e);
        }
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

