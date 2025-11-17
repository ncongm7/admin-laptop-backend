package com.example.backendlaptop.controller.phieugiamgia.customer;

import com.example.backendlaptop.model.response.ResponseObject;
import com.example.backendlaptop.service.phieugiamgia.customer.PhieuGiamGiaCustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/promotions/vouchers")
public class PhieuGiamGiaCustomerController {
    @Autowired
    private PhieuGiamGiaCustomerService phieuGiamGiaCustomerService;

    @GetMapping("")
    public ResponseObject<?> getAll(@RequestParam(value = "pageNo", defaultValue = "0") Integer pageNo) {
        return new ResponseObject<>(phieuGiamGiaCustomerService.getAll(pageNo));
    }
}
