package com.example.backendlaptop.dto.banhang;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class ThanhToanRequest {
    @NotNull(message = "ID phương thức thanh toán không được để trống")
    private UUID idPhuongThucThanhToan;
    
    @NotNull(message = "Số tiền thanh toán không được để trống")
    private BigDecimal soTienThanhToan;
    
    private String ghiChu;
    
    private String maGiaoDich;
}

