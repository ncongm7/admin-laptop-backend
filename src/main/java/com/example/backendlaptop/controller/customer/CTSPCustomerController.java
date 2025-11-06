package com.example.backendlaptop.controller.customer;

import com.example.backendlaptop.dto.sanpham.customer.CTSPResponseCustomer;
import com.example.backendlaptop.model.response.ResponseObject;
import com.example.backendlaptop.service.sanpham.customer.CTSPCustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/customer/ctsp")
public class CTSPCustomerController {
    @Autowired
    private CTSPCustomerService ctspCustomerService;

    @GetMapping("/{id}")
    public ResponseObject<?> findById(@PathVariable("id") UUID id) {
        return new ResponseObject<>(ctspCustomerService.findById(id));
    }

    @GetMapping("/get-bien-the-san-pham/{sanPhamId}")
    public ResponseEntity<?> findAllBySanPhamId(@PathVariable("sanPhamId") UUID sanPhamId) {
        List<CTSPResponseCustomer> ctspResponseCustomers = ctspCustomerService.findAllBySanPhamId(sanPhamId);
        return ResponseEntity.ok(ctspResponseCustomers);
    }
}
