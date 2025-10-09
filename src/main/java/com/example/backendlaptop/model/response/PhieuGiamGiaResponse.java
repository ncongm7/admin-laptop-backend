package com.example.backendlaptop.model.response;

import com.example.backendlaptop.entity.PhieuGiamGia;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * DTO dùng để trả dữ liệu Phiếu Giảm Giá ra frontend.
 * Mapping đơn giản 1-1 từ Entity -> DTO.
 */
@Getter
@Setter
public class PhieuGiamGiaResponse {

    private UUID id;
    private String ma;
    private String tenPhieuGiamGia;
    private Integer loaiPhieuGiamGia;
    private BigDecimal giaTriGiamGia;
    private BigDecimal soTienGiamToiDa;
    private BigDecimal hoaDonToiThieu;
    private Integer soLuongDung;
    private Instant ngayBatDau;
    private Instant ngayKetThuc;
    private Boolean riengTu;
    private String moTa;
    private Integer trangThai;

    /**
     * Constructor mapping entity -> DTO
     */
    public PhieuGiamGiaResponse(PhieuGiamGia entity) {
        this.id = entity.getId();
        this.ma = entity.getMa();
        this.tenPhieuGiamGia = entity.getTenPhieuGiamGia();
        this.loaiPhieuGiamGia = entity.getLoaiPhieuGiamGia();
        this.giaTriGiamGia = entity.getGiaTriGiamGia();
        this.soTienGiamToiDa = entity.getSoTienGiamToiDa();
        this.hoaDonToiThieu = entity.getHoaDonToiThieu();
        this.soLuongDung = entity.getSoLuongDung();
        this.ngayBatDau = entity.getNgayBatDau();
        this.ngayKetThuc = entity.getNgayKetThuc();
        this.riengTu = entity.getRiengTu();
        this.moTa = entity.getMoTa();
        this.trangThai = entity.getTrangThai();
    }
}

