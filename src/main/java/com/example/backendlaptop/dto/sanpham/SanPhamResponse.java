package com.example.backendlaptop.dto.sanpham;

import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
public class SanPhamResponse {
    
    private UUID id;
    private String maSanPham;
    private String tenSanPham;
    private String moTa;
    private Integer trangThai;
    private Instant ngayTao;
    private Instant ngaySua;
    private String nguoiTao;
    private String nguoiSua;
    
    // Danh sách chi tiết sản phẩm (biến thể) - Dùng cho module Bán Hàng
    private List<ChiTietSanPhamResponse> chiTietSanPhams;
}
