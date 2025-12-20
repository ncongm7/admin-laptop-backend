package com.example.backendlaptop.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;
import java.util.UUID;

/**
 * Entity OrderStatusLog - Log mọi thay đổi trạng thái đơn hàng
 */
@Getter
@Setter
@Entity
@Table(name = "order_status_logs")
public class OrderStatusLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hoa_don_id", nullable = false)
    private HoaDon hoaDon;
    
    @Column(name = "old_status")
    private Integer oldStatus;
    
    @Column(name = "new_status", nullable = false)
    private Integer newStatus;
    
    @Column(name = "changed_by")
    private UUID changedBy;  // ID của nhân viên hoặc hệ thống
    
    @Column(name = "changed_at")
    private Instant changedAt;
    
    @Size(max = 500)
    @Nationalized
    @Column(name = "reason", length = 500)
    private String reason;
    
    @PrePersist
    protected void onCreate() {
        if (changedAt == null) {
            changedAt = Instant.now();
        }
    }
}
