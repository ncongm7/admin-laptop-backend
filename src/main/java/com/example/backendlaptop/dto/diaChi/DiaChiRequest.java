package com.example.backendlaptop.dto.diaChi;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
public class DiaChiRequest {
    private String maKhachHang;
    private String hoTen;
    private String sdt;
    private String diaChi;
    private String xa;
    private String tinh;
    private Boolean macDinh;
}
