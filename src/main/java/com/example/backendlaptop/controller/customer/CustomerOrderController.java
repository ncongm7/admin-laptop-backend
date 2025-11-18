package com.example.backendlaptop.controller.customer;

import com.example.backendlaptop.dto.customer.TaoDonHangCustomerRequest;
import com.example.backendlaptop.dto.hoadon.HoaDonDetailResponse;
import com.example.backendlaptop.dto.hoadon.HoaDonListResponse;
import com.example.backendlaptop.model.response.ResponseObject;
import com.example.backendlaptop.service.customer.CustomerOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customer/orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CustomerOrderController {

    private final CustomerOrderService customerOrderService;

    /**
     * Tạo đơn hàng từ customer
     * POST /api/v1/customer/orders
     */
    @PostMapping
    public ResponseEntity<ResponseObject<HoaDonDetailResponse>> taoDonHang(
            @Valid @RequestBody TaoDonHangCustomerRequest request) {
        HoaDonDetailResponse response = customerOrderService.taoDonHang(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseObject<>(response, "Tạo đơn hàng thành công"));
    }

    /**
     * Lấy danh sách đơn hàng của customer
     * GET /api/v1/customer/orders?khachHangId={id}&page=0&size=10
     */
    @GetMapping
    public ResponseEntity<ResponseObject<Page<HoaDonListResponse>>> getDanhSachDonHang(
            @RequestParam UUID khachHangId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<HoaDonListResponse> result = customerOrderService.getDanhSachDonHang(khachHangId, page, size);
        return ResponseEntity.ok(new ResponseObject<>(result, "Lấy danh sách đơn hàng thành công"));
    }

    /**
     * Lấy chi tiết đơn hàng của customer
     * GET /api/v1/customer/orders/{idHoaDon}
     */
    @GetMapping("/{idHoaDon}")
    public ResponseEntity<ResponseObject<HoaDonDetailResponse>> getChiTietDonHang(
            @PathVariable UUID idHoaDon) {
        HoaDonDetailResponse result = customerOrderService.getChiTietDonHang(idHoaDon);
        return ResponseEntity.ok(new ResponseObject<>(result, "Lấy chi tiết đơn hàng thành công"));
    }

    /**
     * Lấy trạng thái đơn hàng của customer
     * GET /api/v1/customer/orders/{idHoaDon}/status
     */
    @GetMapping("/{idHoaDon}/status")
    public ResponseEntity<ResponseObject<HoaDonDetailResponse>> getOrderStatus(
            @PathVariable UUID idHoaDon,
            @RequestParam UUID khachHangId) {
        HoaDonDetailResponse result = customerOrderService.getChiTietDonHang(idHoaDon);
        // TODO: Có thể thêm validation để đảm bảo đơn hàng thuộc về khách hàng này
        return ResponseEntity.ok(new ResponseObject<>(result, "Lấy trạng thái đơn hàng thành công"));
    }

    /**
     * Hủy đơn hàng của customer
     * POST /api/v1/customer/orders/{idHoaDon}/cancel
     */
    @PostMapping("/{idHoaDon}/cancel")
    public ResponseEntity<ResponseObject<HoaDonDetailResponse>> cancelOrder(
            @PathVariable UUID idHoaDon,
            @RequestParam UUID khachHangId) {
        HoaDonDetailResponse result = customerOrderService.huyDonHang(idHoaDon, khachHangId);
        return ResponseEntity.ok(new ResponseObject<>(result, "Hủy đơn hàng thành công"));
    }
}

