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
    public ResponseObject<?> getAll(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "pageNo", defaultValue = "0") Integer pageNo) {
        // Hỗ trợ cả pageNo (cũ) và page (mới)
        int pageNumber = page != null && page != 0 ? page : pageNo;
        return new ResponseObject<>(dotGiamGiaCustomerService.getAll(status, pageNumber, pageSize));
    }

    @GetMapping("/{id}")
    public ResponseObject<?> getCampaignDetail(@PathVariable UUID id, @RequestParam(value = "pageNo", defaultValue = "0") Integer pageNo) {
        return new ResponseObject<>(dotGiamGiaCustomerService.getCampaignDetail(id, pageNo));
    }
}
