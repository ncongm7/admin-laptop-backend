package com.example.backendlaptop.model.response;

import com.example.backendlaptop.entity.DotGiamGia;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class DotGiamGiaResponse {

    private UUID id;
    private String tenKm;
    private Integer giaTri;
    private String moTa;
    private Instant ngayBatDau;
    private Instant ngayKetThuc;
    private Integer trangThai;

    public DotGiamGiaResponse(DotGiamGia entity) {
        this.id = entity.getId();
        this.tenKm = entity.getTenKm();
        this.giaTri = entity.getGiaTri();
        this.moTa = entity.getMoTa();
        this.ngayBatDau = entity.getNgayBatDau();
        this.ngayKetThuc = entity.getNgayKetThuc();
        this.trangThai = entity.getTrangThai();
    }
}
