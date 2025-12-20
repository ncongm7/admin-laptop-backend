package com.example.backendlaptop.dto.thongke;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO thống kê theo kênh bán hàng
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThongKeTheoKenhDTO {
    
    private String tenKenh;  // "POS" hoặc "ONLINE"
    
    private BigDecimal doanhThu;
    
    private Long soDon;
    
    private Long soDonThanhCong;
    
    private Long soDonHuy;
    
    private Double tyLeHuy;  // Tỷ lệ hủy đơn (%)
    
    private BigDecimal giaTriTrungBinh;  // Giá trị đơn hàng trung bình
}
