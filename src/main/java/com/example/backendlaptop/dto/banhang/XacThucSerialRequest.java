package com.example.backendlaptop.dto.banhang;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class XacThucSerialRequest {
    
    @NotNull(message = "ID hóa đơn không được để trống")
    private UUID idHoaDon;
    
    @NotNull(message = "ID chi tiết sản phẩm không được để trống")
    private UUID idChiTietSanPham;
    
    @NotBlank(message = "Serial number không được để trống")
    private String serialNumber;
}

