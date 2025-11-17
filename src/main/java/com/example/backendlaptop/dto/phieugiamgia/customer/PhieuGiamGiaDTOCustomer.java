package com.example.backendlaptop.dto.phieugiamgia.customer;

import com.example.backendlaptop.entity.PhieuGiamGia;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
@Data
public class PhieuGiamGiaDTOCustomer {
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
    private String moTa;

    public PhieuGiamGiaDTOCustomer(PhieuGiamGia entity) {
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
        this.moTa = entity.getMoTa();
    }
}
