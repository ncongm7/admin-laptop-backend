package com.example.backendlaptop.dto.dotgiamgia.customer;

import com.example.backendlaptop.entity.DotGiamGia;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
public class DotGiamGiaDTOCustomer {
    private UUID id;
    private String tenKm;
    private Integer loaiDotGiamGia; // 1: Giảm theo %, 2: Giảm theo số tiền (VND)
    private BigDecimal giaTri; // Giá trị giảm: % (0-100) hoặc số tiền VND
    private BigDecimal soTienGiamToiDa; // Giới hạn số tiền giảm tối đa (chỉ dùng khi loai = 1 - %)
    private String moTa;
    private Instant ngayBatDau;
    private Instant ngayKetThuc;
    private String bannerImageUrl;

    public DotGiamGiaDTOCustomer(DotGiamGia entity) {
        this.id = entity.getId();
        this.tenKm = entity.getTenKm();
        this.loaiDotGiamGia = entity.getLoaiDotGiamGia();
        this.giaTri = entity.getGiaTri();
        this.soTienGiamToiDa = entity.getSoTienGiamToiDa();
        this.moTa = entity.getMoTa();
        this.ngayBatDau = entity.getNgayBatDau();
        this.ngayKetThuc = entity.getNgayKetThuc();
        this.bannerImageUrl = entity.getBannerImageUrl();
    }
}
