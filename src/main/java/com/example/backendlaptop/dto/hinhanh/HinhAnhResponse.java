package com.example.backendlaptop.dto.hinhanh;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class HinhAnhResponse {
    
    private UUID id;
    private UUID idSpct;
    private String url;
    private Boolean anhChinhDaiDien;
    private Instant ngayTao;
    private Instant ngaySua;
    
    // Chi tiết sản phẩm info
    private String tenSanPham;
    private String maCtsp;
}
