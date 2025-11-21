package com.example.backendlaptop.controller.customer;

import com.example.backendlaptop.model.response.ResponseObject;
import com.example.backendlaptop.service.customer.FlashSaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products/flash-sale")
@CrossOrigin("*")
@RequiredArgsConstructor
public class FlashSaleController {

    private final FlashSaleService flashSaleService;

    @GetMapping
    public ResponseObject<?> getFlashSaleProducts() {
        return new ResponseObject<>(flashSaleService.getFlashSaleProducts());
    }
}

