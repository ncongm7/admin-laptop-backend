package com.example.backendlaptop.dto.banhang;

import lombok.Data;

import java.util.UUID;

@Data
public class ThemSanPhamRequest {
    private UUID chiTietSanPhamId;
    private Integer soLuong;
}
