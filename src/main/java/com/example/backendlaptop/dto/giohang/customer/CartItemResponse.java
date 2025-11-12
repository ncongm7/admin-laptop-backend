package com.example.backendlaptop.dto.giohang.customer;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {
    private UUID id;
    private UUID ctspId;
    private String tenSanPham;
    private String maSanPham;
    private String imageUrl;
    private String variantName;          // "Đen - i5/16GB/512GB"
    private BigDecimal price;
    private Integer quantity;
    private Integer maxQuantity;         // Tồn kho
    private BigDecimal subtotal;
    private Boolean selected;            // Checkbox chọn thanh toán
}

