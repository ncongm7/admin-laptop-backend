package com.example.backendlaptop.model.response.dotgiamgia;

import com.example.backendlaptop.entity.DotGiamGia;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class DotGiamGiaResponse {

    private UUID id;
    private String tenKm;
    private Integer loaiDotGiamGia; // 1: Giảm theo %, 2: Giảm theo số tiền (VND)
    private BigDecimal giaTri; // Giá trị giảm: % (0-100) hoặc số tiền VND
    private BigDecimal soTienGiamToiDa; // Giới hạn số tiền giảm tối đa (chỉ dùng khi loai = 1 - %)
    private String moTa;
    private Instant ngayBatDau;
    private Instant ngayKetThuc;
    private Integer trangThai;

    public DotGiamGiaResponse(DotGiamGia entity) {
        this.id = entity.getId();
        this.tenKm = entity.getTenKm();
        this.loaiDotGiamGia = entity.getLoaiDotGiamGia();
        this.giaTri = entity.getGiaTri();
        this.soTienGiamToiDa = entity.getSoTienGiamToiDa();
        this.moTa = entity.getMoTa();
        this.ngayBatDau = entity.getNgayBatDau();
        this.ngayKetThuc = entity.getNgayKetThuc();
        this.trangThai = entity.getTrangThai();
    }
}