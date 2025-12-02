package com.example.backendlaptop.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO Request để tạo QR code thanh toán
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QRCodeRequest {
    
    /**
     * ID hóa đơn (có thể null nếu tạo QR trước khi tạo hóa đơn)
     */
    private UUID hoaDonId;
    
    /**
     * Số tiền thanh toán
     */
    private BigDecimal amount;
    
    /**
     * Mã đơn hàng (dùng làm nội dung chuyển khoản)
     */
    private String orderCode;
    
    /**
     * Thông tin bổ sung (optional)
     */
    private String description;
}
