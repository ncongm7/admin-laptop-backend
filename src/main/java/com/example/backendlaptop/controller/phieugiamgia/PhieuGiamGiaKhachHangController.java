// FILE: src/main/java/com/example/backendlaptop/controller/phieugiamgia/PhieuGiamGiaKhachHangController.java
package com.example.backendlaptop.controller.phieugiamgia;

import com.example.backendlaptop.model.request.phieugiamgia.GanPhieuGiamGiaChoKhachHangRequest;
import com.example.backendlaptop.model.request.phieugiamgia.KiemTraPhieuGiamGiaRequest;
import com.example.backendlaptop.model.response.ResponseObject;
import com.example.backendlaptop.model.response.phieugiamgia.KhachHangPhieuGiamGiaResponse;
import com.example.backendlaptop.model.response.phieugiamgia.KiemTraPhieuGiamGiaResponse;
import com.example.backendlaptop.model.response.phieugiamgia.PhieuGiamGiaResponse;
import com.example.backendlaptop.service.phieugiamgia.PhieuGiamGiaKhachHangService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/phieu-giam-gia-quan-ly")
@CrossOrigin(origins = "*")
public class PhieuGiamGiaKhachHangController {
    
    @Autowired
    private PhieuGiamGiaKhachHangService service;
    
    @PostMapping("/gan-cho-khach-hang")
    public ResponseObject<?> ganPhieuGiamGiaChoKhachHang(@Valid @RequestBody GanPhieuGiamGiaChoKhachHangRequest request) {
        service.ganPhieuGiamGiaChoKhachHang(request);
        return new ResponseObject<>(null, "Gán phiếu giảm giá cho khách hàng thành công");
    }
    
    @GetMapping("/khach-hang/{khachHangId}/danh-sach")
    public ResponseObject<List<PhieuGiamGiaResponse>> getPhieuGiamGiaCuaKhachHang(@PathVariable UUID khachHangId) {
        return new ResponseObject<>(service.getPhieuGiamGiaCuaKhachHang(khachHangId));
    }
    
    @PostMapping("/kiem-tra-va-ap-dung")
    public ResponseObject<KiemTraPhieuGiamGiaResponse> kiemTraVaApDungPhieuGiamGia(@Valid @RequestBody KiemTraPhieuGiamGiaRequest request) {
        return new ResponseObject<>(service.kiemTraVaApDungPhieuGiamGia(request));
    }
    
    @GetMapping("/{phieuGiamGiaId}/khach-hang")
    public ResponseObject<List<KhachHangPhieuGiamGiaResponse>> getKhachHangCuaPhieuGiamGia(@PathVariable UUID phieuGiamGiaId) {
        return new ResponseObject<>(service.getKhachHangCuaPhieuGiamGia(phieuGiamGiaId));
    }
    
    @PutMapping("/{phieuGiamGiaId}/khach-hang")
    public ResponseObject<?> capNhatKhachHangChoPhieuGiamGia(
            @PathVariable UUID phieuGiamGiaId,
            @Valid @RequestBody GanPhieuGiamGiaChoKhachHangRequest request) {
        request.setPhieuGiamGiaId(phieuGiamGiaId);
        service.capNhatKhachHangChoPhieuGiamGia(phieuGiamGiaId, request.getKhachHangIds());
        return new ResponseObject<>(null, "Cập nhật khách hàng cho phiếu giảm giá thành công");
    }
}

