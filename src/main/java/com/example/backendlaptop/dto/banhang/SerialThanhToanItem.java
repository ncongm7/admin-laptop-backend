package com.example.backendlaptop.dto.banhang;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

/**
 * Đại diện cho mỗi Serial Number đã được quét/nhập khi thanh toán
 */
@Data
public class SerialThanhToanItem {
    
    @NotNull(message = "ID chi tiết hóa đơn không được để trống")
    private UUID idHoaDonChiTiet;
    
    @NotNull(message = "ID chi tiết sản phẩm không được để trống")
    private UUID idChiTietSanPham;
    
    @NotBlank(message = "Serial number không được để trống")
    private String serialNumber;
}

