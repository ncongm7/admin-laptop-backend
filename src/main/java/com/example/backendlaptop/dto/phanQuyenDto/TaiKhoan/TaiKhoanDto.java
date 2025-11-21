package com.example.backendlaptop.dto.phanQuyenDto.TaiKhoan;

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
public class TaiKhoanDto {
    private UUID id;
    private String tenVaiTro;
    private String tenDangNhap;
    private String matKhau;
    private String email;
    private Integer trangThai;
    private Instant ngayTao;
    private Instant lanDangNhapCuoi;
}
