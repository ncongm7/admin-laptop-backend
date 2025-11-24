package com.example.backendlaptop.dto.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class ChatRequest {
    @NotNull(message = "ID khách hàng không được để trống")
    private UUID khachHangId;

    private UUID nhanVienId; // Có thể null nếu là tin nhắn từ khách hàng

    @NotBlank(message = "Nội dung tin nhắn không được để trống")
    private String noiDung;

    private UUID conversationId; // ID cuộc hội thoại (null nếu là tin nhắn đầu tiên)

    private String messageType = "text"; // text, image, file, system

    private String fileUrl; // URL file/ảnh nếu có

    private UUID replyToId; // ID tin nhắn được reply (nếu có)

    @NotNull(message = "isFromCustomer không được để trống")
    private Boolean isFromCustomer; // true = từ khách hàng, false = từ nhân viên
}

