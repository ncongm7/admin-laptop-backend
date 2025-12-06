package com.example.backendlaptop.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "chat_quick_replies")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatQuickReply {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "intent_code", length = 50)
    private String intentCode;
    
    @Column(name = "reply_text", nullable = false, length = 200)
    private String replyText;
    
    @Column(name = "reply_value", length = 500)
    private String replyValue;
    
    @Column(name = "reply_type", length = 50)
    private String replyType; // text, url, intent_trigger
    
    @Column(name = "display_order")
    private Integer displayOrder;
    
    @Column(name = "icon", length = 50)
    private String icon;
    
    @Column(name = "is_active")
    private Boolean isActive;
    
    @Column(name = "created_at")
    private Instant createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        if (isActive == null) {
            isActive = true;
        }
        if (replyType == null) {
            replyType = "text";
        }
        if (displayOrder == null) {
            displayOrder = 0;
        }
    }
}
