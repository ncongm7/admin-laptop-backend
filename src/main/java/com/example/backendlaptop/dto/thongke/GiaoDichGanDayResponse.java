package com.example.backendlaptop.dto.thongke;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * DTO response cho giao dịch gần đây
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GiaoDichGanDayResponse {
    private UUID id;
    private String maHoaDon;
    private String tenKhachHang;
    private BigDecimal tongTien;
    private Instant ngayTao;
    private String trangThai; // "DA_THANH_TOAN", "DA_HUY", etc.
    private String loai; // "sale", "refund"
}

