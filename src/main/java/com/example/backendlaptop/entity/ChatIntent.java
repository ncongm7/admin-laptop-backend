package com.example.backendlaptop.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "chat_intents")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatIntent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "intent_code", unique = true, nullable = false, length = 50)
    private String intentCode;
    
    @Column(name = "intent_name", nullable = false, length = 100)
    private String intentName;
    
    @Column(name = "category", nullable = false, length = 50)
    private String category;
    
    @Column(name = "keywords", columnDefinition = "NVARCHAR(MAX)")
    private String keywords; // JSON array
    
    @Column(name = "sample_questions", columnDefinition = "NVARCHAR(MAX)")
    private String sampleQuestions; // JSON array
    
    @Column(name = "auto_response_template", columnDefinition = "NVARCHAR(MAX)")
    private String autoResponseTemplate;
    
    @Column(name = "confidence_threshold", precision = 3, scale = 2)
    private BigDecimal confidenceThreshold;
    
    @Column(name = "requires_data")
    private Boolean requiresData;
    
    @Column(name = "data_source", length = 100)
    private String dataSource;
    
    @Column(name = "is_active")
    private Boolean isActive;
    
    @Column(name = "priority")
    private Integer priority;
    
    @Column(name = "created_at")
    private Instant createdAt;
    
    @Column(name = "updated_at")
    private Instant updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        if (isActive == null) {
            isActive = true;
        }
        if (requiresData == null) {
            requiresData = false;
        }
        if (priority == null) {
            priority = 0;
        }
        if (confidenceThreshold == null) {
            confidenceThreshold = new BigDecimal("0.70");
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
