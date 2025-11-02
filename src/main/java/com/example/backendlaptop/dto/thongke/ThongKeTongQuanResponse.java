package com.example.backendlaptop.dto.thongke;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO response cho API thống kê tổng quan
 * Chứa tất cả các chỉ số cần thiết cho 4 thẻ trên Dashboard
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThongKeTongQuanResponse {
    
    // Thẻ Doanh số
    private DoanhSoInfo doanhSo;
    
    // Thẻ Doanh thu
    private DoanhThuInfo doanhThu;
    
    // Thẻ Khách hàng
    private KhachHangInfo khachHang;
    
    // Thẻ Tồn kho
    private TonKhoInfo tonKho;
    
    /**
     * Thông tin Doanh số
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DoanhSoInfo {
        private Long giaTri; // Số lượng hóa đơn
        private Double soSanhKyTruoc; // % thay đổi so với kỳ trước
    }
    
    /**
     * Thông tin Doanh thu
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DoanhThuInfo {
        private BigDecimal giaTri; // Tổng doanh thu
        private Double soSanhKyTruoc; // % thay đổi so với kỳ trước
        private BigDecimal loiNhuan; // Lợi nhuận
    }
    
    /**
     * Thông tin Khách hàng
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KhachHangInfo {
        private Long giaTri; // Số KH mới trong kỳ
        private Double soSanhKyTruoc; // % thay đổi so với kỳ trước
        private Long moiThangNay; // Số KH mới tháng này
        private Long hoatDong; // Số KH có phát sinh HĐ trong kỳ
    }
    
    /**
     * Thông tin Tồn kho
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TonKhoInfo {
        private Long sapHetHang; // Sản phẩm sắp hết (<=5)
        private Long canBoSung; // Sản phẩm cần bổ sung (=0)
    }
}

