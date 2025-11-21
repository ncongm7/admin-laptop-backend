package com.example.backendlaptop.controller.hoadon;

import com.example.backendlaptop.dto.hoadon.HoaDonDetailResponse;
import com.example.backendlaptop.dto.hoadon.HoaDonListResponse;
import com.example.backendlaptop.dto.hoadon.HoaDonSearchRequest;
import com.example.backendlaptop.model.response.ResponseObject;
import com.example.backendlaptop.service.hoadon.HoaDonService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Controller quản lý hóa đơn
 * Endpoints cho trang "Quản lý đơn hàng"
 */
@RestController
@RequestMapping("/api/v1/hoa-don")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class HoaDonController {

    private final HoaDonService hoaDonService;

    /**
     * API 1: Lấy danh sách hóa đơn (có phân trang, tìm kiếm, lọc)
     * 
     * GET /api/v1/hoa-don
     * 
     * Query Params:
     * - page: Số trang (default: 0)
     * - size: Số lượng/trang (default: 10)
     * - keyword: Tìm theo mã HĐ, tên KH, SĐT
     * - trangThai: Lọc theo trạng thái (0-4)
     * - loaiHoaDon: Lọc theo loại (0: Tại quầy, 1: Online)
     * - trangThaiThanhToan: Lọc theo TT thanh toán (0: Chưa, 1: Đã)
     * - startDate: Từ ngày (yyyy-MM-dd)
     * - endDate: Đến ngày (yyyy-MM-dd)
     */
    @GetMapping
    public ResponseEntity<ResponseObject<Page<HoaDonListResponse>>> getDanhSachHoaDon(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer trangThai,
            @RequestParam(required = false) Integer loaiHoaDon,
            @RequestParam(required = false) Integer trangThaiThanhToan,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        // Build request object
        HoaDonSearchRequest request = new HoaDonSearchRequest();
        request.setPage(page);
        request.setSize(size);
        request.setKeyword(keyword);
        request.setTrangThai(trangThai);
        request.setLoaiHoaDon(loaiHoaDon);
        request.setTrangThaiThanhToan(trangThaiThanhToan);
        request.setStartDate(startDate);
        request.setEndDate(endDate);

        Page<HoaDonListResponse> result = hoaDonService.searchHoaDon(request);

        return ResponseEntity.ok(new ResponseObject<>(result, "Lấy danh sách hóa đơn thành công"));
    }

    /**
     * API 2: Lấy chi tiết một hóa đơn
     * 
     * GET /api/v1/hoa-don/{idHoaDon}
     */
    @GetMapping("/{idHoaDon}")
    public ResponseEntity<ResponseObject<HoaDonDetailResponse>> getChiTietHoaDon(
            @PathVariable UUID idHoaDon
    ) {
        HoaDonDetailResponse result = hoaDonService.getHoaDonDetail(idHoaDon);
        return ResponseEntity.ok(new ResponseObject<>(result, "Lấy chi tiết hóa đơn thành công"));
    }

    /**
     * API 3: Cập nhật trạng thái đơn hàng
     * 
     * PUT /api/v1/hoa-don/{idHoaDon}/status?trangThai=0
     */
    @PutMapping("/{idHoaDon}/status")
    public ResponseEntity<ResponseObject<HoaDonDetailResponse>> capNhatTrangThai(
            @PathVariable UUID idHoaDon,
            @RequestParam Integer trangThai
    ) {
        HoaDonDetailResponse result = hoaDonService.capNhatTrangThai(idHoaDon, trangThai);
        return ResponseEntity.ok(new ResponseObject<>(result, "Cập nhật trạng thái đơn hàng thành công"));
    }

    /**
     * API 4: Xác nhận đơn hàng online (admin)
     * 
     * PUT /api/v1/hoa-don/{idHoaDon}/xac-nhan?nhanVienId={nhanVienId}
     * 
     * Chức năng:
     * - Xác nhận đơn hàng online
     * - Trừ kho (Serial) khi xác nhận
     * - Chỉ áp dụng cho đơn hàng online ở trạng thái CHO_THANH_TOAN
     */
    @PutMapping("/{idHoaDon}/xac-nhan")
    public ResponseEntity<ResponseObject<HoaDonDetailResponse>> xacNhanDonHang(
            @PathVariable UUID idHoaDon,
            @RequestParam(required = false) UUID nhanVienId
    ) {
        HoaDonDetailResponse result = hoaDonService.xacNhanDonHangOnline(idHoaDon, nhanVienId);
        return ResponseEntity.ok(new ResponseObject<>(result, "Xác nhận đơn hàng thành công"));
    }

    /**
     * API 5: Hủy đơn hàng online (admin)
     * 
     * PUT /api/v1/hoa-don/{idHoaDon}/huy?nhanVienId={nhanVienId}
     * 
     * Chức năng:
     * - Hủy đơn hàng online
     * - Chỉ hủy được khi trạng thái = CHO_THANH_TOAN (chưa trừ kho)
     */
    @PutMapping("/{idHoaDon}/huy")
    public ResponseEntity<ResponseObject<HoaDonDetailResponse>> huyDonHang(
            @PathVariable UUID idHoaDon,
            @RequestParam(required = false) UUID nhanVienId
    ) {
        HoaDonDetailResponse result = hoaDonService.huyDonHangOnline(idHoaDon, nhanVienId);
        return ResponseEntity.ok(new ResponseObject<>(result, "Hủy đơn hàng thành công"));
    }

    /**
     * API 6: Lấy danh sách đơn hàng online chờ xác nhận
     * 
     * GET /api/v1/hoa-don/pending-online
     * 
     * Chức năng:
     * - Lấy danh sách đơn hàng online (loai_hoa_don = 1) ở trạng thái chờ xác nhận (trang_thai = 0)
     * - Dùng cho Pending Order Ticker component
     */
    @GetMapping("/pending-online")
    public ResponseEntity<ResponseObject<java.util.List<com.example.backendlaptop.dto.hoadon.PendingOrderResponse>>> getPendingOnlineOrders() {
        java.util.List<com.example.backendlaptop.dto.hoadon.PendingOrderResponse> result = hoaDonService.getPendingOnlineOrders();
        return ResponseEntity.ok(new ResponseObject<>(result, "Lấy danh sách đơn hàng chờ xác nhận thành công"));
    }

    /**
     * API 7: Lấy số lượng hóa đơn theo từng trạng thái
     * 
     * GET /api/v1/hoa-don/status-counts
     * 
     * Chức năng:
     * - Đếm số lượng hóa đơn theo từng trạng thái
     * - Dùng cho hiển thị badge counts trên UI
     */
    @GetMapping("/status-counts")
    public ResponseEntity<ResponseObject<com.example.backendlaptop.dto.hoadon.StatusCountResponse>> getStatusCounts() {
        com.example.backendlaptop.dto.hoadon.StatusCountResponse result = hoaDonService.getStatusCounts();
        return ResponseEntity.ok(new ResponseObject<>(result, "Lấy số lượng hóa đơn theo trạng thái thành công"));
    }
}

