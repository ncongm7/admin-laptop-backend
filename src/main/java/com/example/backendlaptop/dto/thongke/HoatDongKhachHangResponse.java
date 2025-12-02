package com.example.backendlaptop.dto.thongke;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO response cho hoạt động khách hàng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HoatDongKhachHangResponse {
    private UUID id;
    private String tenKhachHang;
    private String moTa; // Mô tả hoạt động
    private Instant thoiGian;
    private String loai; // "purchase", "register", "login", etc.
}

