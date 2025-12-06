package com.example.backendlaptop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "chat")
public class Chat {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "khach_hang_id")
    private KhachHang khachHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nhan_vien_id")
    private NhanVien nhanVien;

    @Nationalized
    @Lob
    @Column(name = "noi_dung")
    private String noiDung;

    @Column(name = "ngay_phan_hoi")
    private Instant ngayPhanHoi;

    @Column(name = "is_from_customer")
    private Boolean isFromCustomer = false; // true = từ khách hàng, false = từ nhân viên

    @Column(name = "is_read")
    private Boolean isRead = false; // true = đã đọc, false = chưa đọc

    @Column(name = "conversation_id")
    private UUID conversationId; // ID cuộc hội thoại để nhóm các tin nhắn

    @Column(name = "message_type", length = 50)
    private String messageType = "text"; // text, image, file, system

    @Column(name = "file_url", length = 500)
    private String fileUrl; // URL file/ảnh nếu có

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_to_id")
    private Chat replyTo; // Tin nhắn được reply

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    // NEW: Chatbot AI fields
    @Column(name = "is_bot_message")
    private Boolean isBotMessage = false; // true = tin nhắn từ bot tự động
    
    @Column(name = "bot_confidence", precision = 3, scale = 2)
    private java.math.BigDecimal botConfidence; // Độ tin cậy của intent detection
    
    @Column(name = "intent_detected", length = 50)
    private String intentDetected; // Intent được phát hiện
    
    @Column(name = "requires_human_review")
    private Boolean requiresHumanReview = false; // Cần nhân viên xem xét

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (updatedAt == null) {
            updatedAt = Instant.now();
        }
        if (ngayPhanHoi == null) {
            ngayPhanHoi = Instant.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    /**
     * Generate hash from message content and timestamp for duplicate detection
     */
    public String generateMessageHash() {
        if (noiDung == null || conversationId == null || ngayPhanHoi == null) {
            return null;
        }
        
        // Create hash from: conversationId + noiDung + timestamp (rounded to seconds)
        String content = conversationId.toString() + "|" + noiDung.trim() + "|" + 
                        (ngayPhanHoi.getEpochSecond() / 60); // Round to minute to allow same message within 1 minute
        
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(content.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            // Fallback to simple hash
            return String.valueOf(content.hashCode());
        }
    }
}