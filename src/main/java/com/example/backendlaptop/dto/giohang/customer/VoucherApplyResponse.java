package com.example.backendlaptop.dto.giohang.customer;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VoucherApplyResponse {
    private Boolean success;
    private String message;
    private BigDecimal discountAmount;
    private CartResponse updatedCart;
}

