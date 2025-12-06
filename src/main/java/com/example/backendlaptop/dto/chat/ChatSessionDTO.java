package com.example.backendlaptop.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatSessionDTO {
    
    private UUID id;
    private UUID conversationId;
    private UUID khachHangId;
    private String khachHangTen;
    private String currentIntent;
    private String contextData;
    private Boolean isBotHandling;
    private Boolean isEscalated;
    private String escalationReason;
    private UUID nhanVienId;
    private String nhanVienTen;
    private Integer botSatisfactionRating;
    private Instant startedAt;
    private Instant escalatedAt;
    private Instant resolvedAt;
    private Instant lastActivity;
}
