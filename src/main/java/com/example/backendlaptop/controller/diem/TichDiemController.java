package com.example.backendlaptop.controller.diem;

import com.example.backendlaptop.model.request.diem.TichDiemRequest;
import com.example.backendlaptop.model.response.ResponseObject;
import com.example.backendlaptop.model.response.diem.LichSuDiemResponse;
import com.example.backendlaptop.model.response.diem.TichDiemResponse;
import com.example.backendlaptop.service.diem.TichDiemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/diem")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class TichDiemController {
    
    private final TichDiemService tichDiemService;
    
    /**
     * Lấy thông tin ví điểm của khách hàng đang đăng nhập
     * GET /api/v1/diem/tich-diem/me?userId={userId}
     */
    @GetMapping("/tich-diem/me")
    public ResponseEntity<ResponseObject<TichDiemResponse>> getTichDiemMe(
            @RequestParam UUID userId) {
        try {
            TichDiemResponse response = tichDiemService.getTichDiemByUserId(userId);
            return ResponseEntity.ok(new ResponseObject<>(response, "Lấy thông tin ví điểm thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ResponseObject<>(null, "Lỗi: " + e.getMessage()));
        }
    }
    
    /**
     * Lấy thông tin ví điểm theo userId (admin)
     * GET /api/v1/diem/tich-diem/{userId}
     */
    @GetMapping("/tich-diem/{userId}")
    public ResponseEntity<ResponseObject<TichDiemResponse>> getTichDiemByUserId(
            @PathVariable UUID userId) {
        try {
            TichDiemResponse response = tichDiemService.getTichDiemByUserId(userId);
            return ResponseEntity.ok(new ResponseObject<>(response, "Lấy thông tin ví điểm thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ResponseObject<>(null, "Lỗi: " + e.getMessage()));
        }
    }
    
    /**
     * Lấy lịch sử điểm của khách hàng đang đăng nhập (phân trang)
     * GET /api/v1/diem/lich-su/me?userId={userId}&page=0&size=10&loaiDiem=1
     */
    @GetMapping("/lich-su/me")
    public ResponseEntity<ResponseObject<Page<LichSuDiemResponse>>> getLichSuDiemMe(
            @RequestParam UUID userId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer loaiDiem) {
        try {
            Page<LichSuDiemResponse> response = tichDiemService.getLichSuDiemByUserId(userId, page, size);
            return ResponseEntity.ok(new ResponseObject<>(response, "Lấy lịch sử điểm thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ResponseObject<>(null, "Lỗi: " + e.getMessage()));
        }
    }
    
    /**
     * Lấy lịch sử điểm theo hóa đơn
     * GET /api/v1/diem/lich-su/hoa-don/{hoaDonId}
     */
    @GetMapping("/lich-su/hoa-don/{hoaDonId}")
    public ResponseEntity<ResponseObject<List<LichSuDiemResponse>>> getLichSuDiemByHoaDon(
            @PathVariable UUID hoaDonId) {
        try {
            List<LichSuDiemResponse> response = tichDiemService.getLichSuDiemByHoaDonId(hoaDonId);
            return ResponseEntity.ok(new ResponseObject<>(response, "Lấy lịch sử điểm thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ResponseObject<>(null, "Lỗi: " + e.getMessage()));
        }
    }
    
    /**
     * Thêm/bớt điểm thủ công (admin)
     * POST /api/v1/diem/tich-diem/manual
     */
    @PostMapping("/tich-diem/manual")
    public ResponseEntity<ResponseObject<String>> themBotDiemThuCong(
            @RequestBody TichDiemRequest request) {
        try {
            tichDiemService.themBotDiemThuCong(request);
            return ResponseEntity.ok(new ResponseObject<>("Thành công", "Thêm/bớt điểm thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ResponseObject<>(null, "Lỗi: " + e.getMessage()));
        }
    }
}

