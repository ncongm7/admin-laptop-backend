package com.example.backendlaptop.dto.banhang;

import lombok.Data;

import java.util.UUID;

@Data
public class TaoHoaDonRequest {
    private UUID nhanVienId;
    private UUID khachHangId; // Có thể null nếu là khách vãng lai
}
