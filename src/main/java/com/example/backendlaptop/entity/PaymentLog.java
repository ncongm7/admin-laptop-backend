package com.example.backendlaptop.entity;

import com.example.backendlaptop.model.PaymentMethod;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Entity PaymentLog - Log mọi sự kiện thanh toán để audit trail
 */
@Getter
@Setter
@Entity
@Table(name = "payment_logs")
public class PaymentLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hoa_don_id", nullable = false)
    private HoaDon hoaDon;
    
    @Convert(converter = com.example.backendlaptop.converter.PaymentMethodConverter.class)
    @Column(name = "payment_method", length = 20, nullable = false)
    private PaymentMethod paymentMethod;
    
    @Column(name = "amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal amount;
    
    @Size(max = 20)
    @Column(name = "status", length = 20, nullable = false)
    private String status;  // PENDING, CONFIRMED, CANCELLED, REFUNDED
    
    @Column(name = "confirmed_at")
    private Instant confirmedAt;
    
    @Column(name = "created_at")
    private Instant createdAt;
    
    @Nationalized
    @Lob
    @Column(name = "notes")
    private String notes;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
