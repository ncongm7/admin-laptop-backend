package com.example.backendlaptop.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "chat_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatSession {
    
    @Id
    private UUID id;
    
    @Column(name = "conversation_id", nullable = false)
    private UUID conversationId;
    
    @Column(name = "khach_hang_id")
    private UUID khachHangId;
    
    @Column(name = "current_intent", length = 50)
    private String currentIntent;
    
    @Column(name = "context_data", columnDefinition = "NVARCHAR(MAX)")
    private String contextData; // JSON
    
    @Column(name = "is_bot_handling")
    private Boolean isBotHandling;
    
    @Column(name = "is_escalated")
    private Boolean isEscalated;
    
    @Column(name = "escalation_reason", length = 200)
    private String escalationReason;
    
    @Column(name = "nhan_vien_id")
    private UUID nhanVienId;
    
    @Column(name = "bot_satisfaction_rating")
    private Integer botSatisfactionRating;
    
    @Column(name = "started_at")
    private Instant startedAt;
    
    @Column(name = "escalated_at")
    private Instant escalatedAt;
    
    @Column(name = "resolved_at")
    private Instant resolvedAt;
    
    @Column(name = "last_activity")
    private Instant lastActivity;
    
    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        startedAt = Instant.now();
        lastActivity = Instant.now();
        if (isBotHandling == null) {
            isBotHandling = true;
        }
        if (isEscalated == null) {
            isEscalated = false;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        lastActivity = Instant.now();
    }
}
