package com.example.backendlaptop.controller.customer;

import com.example.backendlaptop.dto.banhang.VoucherSuggestionResponse;
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

import java.math.BigDecimal;
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
     * Lấy danh sách voucher suggestions cho giỏ hàng
     * GET /api/customer/phieu-giam-gia/suggestions
     * Bao gồm cả phiếu giảm giá cá nhân (riêng tư)
     */
    @Operation(summary = "Gợi ý voucher cho giỏ hàng", 
               description = "Lấy danh sách voucher có thể sử dụng cho giỏ hàng hiện tại, bao gồm cả phiếu giảm giá cá nhân")
    @GetMapping("/phieu-giam-gia/suggestions")
    public ResponseEntity<ResponseObject<List<VoucherSuggestionResponse>>> getVoucherSuggestions(
            @RequestParam(value = "khachHangId", required = false) UUID khachHangId,
            @RequestParam(value = "tongTienGioHang", defaultValue = "0") BigDecimal tongTienGioHang) {
        List<VoucherSuggestionResponse> suggestions = customerVoucherService.getVoucherSuggestions(
            khachHangId, tongTienGioHang);
        return ResponseEntity.ok(new ResponseObject<>(suggestions, "Lấy danh sách voucher suggestions thành công"));
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

