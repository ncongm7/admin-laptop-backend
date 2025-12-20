package com.example.backendlaptop.dto.thongke;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO thống kê theo phương thức thanh toán
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThongKeTheoPhuongThucDTO {
    
    private String phuongThuc;  // "CASH", "QR", "COD"
    
    private BigDecimal doanhThu;
    
    private Long soDon;
    
    private BigDecimal doanhThuDaXacNhan;  // Đã confirm
    
    private BigDecimal doanhThuChuaXacNhan;  // Pending
    
    private Double tyLeSuDung;  // % so với tổng
}
