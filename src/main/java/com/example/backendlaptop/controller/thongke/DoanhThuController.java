package com.example.backendlaptop.controller.thongke;

import com.example.backendlaptop.dto.thongke.*;
import com.example.backendlaptop.model.response.ResponseObject;
import com.example.backendlaptop.service.thongke.DoanhThuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller mới cho thống kê doanh thu theo nghiệp vụ CHÍNH XÁC
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/thongke")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DoanhThuController {
    
    private final DoanhThuService doanhThuService;
    
    /**
     * API 1: Lấy doanh thu chi tiết đầy đủ
     * GET /api/v1/thongke/doanh-thu-chi-tiet?startDate=2024-01-01&endDate=2024-01-31
     */
    @GetMapping("/doanh-thu-chi-tiet")
    public ResponseEntity<ResponseObject<DoanhThuChiTietDTO>> getDoanhThuChiTiet(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        log.info("📊 [DoanhThuController] Lấy doanh thu chi tiết từ {} đến {}", startDate, endDate);
        
        try {
            DoanhThuChiTietDTO result = doanhThuService.layDoanhThuChiTiet(startDate, endDate);
            return ResponseEntity.ok(new ResponseObject<>(result, "Lấy doanh thu chi tiết thành công"));
        } catch (Exception e) {
            log.error("❌ [DoanhThuController] Lỗi khi lấy doanh thu chi tiết", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseObject<>(false, null, "Lỗi: " + e.getMessage()));
        }
    }
    
    /**
     * API 2: Thống kê theo kênh bán hàng
     * GET /api/v1/thongke/theo-kenh?startDate=2024-01-01&endDate=2024-01-31
     */
    @GetMapping("/theo-kenh")
    public ResponseEntity<ResponseObject<List<ThongKeTheoKenhDTO>>> getThongKeTheoKenh(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        log.info("📊 [DoanhThuController] Thống kê theo kênh từ {} đến {}", startDate, endDate);
        
        try {
            List<ThongKeTheoKenhDTO> result = doanhThuService.thongKeTheoKenh(startDate, endDate);
            return ResponseEntity.ok(new ResponseObject<>(result, "Lấy thống kê theo kênh thành công"));
        } catch (Exception e) {
            log.error("❌ [DoanhThuController] Lỗi khi thống kê theo kênh", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseObject<>(false, null, "Lỗi: " + e.getMessage()));
        }
    }
    
    /**
     * API 3: Thống kê theo phương thức thanh toán
     * GET /api/v1/thongke/theo-phuong-thuc?startDate=2024-01-01&endDate=2024-01-31
     */
    @GetMapping("/theo-phuong-thuc")
    public ResponseEntity<ResponseObject<List<ThongKeTheoPhuongThucDTO>>> getThongKeTheoPhuongThuc(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        log.info("📊 [DoanhThuController] Thống kê theo phương thức từ {} đến {}", startDate, endDate);
        
        try {
            List<ThongKeTheoPhuongThucDTO> result = doanhThuService.thongKeTheoPhuongThuc(startDate, endDate);
            return ResponseEntity.ok(new ResponseObject<>(result, "Lấy thống kê theo phương thức thành công"));
        } catch (Exception e) {
            log.error("❌ [DoanhThuController] Lỗi khi thống kê theo phương thức", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseObject<>(false, null, "Lỗi: " + e.getMessage()));
        }
    }
    
    /**
     * API 4: Phân tích dòng tiền
     * GET /api/v1/thongke/dong-tien?startDate=2024-01-01&endDate=2024-01-31
     */
    @GetMapping("/dong-tien")
    public ResponseEntity<ResponseObject<DongTienDTO>> getDongTien(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        log.info("📊 [DoanhThuController] Phân tích dòng tiền từ {} đến {}", startDate, endDate);
        
        try {
            DongTienDTO result = doanhThuService.phanTichDongTien(startDate, endDate);
            return ResponseEntity.ok(new ResponseObject<>(result, "Lấy phân tích dòng tiền thành công"));
        } catch (Exception e) {
            log.error("❌ [DoanhThuController] Lỗi khi phân tích dòng tiền", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseObject<>(false, null, "Lỗi: " + e.getMessage()));
        }
    }
}
