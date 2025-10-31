package com.example.backendlaptop.dto.banhang;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

/**
 * DTO cho request áp dụng voucher
 */
@Data
public class ApDungVoucherRequest {
    
    @NotNull(message = "ID phiếu giảm giá không được để trống")
    private UUID idPhieuGiamGia;
}

