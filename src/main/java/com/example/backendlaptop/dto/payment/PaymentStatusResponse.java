package com.example.backendlaptop.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * DTO Response cho trạng thái thanh toán
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentStatusResponse {
    
    /**
     * ID hóa đơn
     */
    private UUID hoaDonId;
    
    /**
     * Mã đơn hàng
     */
    private String orderCode;
    
    /**
     * Trạng thái thanh toán (0: Chưa thanh toán, 1: Đã thanh toán)
     */
    private Integer trangThaiThanhToan;
    
    /**
     * Số tiền
     */
    private BigDecimal amount;
    
    /**
     * Mã giao dịch
     */
    private String transactionId;
    
    /**
     * Thời gian thanh toán
     */
    private Instant paymentTime;
    
    /**
     * Phương thức thanh toán
     */
    private String paymentMethod;
}
