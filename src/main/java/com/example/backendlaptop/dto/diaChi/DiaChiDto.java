package com.example.backendlaptop.dto.diaChi;


import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DiaChiDto {
    private UUID id;
    private String maKhachHang;
    private String hoTen;
    private String sdt;
    private String diaChi;
    private String xa;
    private String tinh;
    private Boolean macDinh;

}
