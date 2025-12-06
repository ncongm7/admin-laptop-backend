package com.example.backendlaptop.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "chat_analytics")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatAnalytics {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "conversation_id")
    private UUID conversationId;
    
    @Column(name = "message_id")
    private UUID messageId;
    
    @Column(name = "intent_detected", length = 50)
    private String intentDetected;
    
    @Column(name = "confidence_score", precision = 3, scale = 2)
    private BigDecimal confidenceScore;
    
    @Column(name = "was_auto_responded")
    private Boolean wasAutoResponded;
    
    @Column(name = "was_helpful")
    private Boolean wasHelpful;
    
    @Column(name = "response_time_ms")
    private Integer responseTimeMs;
    
    @Column(name = "created_at")
    private Instant createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        if (wasAutoResponded == null) {
            wasAutoResponded = false;
        }
    }
}
