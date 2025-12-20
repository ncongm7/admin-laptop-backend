package com.example.backendlaptop.dto.giohang.customer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response cho cart validation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartValidationResponse {
    private Boolean isValid;
    private String message;
    private List<OutOfStockItem> outOfStockItems;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OutOfStockItem {
        private String productId;
        private String productName;
        private String variantName;
        private Integer requestedQuantity;
        private Integer availableStock;
    }
}
