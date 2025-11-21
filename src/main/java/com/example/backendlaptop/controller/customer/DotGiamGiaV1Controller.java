package com.example.backendlaptop.controller.customer;

import com.example.backendlaptop.model.response.ResponseObject;
import com.example.backendlaptop.service.dotgiamgia.customer.DotGiamGiaCustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customer/dot-giam-gia")
@CrossOrigin("*")
@RequiredArgsConstructor
public class DotGiamGiaV1Controller {
    
    private final DotGiamGiaCustomerService dotGiamGiaCustomerService;

    @GetMapping("/active")
    public ResponseObject<?> getActivePromotions() {
        // Lấy promotions đang active (status = active)
        return new ResponseObject<>(dotGiamGiaCustomerService.getAll("active", 0, 100));
    }
}

