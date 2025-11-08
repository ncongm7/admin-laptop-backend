package com.example.backendlaptop.dto.giohang.customer;

import com.example.backendlaptop.model.response.phieugiamgia.PhieuGiamGiaResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {
    private UUID id;
    private List<CartItemResponse> items;
    private BigDecimal subtotal;
    private BigDecimal discount;
    private BigDecimal shippingFee;
    private BigDecimal total;
    private PhieuGiamGiaResponse appliedVoucher;
    private Integer availablePoints;     // Điểm tích lũy
}

