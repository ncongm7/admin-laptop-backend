package com.example.backendlaptop.dto.giohang;

import com.example.backendlaptop.entity.GioHangChiTiet;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class GioHangChiTietResponse {
    private UUID id;
    private UUID gioHangId;
    private UUID chiTietSanPhamId;
    private Integer soLuong;
    private BigDecimal donGia;
    private Instant ngayThem;

    public GioHangChiTietResponse(GioHangChiTiet gioHangChiTiet) {
        this.id = gioHangChiTiet.getId();
        this.gioHangId = gioHangChiTiet.getGioHang().getId();
        this.chiTietSanPhamId = gioHangChiTiet.getChiTietSanPham().getId();
        this.soLuong = gioHangChiTiet.getSoLuong();
        this.donGia = gioHangChiTiet.getDonGia();
        this.ngayThem = gioHangChiTiet.getNgayThem();
    }
}
