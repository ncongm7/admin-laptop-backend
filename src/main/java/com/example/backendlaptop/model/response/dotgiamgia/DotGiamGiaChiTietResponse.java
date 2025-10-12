package com.example.backendlaptop.model.response.dotgiamgia;

import com.example.backendlaptop.entity.DotGiamGiaChiTiet;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class DotGiamGiaChiTietResponse {

    private UUID id;
    private String tenDotGiamGia;
    private String idCtsp;
    private BigDecimal giaBanDau;
    private BigDecimal giaSauKhiGiam;
    private String ghiChu;

    public DotGiamGiaChiTietResponse(DotGiamGiaChiTiet entity) {
        this.id = entity.getId();
        this.tenDotGiamGia = entity.getDotGiamGia().getTenKm();
        this.idCtsp = entity.getIdCtsp().getMaCtsp();
        this.giaBanDau = entity.getGiaBanDau();
        this.giaSauKhiGiam = entity.getGiaSauKhiGiam();
    }
}
