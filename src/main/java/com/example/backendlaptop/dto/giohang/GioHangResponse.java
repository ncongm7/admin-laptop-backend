package com.example.backendlaptop.dto.giohang;

import com.example.backendlaptop.entity.GioHang;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class GioHangResponse {
    private UUID id;
    private UUID khachHangId;
    private Instant ngayTao;
    private Instant ngayCapNhat;
    private Integer trangThaiGioHang;

    public GioHangResponse(GioHang gioHang) {
        this.id = gioHang.getId();
        this.khachHangId = gioHang.getKhachHang().getId();
        this.ngayTao = gioHang.getNgayTao();
        this.ngayCapNhat = gioHang.getNgayCapNhat();
        this.trangThaiGioHang = gioHang.getTrangThaiGioHang();
    }
}
