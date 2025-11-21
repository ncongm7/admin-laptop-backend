package com.example.backendlaptop.dto.review;

import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
public class ReviewResponse {
    
    private UUID id;
    private Integer soSao;
    private String noiDung;
    private Instant ngayDanhGia;
    private Integer trangThaiDanhGia;
    
    // Thông tin khách hàng
    private CustomerInfo khachHang;
    
    // Hình ảnh đánh giá
    private List<String> imageUrls;
    
    @Data
    public static class CustomerInfo {
        private UUID id;
        private String tenKhachHang;
        private String avatarUrl;
    }
}

