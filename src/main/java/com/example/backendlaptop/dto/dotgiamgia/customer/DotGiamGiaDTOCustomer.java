package com.example.backendlaptop.dto.dotgiamgia.customer;

import com.example.backendlaptop.entity.DotGiamGia;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;
@Data
public class DotGiamGiaDTOCustomer {
    private UUID id;
    private String tenKm;
    private Integer giaTri;
    private String moTa;
    private Instant ngayBatDau;
    private Instant ngayKetThuc;
    private String bannerImageUrl;

    public DotGiamGiaDTOCustomer(DotGiamGia entity) {
        this.id = entity.getId();
        this.tenKm = entity.getTenKm();
        this.giaTri = entity.getGiaTri();
        this.moTa = entity.getMoTa();
        this.ngayBatDau = entity.getNgayBatDau();
        this.ngayKetThuc = entity.getNgayKetThuc();
        this.bannerImageUrl = entity.getBannerImageUrl();
    }
}
