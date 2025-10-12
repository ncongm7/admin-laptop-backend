package com.example.backendlaptop.controller;

import com.example.backendlaptop.dto.banhang.TaoHoaDonRequest;
import com.example.backendlaptop.dto.banhang.ThemSanPhamRequest;
import com.example.backendlaptop.entity.HoaDon;
import com.example.backendlaptop.model.response.ResponseObject;
import com.example.backendlaptop.service.BanHangTaiQuayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/ban-hang-tai-quay")
public class BanHangTaiQuayController {

//    @Autowired
//    private BanHangTaiQuayService banHangTaiQuayService;
//
//    @GetMapping("/hoa-don/cho-thanh-toan")
//    public ResponseEntity<?> getHoaDonCho() {
//        return ResponseEntity.ok(new ResponseObject("OK", "Lấy danh sách hóa đơn chờ thành công", banHangTaiQuayService.getDanhSachHoaDonCho()));
//    }
//
//    @PostMapping("/hoa-don")
//    public ResponseEntity<?> taoHoaDonCho(@RequestBody TaoHoaDonRequest request) {
//        HoaDon hoaDon = banHangTaiQuayService.taoHoaDonCho(request.getNhanVienId(), request.getKhachHangId());
//        return ResponseEntity.ok(new ResponseObject("OK", "Tạo hóa đơn chờ thành công", hoaDon));
//    }
//
//    @PostMapping("/hoa-don/{hoaDonId}/them-san-pham")
//    public ResponseEntity<?> themSanPhamVaoHoaDon(@PathVariable UUID hoaDonId, @RequestBody ThemSanPhamRequest request) {
//        HoaDon hoaDon = banHangTaiQuayService.themSanPhamVaoHoaDon(hoaDonId, request);
//        return ResponseEntity.ok(new ResponseObject("OK", "Thêm sản phẩm thành công", hoaDon));
//    }
//
//    @DeleteMapping("/hoa-don-chi-tiet/{hoaDonChiTietId}")
//    public ResponseEntity<?> xoaSanPhamKhoiHoaDon(@PathVariable UUID hoaDonChiTietId) {
//        HoaDon hoaDon = banHangTaiQuayService.xoaSanPhamKhoiHoaDon(hoaDonChiTietId);
//        return ResponseEntity.ok(new ResponseObject("OK", "Xóa sản phẩm thành công", hoaDon));
//    }
//
//    @PutMapping("/hoa-don/{hoaDonId}/huy")
//    public ResponseEntity<?> huyHoaDon(@PathVariable UUID hoaDonId) {
//        HoaDon hoaDon = banHangTaiQuayService.huyHoaDon(hoaDonId);
//        return ResponseEntity.ok(new ResponseObject("OK", "Hủy hóa đơn thành công", hoaDon));
//    }
//
//    @PostMapping("/hoa-don/{hoaDonId}/thanh-toan")
//    public ResponseEntity<?> thanhToan(@PathVariable UUID hoaDonId) {
//        HoaDon hoaDon = banHangTaiQuayService.thanhToanHoaDon(hoaDonId);
//        return ResponseEntity.ok(new ResponseObject("OK", "Thanh toán hóa đơn thành công", hoaDon));
//    }
}
