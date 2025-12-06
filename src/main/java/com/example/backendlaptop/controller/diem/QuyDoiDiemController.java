package com.example.backendlaptop.controller.diem;

import com.example.backendlaptop.model.request.diem.QuyDoiDiemRequest;
import com.example.backendlaptop.model.response.ResponseObject;
import com.example.backendlaptop.model.response.diem.QuyDoiDiemResponse;
import com.example.backendlaptop.service.diem.QuyDoiDiemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/diem/quy-doi")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class QuyDoiDiemController {
    
    private final QuyDoiDiemService quyDoiDiemService;
    
    /**
     * Lấy tất cả quy đổi điểm
     * GET /api/v1/diem/quy-doi/all
     */
    @GetMapping("/all")
    public ResponseEntity<ResponseObject<List<QuyDoiDiemResponse>>> getAllQuyDoiDiem() {
        try {
            List<QuyDoiDiemResponse> response = quyDoiDiemService.getAllQuyDoiDiem();
            return ResponseEntity.ok(new ResponseObject<>(response, "Lấy danh sách quy đổi điểm thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ResponseObject<>(null, "Lỗi: " + e.getMessage()));
        }
    }
    
    /**
     * Lấy quy đổi điểm đang hoạt động
     * GET /api/v1/diem/quy-doi/active
     */
    @GetMapping("/active")
    public ResponseEntity<ResponseObject<QuyDoiDiemResponse>> getQuyDoiDiemDangHoatDong() {
        try {
            QuyDoiDiemResponse response = quyDoiDiemService.getQuyDoiDiemDangHoatDong();
            if (response == null) {
                // Trả về 200 với data null và message thông báo
                return ResponseEntity.ok(new ResponseObject<>(null, "Hiện tại chưa có quy đổi điểm đang hoạt động"));
            }
            return ResponseEntity.ok(new ResponseObject<>(response, "Lấy quy đổi điểm đang hoạt động thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ResponseObject<>(null, "Lỗi: " + e.getMessage()));
        }
    }
    
    /**
     * Tạo quy đổi điểm mới (admin)
     * POST /api/v1/diem/quy-doi
     */
    @PostMapping
    public ResponseEntity<ResponseObject<QuyDoiDiemResponse>> createQuyDoiDiem(
            @RequestBody QuyDoiDiemRequest request) {
        try {
            QuyDoiDiemResponse response = quyDoiDiemService.createQuyDoiDiem(request);
            return ResponseEntity.ok(new ResponseObject<>(response, "Tạo quy đổi điểm thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ResponseObject<>(null, "Lỗi: " + e.getMessage()));
        }
    }
    
    /**
     * Cập nhật quy đổi điểm (admin)
     * PUT /api/v1/diem/quy-doi/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ResponseObject<QuyDoiDiemResponse>> updateQuyDoiDiem(
            @PathVariable UUID id,
            @RequestBody QuyDoiDiemRequest request) {
        try {
            QuyDoiDiemResponse response = quyDoiDiemService.updateQuyDoiDiem(id, request);
            return ResponseEntity.ok(new ResponseObject<>(response, "Cập nhật quy đổi điểm thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ResponseObject<>(null, "Lỗi: " + e.getMessage()));
        }
    }
    
    /**
     * Vô hiệu hóa quy đổi điểm (admin)
     * DELETE /api/v1/diem/quy-doi/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseObject<String>> deactivateQuyDoiDiem(
            @PathVariable UUID id) {
        try {
            quyDoiDiemService.deactivateQuyDoiDiem(id);
            return ResponseEntity.ok(new ResponseObject<>("Thành công", "Vô hiệu hóa quy đổi điểm thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ResponseObject<>(null, "Lỗi: " + e.getMessage()));
        }
    }
}

