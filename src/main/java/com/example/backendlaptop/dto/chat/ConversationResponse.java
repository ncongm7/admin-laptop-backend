package com.example.backendlaptop.dto.chat;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class ConversationResponse {
    private UUID conversationId;
    private UUID khachHangId;
    private String khachHangTen;
    private String khachHangAvatar;
    private String khachHangMa;
    private UUID nhanVienId;
    private String nhanVienTen;
    private String nhanVienAvatar;
    private ChatResponse lastMessage; // Tin nhắn cuối cùng
    private Long unreadCount; // Số tin nhắn chưa đọc
    private Instant lastMessageTime;
    private Boolean isOnline; // Trạng thái online (có thể implement sau)
}

