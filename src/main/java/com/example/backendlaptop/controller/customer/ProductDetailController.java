package com.example.backendlaptop.controller.customer;

import com.example.backendlaptop.dto.sanpham.ProductDetailResponse;
import com.example.backendlaptop.model.response.ResponseObject;
import com.example.backendlaptop.service.customer.ProductDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@CrossOrigin("*")
@RequiredArgsConstructor
public class ProductDetailController {

    private final ProductDetailService productDetailService;

    @GetMapping("/{id}")
    public ResponseObject<ProductDetailResponse> getProductDetail(@PathVariable UUID id) {
        ProductDetailResponse response = productDetailService.getProductDetail(id);
        return new ResponseObject<>(response);
    }

    @GetMapping("/{id}/related")
    public ResponseObject<?> getRelatedProducts(
            @PathVariable UUID id,
            @RequestParam(value = "limit", defaultValue = "8") Integer limit) {
        return new ResponseObject<>(productDetailService.getRelatedProducts(id, limit));
    }
}

