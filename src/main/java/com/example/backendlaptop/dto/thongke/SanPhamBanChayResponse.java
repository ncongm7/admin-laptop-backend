package com.example.backendlaptop.dto.thongke;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO response cho API sản phẩm bán chạy
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SanPhamBanChayResponse {
    private UUID id; // ID của ChiTietSanPham
    private String tenSanPham; // Tên sản phẩm
    private String maCtsp; // Mã chi tiết sản phẩm
    private String anhDaiDien; // URL ảnh đại diện
    private Long soLuongBan; // Tổng số lượng đã bán
    private BigDecimal doanhThu; // Tổng doanh thu từ sản phẩm này
}

