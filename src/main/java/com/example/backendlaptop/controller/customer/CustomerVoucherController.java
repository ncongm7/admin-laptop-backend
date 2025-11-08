package com.example.backendlaptop.controller.customer;

import com.example.backendlaptop.dto.giohang.customer.ApplyVoucherRequest;
import com.example.backendlaptop.dto.giohang.customer.VoucherApplyResponse;
import com.example.backendlaptop.model.response.ResponseObject;
import com.example.backendlaptop.model.response.phieugiamgia.PhieuGiamGiaResponse;
import com.example.backendlaptop.service.phieugiamgia.CustomerVoucherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/customer")
@CrossOrigin(origins = "*")
@Tag(name = "Customer Voucher", description = "API voucher cho khách hàng")
public class CustomerVoucherController {

    @Autowired
    private CustomerVoucherService customerVoucherService;

    /**
     * Lấy danh sách voucher khả dụng
     * GET /api/customer/phieu-giam-gia/available
     */
    @Operation(summary = "Danh sách voucher khả dụng", description = "Lấy danh sách voucher có thể sử dụng")
    @GetMapping("/phieu-giam-gia/available")
    public ResponseEntity<ResponseObject<List<PhieuGiamGiaResponse>>> getAvailableVouchers() {
        List<PhieuGiamGiaResponse> vouchers = customerVoucherService.getAvailableVouchers();
        return ResponseEntity.ok(new ResponseObject<>(vouchers, "Lấy danh sách voucher thành công"));
    }

    /**
     * Áp dụng voucher vào giỏ hàng
     * POST /api/customer/gio-hang/apply-voucher
     */
    @Operation(summary = "Áp dụng voucher", description = "Áp dụng mã giảm giá vào giỏ hàng")
    @PostMapping("/gio-hang/apply-voucher")
    public ResponseEntity<ResponseObject<VoucherApplyResponse>> applyVoucher(
            @RequestParam("khachHangId") UUID khachHangId,
            @Valid @RequestBody ApplyVoucherRequest request) {
        VoucherApplyResponse response = customerVoucherService.applyVoucher(khachHangId, request);
        
        if (response.getSuccess()) {
            return ResponseEntity.ok(new ResponseObject<>(response, "Áp dụng voucher thành công"));
        } else {
            return ResponseEntity.badRequest().body(new ResponseObject<>(false, response, response.getMessage()));
        }
    }
}

