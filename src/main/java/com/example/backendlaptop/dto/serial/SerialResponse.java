package com.example.backendlaptop.dto.serial;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class SerialResponse {
    
    private UUID id;
    private UUID ctspId;
    private String serialNo;
    private Integer trangThai;
    private Instant ngayNhap;
    
    // Chi tiết sản phẩm info
    private String tenSanPham;
    private String maCtsp;
    
    // Status description
    private String trangThaiText;
    
    public String getTrangThaiText() {
        if (trangThai == null) return "Không xác định";
        return switch (trangThai) {
            case 1 -> "Chưa bán";
            case 2 -> "Đã bán";
            case 0 -> "Hỏng";
            default -> "Không xác định";
        };
    }
}
