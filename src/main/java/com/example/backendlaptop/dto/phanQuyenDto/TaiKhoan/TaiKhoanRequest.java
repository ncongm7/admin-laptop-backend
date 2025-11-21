package com.example.backendlaptop.dto.phanQuyenDto.TaiKhoan;

import com.example.backendlaptop.entity.VaiTro;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TaiKhoanRequest {
    private VaiTro maVaiTro;
    private String tenDangNhap;
    private String matKhau;
    private String email;
    private Integer trangThai;
    private Instant ngayTao;
    private Instant lanDangNhapCuoi;
}
