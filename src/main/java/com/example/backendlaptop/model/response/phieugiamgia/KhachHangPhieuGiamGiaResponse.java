// FILE: src/main/java/com/example/backendlaptop/model/response/phieugiamgia/KhachHangPhieuGiamGiaResponse.java
package com.example.backendlaptop.model.response.phieugiamgia;

import com.example.backendlaptop.entity.KhachHang;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KhachHangPhieuGiamGiaResponse {
    private UUID id;
    private String maKhachHang;
    private String hoTen;
    private String email;
    private String soDienThoai;
    
    public KhachHangPhieuGiamGiaResponse(KhachHang khachHang) {
        this.id = khachHang.getId();
        this.maKhachHang = khachHang.getMaKhachHang();
        this.hoTen = khachHang.getHoTen();
        this.email = khachHang.getEmail();
        this.soDienThoai = khachHang.getSoDienThoai();
    }
}

