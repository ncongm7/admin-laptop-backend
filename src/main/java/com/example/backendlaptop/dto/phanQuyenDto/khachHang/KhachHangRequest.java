package com.example.backendlaptop.dto.phanQuyenDto.khachHang;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter

public class KhachHangRequest {


    @NotBlank(message = "Mã k để trống")
    private String maKhachHang;


    private String hoTen;

    private String soDienThoai;

    private String email;

    private Integer gioiTinh;

    private LocalDate ngaySinh;

    private Integer trangThai;

}
