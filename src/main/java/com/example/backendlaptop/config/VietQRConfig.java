package com.example.backendlaptop.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình VietQR Payment Gateway
 * Sử dụng VietQR API để generate QR code cho thanh toán chuyển khoản
 */
@Configuration
@ConfigurationProperties(prefix = "vietqr")
@Data
public class VietQRConfig {
    
    /**
     * Thông tin ngân hàng nhận tiền
     */
    private Bank bank = new Bank();
    
    /**
     * Thông tin API
     */
    private Api api = new Api();
    
    /**
     * Thông tin thanh toán
     */
    private Payment payment = new Payment();
    
    @Data
    public static class Bank {
        /**
         * Mã BIN của ngân hàng (VD: 970415 = VietinBank)
         */
        private String bin;
        
        /**
         * Số tài khoản nhận tiền
         */
        private String accountNo;
        
        /**
         * Tên chủ tài khoản
         */
        private String accountName;
        
        /**
         * Template QR code (compact, compact2, qr_only, print)
         */
        private String template = "compact2";
    }
    
    @Data
    public static class Api {
        /**
         * URL API VietQR (https://img.vietqr.io/image)
         */
        private String url;
    }
    
    @Data
    public static class Payment {
        /**
         * Timeout thanh toán (giây) - default 15 phút = 900s
         */
        private Integer timeout = 900;
    }
}
