package com.example.backendlaptop.dto.phanQuyenDto.khachHang;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class KhachHangDto {
    private UUID id;
    
    private String maKhachHang;

    private String hoTen;

    private String soDienThoai;

    private String email;

    private Integer gioiTinh;

    private LocalDate ngaySinh;

    private Integer trangThai;
    
    // Thêm field để biết khách hàng có tài khoản hay không
    private Boolean hasTaiKhoan;
    
    // Constructor không có hasTaiKhoan (để tương thích với code cũ)
    public KhachHangDto(UUID id, String maKhachHang, String hoTen, String soDienThoai, 
                        String email, Integer gioiTinh, LocalDate ngaySinh, Integer trangThai) {
        this.id = id;
        this.maKhachHang = maKhachHang;
        this.hoTen = hoTen;
        this.soDienThoai = soDienThoai;
        this.email = email;
        this.gioiTinh = gioiTinh;
        this.ngaySinh = ngaySinh;
        this.trangThai = trangThai;
        this.hasTaiKhoan = false; // Mặc định false
    }

}
