package com.example.backendlaptop.dto.phanQuyenDto.khachHang;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class KhachHangDto {
    private String maKhachHang;

    private String hoTen;

    private String soDienThoai;

    private String email;

    private Integer gioiTinh;

    private LocalDate ngaySinh;

    private Integer trangThai;

}
