package com.example.backendlaptop.controller;

import com.example.backendlaptop.dto.banhang.TaoHoaDonRequest;
import com.example.backendlaptop.dto.banhang.ThemSanPhamRequest;
import com.example.backendlaptop.entity.HoaDon;
import com.example.backendlaptop.model.response.ResponseObject;
import com.example.backendlaptop.service.BanHangTaiQuayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/ban-hang-tai-quay")
public class BanHangTaiQuayController {

    @Autowired
    private BanHangTaiQuayService banHangTaiQuayService;

    @GetMapping("/hoa-don/cho-thanh-toan")
    public ResponseEntity<ResponseObject<List<HoaDon>>> getHoaDonCho() {
        List<HoaDon> hoaDons = banHangTaiQuayService.getDanhSachHoaDonCho();
        return ResponseEntity.ok(new ResponseObject<>(hoaDons, "Lấy danh sách hóa đơn chờ thành công"));
    }

    @PostMapping("/hoa-don")
    public ResponseEntity<ResponseObject<HoaDon>> taoHoaDonCho(@RequestBody TaoHoaDonRequest request) {
        HoaDon hoaDon = banHangTaiQuayService.taoHoaDonCho(request.getNhanVienId(), request.getKhachHangId());
        return ResponseEntity.ok(new ResponseObject<>(hoaDon, "Tạo hóa đơn chờ thành công"));
    }

    @PostMapping("/hoa-don/{hoaDonId}/them-san-pham")
    public ResponseEntity<ResponseObject<HoaDon>> themSanPhamVaoHoaDon(@PathVariable UUID hoaDonId, @RequestBody ThemSanPhamRequest request) {
        HoaDon hoaDon = banHangTaiQuayService.themSanPhamVaoHoaDon(hoaDonId, request);
        return ResponseEntity.ok(new ResponseObject<>(hoaDon, "Thêm sản phẩm thành công"));
    }

    @DeleteMapping("/hoa-don-chi-tiet/{hoaDonChiTietId}")
    public ResponseEntity<ResponseObject<HoaDon>> xoaSanPhamKhoiHoaDon(@PathVariable UUID hoaDonChiTietId) {
        HoaDon hoaDon = banHangTaiQuayService.xoaSanPhamKhoiHoaDon(hoaDonChiTietId);
        return ResponseEntity.ok(new ResponseObject<>(hoaDon, "Xóa sản phẩm thành công"));
    }

    @PutMapping("/hoa-don/{hoaDonId}/huy")
    public ResponseEntity<ResponseObject<HoaDon>> huyHoaDon(@PathVariable UUID hoaDonId) {
        HoaDon hoaDon = banHangTaiQuayService.huyHoaDon(hoaDonId);
        return ResponseEntity.ok(new ResponseObject<>(hoaDon, "Hủy hóa đơn thành công"));
    }

    @PostMapping("/hoa-don/{hoaDonId}/thanh-toan")
    public ResponseEntity<ResponseObject<HoaDon>> thanhToan(@PathVariable UUID hoaDonId) {
        HoaDon hoaDon = banHangTaiQuayService.thanhToanHoaDon(hoaDonId);
        return ResponseEntity.ok(new ResponseObject<>(hoaDon, "Thanh toán hóa đơn thành công"));
    }
}
