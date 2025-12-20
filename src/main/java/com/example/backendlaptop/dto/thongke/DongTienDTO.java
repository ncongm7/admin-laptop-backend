package com.example.backendlaptop.dto.thongke;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO phân tích dòng tiền
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DongTienDTO {
    
    // ===== TIỀN ĐÃ VỀ =====
    /**
     * Tiền mặt thu tại quầy (CASH confirmed ngay)
     */
    private BigDecimal tienMatVe;
    
    /**
     * QR đã xác nhận (có payment_confirmed_at)
     */
    private BigDecimal qrDaVe;
    
    /**
     * COD đã thu (status = HOAN_THANH)
     */
    private BigDecimal codDaThu;
    
    /**
     * Tổng tiền đã về
     */
    private BigDecimal tongTienDaVe;
    
    // ===== TIỀN ĐANG CHỜ =====
    /**
     * QR đang chờ xác nhận
     */
    private BigDecimal qrDangCho;
    
    /**
     * COD đang giao hàng
     */
    private BigDecimal codDangGiao;
    
    /**
     * Tổng tiền đang chờ
     */
    private BigDecimal tongTienDangCho;
    
    // ===== TIỀN MẤT =====
    /**
     * Đơn hủy - tiền không thu được
     */
    private BigDecimal tienMatDoHuy;
    
    /**
     * Tiền hoàn trả (refund)
     */
    private BigDecimal tienHoanTra;
}
