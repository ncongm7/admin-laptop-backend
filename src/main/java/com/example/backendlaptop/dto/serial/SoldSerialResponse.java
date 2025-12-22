package com.example.backendlaptop.dto.serial;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SoldSerialResponse {
    private UUID idSerial;
    private String serialNo;
    private String tenSanPham;
    private String tenKhachHang;
    private String sdtKhachHang;
    private UUID idHoaDon;
    private UUID idKhachHang;
    private UUID idHoaDonChiTiet;
    private UUID idSerialDaBan;
    private Instant ngayBan;
}
