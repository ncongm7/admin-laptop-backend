package com.example.backendlaptop.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO Response chứa thông tin QR code thanh toán
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QRCodeResponse {
    
    /**
     * URL ảnh QR code (VietQR API trả về trực tiếp URL ảnh)
     */
    private String qrCodeUrl;
    
    /**
     * Data URL QR code dạng base64 (nếu cần embed vào HTML)
     */
    private String qrCodeDataUrl;
    
    /**
     * URL để thanh toán (deep link vào app ngân hàng)
     */
    private String paymentUrl;
    
    /**
     * Thời gian hết hạn QR code
     */
    private Instant expiryTime;
    
    /**
     * Mã đơn hàng
     */
    private String orderCode;
    
    /**
     * Số tiền
     */
    private Long amount;
    
    /**
     * Nội dung chuyển khoản
     */
    private String description;
    
    /**
     * Thông tin ngân hàng
     */
    private BankInfo bankInfo;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BankInfo {
        private String bankName;
        private String accountNo;
        private String accountName;
    }
}
