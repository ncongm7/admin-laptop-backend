package com.example.backendlaptop.controller.banhang;

import com.example.backendlaptop.dto.banhang.HoaDonResponse;
import com.example.backendlaptop.dto.banhang.TaoHoaDonRequest;
import com.example.backendlaptop.dto.banhang.ThanhToanRequest;
import com.example.backendlaptop.dto.banhang.ThemSanPhamRequest;
import com.example.backendlaptop.model.response.ResponseObject;
import com.example.backendlaptop.service.banhang.BanHangTaiQuayService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ban-hang")
@CrossOrigin(origins = "*")
public class BanHangTaiQuayController {

    @Autowired
    private BanHangTaiQuayService banHangTaiQuayService;

    /**
     * API 1: Tạo Hóa Đơn Chờ Mới
     * Endpoint: POST /api/v1/ban-hang/hoa-don/tao-moi
     */
    @PostMapping("/hoa-don/tao-moi")
    public ResponseEntity<ResponseObject<HoaDonResponse>> taoHoaDonMoi(@RequestBody TaoHoaDonRequest request) {
        HoaDonResponse response = banHangTaiQuayService.taoHoaDonChoiMoi(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseObject<>(response, "Tạo hóa đơn chờ thành công"));
    }

    /**
     * API 2: Thêm Sản Phẩm Vào Hóa Đơn Chờ
     * Endpoint: POST /api/v1/ban-hang/hoa-don/{idHoaDon}/them-san-pham
     */
    @PostMapping("/hoa-don/{idHoaDon}/them-san-pham")
    public ResponseEntity<ResponseObject<HoaDonResponse>> themSanPhamVaoHoaDon(
            @PathVariable UUID idHoaDon,
            @Valid @RequestBody ThemSanPhamRequest request) {
        HoaDonResponse response = banHangTaiQuayService.themSanPhamVaoHoaDon(idHoaDon, request);
        return ResponseEntity.ok(new ResponseObject<>(response, "Thêm sản phẩm vào hóa đơn thành công"));
    }

    /**
     * API 3: Xóa Sản Phẩm Khỏi Hóa Đơn Chờ
     * Endpoint: DELETE /api/v1/ban-hang/hoa-don/xoa-san-pham/{idHoaDonChiTiet}
     */
    @DeleteMapping("/hoa-don/xoa-san-pham/{idHoaDonChiTiet}")
    public ResponseEntity<ResponseObject<HoaDonResponse>> xoaSanPhamKhoiHoaDon(@PathVariable UUID idHoaDonChiTiet) {
        HoaDonResponse response = banHangTaiQuayService.xoaSanPhamKhoiHoaDon(idHoaDonChiTiet);
        return ResponseEntity.ok(new ResponseObject<>(response, "Xóa sản phẩm khỏi hóa đơn thành công"));
    }

    /**
     * API 4: Hoàn Tất Thanh Toán Hóa Đơn
     * Endpoint: POST /api/v1/ban-hang/hoa-don/{idHoaDon}/thanh-toan
     */
    @PostMapping("/hoa-don/{idHoaDon}/thanh-toan")
    public ResponseEntity<ResponseObject<HoaDonResponse>> thanhToanHoaDon(
            @PathVariable UUID idHoaDon,
            @Valid @RequestBody ThanhToanRequest request) {
        HoaDonResponse response = banHangTaiQuayService.thanhToanHoaDon(idHoaDon, request);
        return ResponseEntity.ok(new ResponseObject<>(response, "Thanh toán hóa đơn thành công"));
    }
}
