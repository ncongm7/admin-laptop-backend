package com.example.backendlaptop.dto.banhang;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class XacThucSerialResponse {
    
    private boolean isValid;
    private String message;
    private UUID serialId;
    private String serialNumber;
    private String tenSanPham;
    private String maChiTietSanPham;
}

