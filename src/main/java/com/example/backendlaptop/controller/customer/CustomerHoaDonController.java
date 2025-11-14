package com.example.backendlaptop.controller.customer;

import com.example.backendlaptop.dto.hoadon.HoaDonDetailResponse;
import com.example.backendlaptop.dto.hoadon.HoaDonListResponse;
import com.example.backendlaptop.entity.HoaDon;
import com.example.backendlaptop.model.TrangThaiHoaDon;
import com.example.backendlaptop.model.response.ResponseObject;
import com.example.backendlaptop.service.hoadon.HoaDonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller xử lý các API liên quan đến đơn hàng của khách hàng
 */
@RestController
@RequestMapping("/api/customer/hoa-don")
@CrossOrigin(origins = "*")
public class CustomerHoaDonController {

    @Autowired
    private HoaDonService hoaDonService;

    /**
     * Lấy danh sách đơn hàng của khách (có phân trang, lọc theo trạng thái)
     * GET /api/customer/hoa-don?page=0&size=10&trangThai=CHO_THANH_TOAN
     */
    @GetMapping
    public ResponseEntity<?> getCustomerOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String trangThai,
            @RequestParam(required = false) UUID khachHangId) {
        
        try {
            // Tạo Pageable với sắp xếp theo ngày tạo giảm dần (mới nhất trước)
            Pageable pageable = PageRequest.of(page, size, Sort.by("ngayTao").descending());
            
            Page<HoaDonListResponse> orders = hoaDonService.getCustomerOrders(khachHangId, trangThai, pageable);
            
            return ResponseEntity.ok(new ResponseObject<>(orders));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseObject<>(false, null, "Lỗi khi lấy danh sách đơn hàng: " + e.getMessage()));
        }
    }

    /**
     * Lấy chi tiết đơn hàng theo ID
     * GET /api/customer/hoa-don/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderDetail(@PathVariable UUID id, @RequestParam(required = false) UUID khachHangId) {
        try {
            HoaDonDetailResponse orderDetail = hoaDonService.getOrderDetailForCustomer(id, khachHangId);
            
            if (orderDetail == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ResponseObject<>(false, null, "Không tìm thấy đơn hàng"));
            }
            
            return ResponseEntity.ok(new ResponseObject<>(orderDetail));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ResponseObject<>(false, null, "Bạn không có quyền xem đơn hàng này"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseObject<>(false, null, "Lỗi khi lấy chi tiết đơn hàng: " + e.getMessage()));
        }
    }

    /**
     * Hủy đơn hàng (chỉ cho phép khi trạng thái = CHO_THANH_TOAN)
     * PUT /api/customer/hoa-don/{id}/cancel
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable UUID id, @RequestParam(required = false) UUID khachHangId) {
        try {
            boolean result = hoaDonService.cancelOrderForCustomer(id, khachHangId);
            
            if (result) {
                return ResponseEntity.ok(new ResponseObject<>(true, null, "Hủy đơn hàng thành công"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ResponseObject<>(false, null, "Không thể hủy đơn hàng ở trạng thái hiện tại"));
            }
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ResponseObject<>(false, null, "Bạn không có quyền hủy đơn hàng này"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseObject<>(false, null, "Lỗi khi hủy đơn hàng: " + e.getMessage()));
        }
    }

    /**
     * Mua lại đơn hàng (thêm các sản phẩm trong đơn vào giỏ hàng)
     * POST /api/customer/hoa-don/{id}/reorder
     */
    @PostMapping("/{id}/reorder")
    public ResponseEntity<?> reorder(@PathVariable UUID id, @RequestParam(required = false) UUID khachHangId) {
        try {
            boolean result = hoaDonService.reorderForCustomer(id, khachHangId);
            
            if (result) {
                return ResponseEntity.ok(new ResponseObject<>(true, null, "Đã thêm sản phẩm vào giỏ hàng"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ResponseObject<>(false, null, "Không thể mua lại đơn hàng này"));
            }
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ResponseObject<>(false, null, "Bạn không có quyền thực hiện thao tác này"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseObject<>(false, null, "Lỗi khi mua lại đơn hàng: " + e.getMessage()));
        }
    }
}
