package com.example.backendlaptop.controller.thongke;

import com.example.backendlaptop.dto.thongke.*;
import com.example.backendlaptop.model.response.ResponseObject;
import com.example.backendlaptop.service.thongke.ThongKeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller cho Dashboard thống kê
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/thongke")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ThongKeController {
    
    private final ThongKeService thongKeService;
    
    /**
     * API 1: Lấy thống kê tổng quan cho Dashboard
     * GET /api/v1/thongke/tong-quan?startDate=2024-01-01&endDate=2024-01-31
     */
    @GetMapping("/tong-quan")
    public ResponseEntity<ResponseObject<ThongKeTongQuanResponse>> getThongKeTongQuan(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        log.info("📊 [ThongKeController] Nhận yêu cầu thống kê tổng quan từ {} đến {}", startDate, endDate);
        
        try {
            ThongKeTongQuanResponse response = thongKeService.getThongKeTongQuan(startDate, endDate);
            
            return ResponseEntity.ok(new ResponseObject<>(response, "Lấy thống kê tổng quan thành công"));
        } catch (Exception e) {
            log.error("❌ [ThongKeController] Lỗi khi lấy thống kê tổng quan", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseObject<>(false, null, "Không thể lấy thống kê tổng quan: " + e.getMessage()));
        }
    }
    
    /**
     * API 2: Lấy dữ liệu biểu đồ doanh số
     * GET /api/v1/thongke/bieu-do-doanh-so?startDate=2024-01-01&endDate=2024-01-31&groupBy=day
     */
    @GetMapping("/bieu-do-doanh-so")
    public ResponseEntity<ResponseObject<List<BieuDoDoanhSoResponse>>> getBieuDoDoanhSo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "day") String groupBy
    ) {
        log.info("📊 [ThongKeController] Nhận yêu cầu biểu đồ doanh số, groupBy: {}", groupBy);
        
        try {
            List<BieuDoDoanhSoResponse> response = thongKeService.getBieuDoDoanhSo(startDate, endDate, groupBy);
            
            return ResponseEntity.ok(new ResponseObject<>(response, "Lấy dữ liệu biểu đồ thành công"));
        } catch (Exception e) {
            log.error("❌ [ThongKeController] Lỗi khi lấy dữ liệu biểu đồ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseObject<>(false, null, "Không thể lấy dữ liệu biểu đồ: " + e.getMessage()));
        }
    }
    
    /**
     * API 3: Lấy top sản phẩm bán chạy
     * GET /api/v1/thongke/san-pham-ban-chay?startDate=2024-01-01&endDate=2024-01-31&limit=5
     */
    @GetMapping("/san-pham-ban-chay")
    public ResponseEntity<ResponseObject<List<SanPhamBanChayResponse>>> getSanPhamBanChay(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "5") Integer limit
    ) {
        log.info("📊 [ThongKeController] Nhận yêu cầu top {} sản phẩm bán chạy", limit);
        
        try {
            List<SanPhamBanChayResponse> response = thongKeService.getSanPhamBanChay(startDate, endDate, limit);
            
            return ResponseEntity.ok(new ResponseObject<>(response, "Lấy danh sách sản phẩm bán chạy thành công"));
        } catch (Exception e) {
            log.error("❌ [ThongKeController] Lỗi khi lấy sản phẩm bán chạy", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseObject<>(false, null, "Không thể lấy danh sách sản phẩm bán chạy: " + e.getMessage()));
        }
    }
    
    /**
     * API 4: Lấy giao dịch gần đây
     * GET /api/v1/thongke/giao-dich-gan-day?limit=10
     */
    @GetMapping("/giao-dich-gan-day")
    public ResponseEntity<ResponseObject<List<GiaoDichGanDayResponse>>> getGiaoDichGanDay(
            @RequestParam(defaultValue = "10") Integer limit
    ) {
        log.info("📊 [ThongKeController] Nhận yêu cầu {} giao dịch gần đây", limit);
        
        try {
            List<GiaoDichGanDayResponse> response = thongKeService.getGiaoDichGanDay(limit);
            
            return ResponseEntity.ok(new ResponseObject<>(response, "Lấy danh sách giao dịch gần đây thành công"));
        } catch (Exception e) {
            log.error("❌ [ThongKeController] Lỗi khi lấy giao dịch gần đây", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseObject<>(false, null, "Không thể lấy danh sách giao dịch gần đây: " + e.getMessage()));
        }
    }
    
    /**
     * API 5: Lấy hoạt động khách hàng gần đây
     * GET /api/v1/thongke/hoat-dong-khach-hang?limit=10
     */
    @GetMapping("/hoat-dong-khach-hang")
    public ResponseEntity<ResponseObject<List<HoatDongKhachHangResponse>>> getHoatDongKhachHang(
            @RequestParam(defaultValue = "10") Integer limit
    ) {
        log.info("📊 [ThongKeController] Nhận yêu cầu {} hoạt động khách hàng gần đây", limit);
        
        try {
            List<HoatDongKhachHangResponse> response = thongKeService.getHoatDongKhachHang(limit);
            
            return ResponseEntity.ok(new ResponseObject<>(response, "Lấy danh sách hoạt động khách hàng thành công"));
        } catch (Exception e) {
            log.error("❌ [ThongKeController] Lỗi khi lấy hoạt động khách hàng", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseObject<>(false, null, "Không thể lấy danh sách hoạt động khách hàng: " + e.getMessage()));
        }
    }
}

