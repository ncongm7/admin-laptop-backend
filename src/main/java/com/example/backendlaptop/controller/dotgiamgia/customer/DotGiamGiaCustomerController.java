package com.example.backendlaptop.controller.dotgiamgia.customer;

import com.example.backendlaptop.model.response.ResponseObject;
import com.example.backendlaptop.service.dotgiamgia.customer.DotGiamGiaCustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/promotions/campaigns")
public class DotGiamGiaCustomerController {
    @Autowired
    private DotGiamGiaCustomerService dotGiamGiaCustomerService;

    @GetMapping("")
    public ResponseObject<?> getAll(@RequestParam(value = "pageNo", defaultValue = "0") Integer pageNo) {
        return new ResponseObject<>(dotGiamGiaCustomerService.getAll(pageNo));
    }

    @GetMapping("/{id}")
    public ResponseObject<?> getIdCtspByIdDotGiamGia(@PathVariable UUID id, @RequestParam(value = "pageNo", defaultValue = "0") Integer pageNo) {
        return new ResponseObject<>(dotGiamGiaCustomerService.getIdCtspByIdDotGiamGia(id, pageNo));
    }
}
