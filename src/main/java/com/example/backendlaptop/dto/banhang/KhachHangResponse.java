package com.example.backendlaptop.dto.banhang;

import com.example.backendlaptop.entity.KhachHang;
import lombok.Data;

import java.util.UUID;

/**
 * DTO trả về thông tin khách hàng trong hóa đơn
 * Bao gồm điểm tích lũy để hiển thị và sử dụng
 */
@Data
public class KhachHangResponse {
    private UUID id;
    private String maKhachHang;
    private String hoTen;
    private String soDienThoai;
    private String email;
    private Integer diemTichLuy; // Tổng điểm tích lũy hiện có

    public KhachHangResponse(KhachHang khachHang, Integer diemTichLuy) {
        if (khachHang != null) {
            this.id = khachHang.getId();
            this.maKhachHang = khachHang.getMaKhachHang();
            this.hoTen = khachHang.getHoTen();
            this.soDienThoai = khachHang.getSoDienThoai();
            this.email = khachHang.getEmail();
        }
        this.diemTichLuy = diemTichLuy != null ? diemTichLuy : 0;
    }
}

