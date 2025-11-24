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
}