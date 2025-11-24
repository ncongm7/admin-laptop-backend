package com.example.backendlaptop.dto.chat;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class ChatResponse {
    private UUID id;
    private UUID khachHangId;
    private String khachHangTen;
    private String khachHangAvatar;
    private UUID nhanVienId;
    private String nhanVienTen;
    private String nhanVienAvatar;
    private String noiDung;
    private Instant ngayPhanHoi;
    private Boolean isFromCustomer;
    private Boolean isRead;
    private UUID conversationId;
    private String messageType;
    private String fileUrl;
    private UUID replyToId;
    private ChatResponse replyTo; // Thông tin tin nhắn được reply
    private Instant createdAt;
    private Instant updatedAt;
}

