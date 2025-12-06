package com.example.backendlaptop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "helpful_votes", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"danh_gia_id", "khach_hang_id"}))
public class HelpfulVote {
    
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "danh_gia_id", nullable = false)
    private DanhGia danhGia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "khach_hang_id", nullable = false)
    private KhachHang khachHang;

    @Column(name = "is_helpful", nullable = false)
    private Boolean isHelpful; // true = helpful, false = not helpful

    @Column(name = "ngay_vote")
    private Instant ngayVote;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (ngayVote == null) {
            ngayVote = Instant.now();
        }
    }
}
