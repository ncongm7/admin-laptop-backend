package com.example.backendlaptop.dto.banhang;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO response cho API kiểm tra và cập nhật giá sản phẩm
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CapNhatGiaResponse {
    /**
     * Có thay đổi giá hay không
     */
    private boolean coThayDoi;
    
    /**
     * Số lượng sản phẩm có giá thay đổi
     */
    private Integer soSanPhamThayDoi;
    
    /**
     * Danh sách thông tin thay đổi giá
     */
    private List<ThongTinThayDoiGia> danhSachThayDoi;
    
    /**
     * Hóa đơn đã được cập nhật
     */
    private HoaDonResponse hoaDon;
    
    /**
     * Thông tin chi tiết về thay đổi giá của một sản phẩm
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ThongTinThayDoiGia {
        private String tenSanPham;
        private String maCtsp;
        private BigDecimal giaCu;
        private BigDecimal giaMoi;
    }
}

