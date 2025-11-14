package com.example.backendlaptop.controller.customer;

import com.example.backendlaptop.dto.giohang.customer.AddToCartRequest;
import com.example.backendlaptop.dto.giohang.customer.CartResponse;
import com.example.backendlaptop.dto.giohang.customer.UpdateCartItemRequest;
import com.example.backendlaptop.model.response.ResponseObject;
import com.example.backendlaptop.service.banhang.CustomerGioHangService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/customer/gio-hang")
@CrossOrigin(origins = "*")
@Tag(name = "Customer Cart", description = "API giỏ hàng cho khách hàng")
public class CustomerGioHangController {

    @Autowired
    private CustomerGioHangService customerGioHangService;

    /**
     * Lấy giỏ hàng của khách hàng hiện tại
     * GET /api/customer/gio-hang
     */
    @Operation(summary = "Lấy giỏ hàng", description = "Lấy giỏ hàng của khách hàng hiện tại")
    @GetMapping("")
    public ResponseEntity<ResponseObject<CartResponse>> getCart(
            @RequestParam("khachHangId") UUID khachHangId) {
        // TODO: Get khachHangId from JWT token instead of parameter
        CartResponse cart = customerGioHangService.getCart(khachHangId);
        return ResponseEntity.ok(new ResponseObject<>(cart, "Lấy giỏ hàng thành công"));
    }

    /**
     * Thêm sản phẩm vào giỏ hàng
     * POST /api/customer/gio-hang/items
     */
    @Operation(summary = "Thêm sản phẩm vào giỏ", description = "Thêm sản phẩm vào giỏ hàng")
    @PostMapping("/items")
    public ResponseEntity<ResponseObject<CartResponse>> addToCart(
            @RequestParam("khachHangId") UUID khachHangId,
            @Valid @RequestBody AddToCartRequest request) {
        CartResponse cart = customerGioHangService.addToCart(khachHangId, request);
        return ResponseEntity.ok(new ResponseObject<>(cart, "Thêm sản phẩm vào giỏ hàng thành công"));
    }

    /**
     * Cập nhật số lượng sản phẩm trong giỏ
     * PUT /api/customer/gio-hang/items/{itemId}
     */
    @Operation(summary = "Cập nhật số lượng", description = "Cập nhật số lượng sản phẩm trong giỏ")
    @PutMapping("/items/{itemId}")
    public ResponseEntity<ResponseObject<CartResponse>> updateCartItem(
            @RequestParam("khachHangId") UUID khachHangId,
            @PathVariable UUID itemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        CartResponse cart = customerGioHangService.updateCartItem(khachHangId, itemId, request);
        return ResponseEntity.ok(new ResponseObject<>(cart, "Cập nhật giỏ hàng thành công"));
    }

    /**
     * Xóa sản phẩm khỏi giỏ hàng
     * DELETE /api/customer/gio-hang/items/{itemId}
     */
    @Operation(summary = "Xóa sản phẩm", description = "Xóa sản phẩm khỏi giỏ hàng")
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<ResponseObject<CartResponse>> removeCartItem(
            @RequestParam("khachHangId") UUID khachHangId,
            @PathVariable UUID itemId) {
        CartResponse cart = customerGioHangService.removeCartItem(khachHangId, itemId);
        return ResponseEntity.ok(new ResponseObject<>(cart, "Xóa sản phẩm khỏi giỏ hàng thành công"));
    }

    /**
     * Xóa toàn bộ giỏ hàng
     * DELETE /api/customer/gio-hang/clear
     */
    @Operation(summary = "Xóa giỏ hàng", description = "Xóa toàn bộ sản phẩm trong giỏ hàng")
    @DeleteMapping("/clear")
    public ResponseEntity<ResponseObject<String>> clearCart(
            @RequestParam("khachHangId") UUID khachHangId) {
        customerGioHangService.clearCart(khachHangId);
        return ResponseEntity.ok(new ResponseObject<>("Đã xóa toàn bộ giỏ hàng", "Xóa giỏ hàng thành công"));
    }
}

