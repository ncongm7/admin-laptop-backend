package com.example.backendlaptop.model.response.dotgiamgia;

import com.example.backendlaptop.entity.SanPham;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;
@Getter
@Setter
public class SanPhamDotGiamGiaResponse {
    private UUID id;
    private String ma;
    private String ten;

    public  SanPhamDotGiamGiaResponse(SanPham sanPham) {
        this.id = sanPham.getId();
        this.ten = sanPham.getTenSanPham();
        this.ma = sanPham.getMaSanPham();
    }
}
