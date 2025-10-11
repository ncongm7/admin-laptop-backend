package com.example.backendlaptop.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class DotGiamGiaChiTietRequest {

    @NotNull(message = "ID đợt giảm giá không được để trống")
    private UUID idKm;

    @NotNull(message = "ID chi tiết sản phẩm không được để trống")
    private UUID idCtsp;

    private BigDecimal giaBanDau;

    private BigDecimal giaSauKhiGiam;

    private String ghiChu;
}
