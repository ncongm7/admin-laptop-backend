package com.example.backendlaptop.controller.chat;

import com.example.backendlaptop.dto.chat.ChatRequest;
import com.example.backendlaptop.dto.chat.ChatResponse;
import com.example.backendlaptop.service.chat.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.UUID;

/**
 * WebSocket Controller cho real-time chat
 * Sử dụng STOMP protocol
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Xử lý tin nhắn mới từ client
     * Client gửi đến: /app/chat.send
     * Server broadcast đến: /topic/conversation/{conversationId}
     * 
     * LƯU Ý: KHÔNG dùng @SendTo vì sẽ gửi duplicate. Chỉ dùng messagingTemplate.
     */
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatRequest request) {
        log.info("📨 Nhận tin nhắn từ WebSocket: {}", request);
        
        try {
            // Lưu tin nhắn vào database
            ChatResponse response = chatService.sendMessage(request);
            
            // CHỈ gửi tin nhắn đến conversation cụ thể (KHÔNG dùng @SendTo để tránh duplicate)
            UUID conversationId = response.getConversationId();
            if (conversationId != null) {
                messagingTemplate.convertAndSend("/topic/conversation/" + conversationId, response);
                log.debug("✅ Đã gửi message đến conversation: {}", conversationId);
            }
            
            // Gửi notification đến user cụ thể (nếu cần) - chỉ notification, không phải message
            // Comment out để tránh duplicate, chỉ dùng conversation topic
            /*
            if (response.getIsFromCustomer()) {
                // Gửi đến nhân viên
                if (response.getNhanVienId() != null) {
                    messagingTemplate.convertAndSendToUser(
                        response.getNhanVienId().toString(),
                        "/queue/notifications",
                        response
                    );
                }
            } else {
                // Gửi đến khách hàng
                if (response.getKhachHangId() != null) {
                    messagingTemplate.convertAndSendToUser(
                        response.getKhachHangId().toString(),
                        "/queue/notifications",
                        response
                    );
                }
            }
            */
        } catch (Exception e) {
            log.error("❌ Lỗi khi xử lý tin nhắn WebSocket: ", e);
            throw e;
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

