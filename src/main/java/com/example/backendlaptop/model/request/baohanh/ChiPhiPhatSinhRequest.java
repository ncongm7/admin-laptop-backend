package com.example.backendlaptop.model.request.baohanh;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ChiPhiPhatSinhRequest {
    @NotNull(message = "Chi phí phát sinh không được để trống")
    @DecimalMin(value = "0.0", message = "Chi phí phát sinh phải lớn hơn hoặc bằng 0")
    private BigDecimal chiPhiPhatSinh;

    @NotNull(message = "Lý do chi phí không được để trống")
    private String lyDo;
}

