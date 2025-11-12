package com.example.backendlaptop.dto.giohang.customer;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApplyVoucherRequest {
    @NotBlank(message = "Mã giảm giá không được để trống")
    private String voucherCode;
}

