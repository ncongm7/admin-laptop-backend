package com.example.backendlaptop.controller.banhang;

import com.example.backendlaptop.dto.banhang.*;
import com.example.backendlaptop.dto.banhang.CapNhatKhachHangRequest;
import com.example.backendlaptop.model.response.ResponseObject;
import com.example.backendlaptop.service.banhang.BanHangTaiQuayFacade;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ban-hang")
@CrossOrigin(origins = "*")
public class BanHangTaiQuayController {

    @Autowired
    private BanHangTaiQuayFacade banHangTaiQuayFacade;

    @PostMapping("/hoa-don/tao-moi")
    public ResponseEntity<ResponseObject<HoaDonResponse>> taoHoaDonMoi(@RequestBody TaoHoaDonRequest request) {
        HoaDonResponse response = banHangTaiQuayFacade.taoHoaDonChoiMoi(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseObject<>(response, "Tạo hóa đơn chờ thành công"));
    }

    @PostMapping("/hoa-don/{idHoaDon}/them-san-pham")
    public ResponseEntity<ResponseObject<HoaDonResponse>> themSanPhamVaoHoaDon(
            @PathVariable String idHoaDon,
            @Valid @RequestBody ThemSanPhamRequest request) {
        UUID hoaDonUUID = parseHoaDonId(idHoaDon);
        HoaDonResponse response = banHangTaiQuayFacade.themSanPhamVaoHoaDon(hoaDonUUID, request);
        return ResponseEntity.ok(new ResponseObject<>(response, "Thêm sản phẩm vào hóa đơn thành công"));
    }

    /**
     * API 3: Xóa Sản Phẩm Khỏi Hóa Đơn Chờ
     * Endpoint: DELETE /api/v1/ban-hang/hoa-don/xoa-san-pham/{idHoaDonChiTiet}
     */
    @DeleteMapping("/hoa-don/xoa-san-pham/{idHoaDonChiTiet}")
    public ResponseEntity<ResponseObject<HoaDonResponse>> xoaSanPhamKhoiHoaDon(@PathVariable UUID idHoaDonChiTiet) {
        HoaDonResponse response = banHangTaiQuayFacade.xoaSanPhamKhoiHoaDon(idHoaDonChiTiet);
        return ResponseEntity.ok(new ResponseObject<>(response, "Xóa sản phẩm khỏi hóa đơn thành công"));
    }

    /**
     * API 4: Hoàn Tất Thanh Toán Hóa Đơn
     * Endpoint: POST /api/v1/ban-hang/hoa-don/{idHoaDon}/thanh-toan
     */
    @PostMapping("/hoa-don/{idHoaDon}/thanh-toan")
    public ResponseEntity<ResponseObject<HoaDonResponse>> thanhToanHoaDon(
            @PathVariable String idHoaDon,
            @Valid @RequestBody ThanhToanRequest request) {
        UUID hoaDonUUID = parseHoaDonId(idHoaDon);
        HoaDonResponse response = banHangTaiQuayFacade.thanhToanHoaDon(hoaDonUUID, request);
        return ResponseEntity.ok(new ResponseObject<>(response, "Thanh toán hóa đơn thành công"));
    }

    /**
     * API: Thanh toán COD (Cash on Delivery) khi giao hàng thành công
     */
    @PostMapping("/hoa-don/{idHoaDon}/thanh-toan-cod")
    public ResponseEntity<ResponseObject<HoaDonResponse>> thanhToanCOD(
            @PathVariable String idHoaDon,
            @RequestBody java.util.Map<String, Object> request) {
        UUID hoaDonUUID = parseHoaDonId(idHoaDon);
        
        // Lấy số tiền khách đưa từ request
        Object tienKhachDuaObj = request.get("tienKhachDua");
        if (tienKhachDuaObj == null) {
            return ResponseEntity.badRequest()
                    .body(new ResponseObject<>(null, "Số tiền khách đưa không được để trống!"));
        }
        
        java.math.BigDecimal tienKhachDua;
        try {
            if (tienKhachDuaObj instanceof Number) {
                tienKhachDua = java.math.BigDecimal.valueOf(((Number) tienKhachDuaObj).doubleValue());
            } else {
                tienKhachDua = new java.math.BigDecimal(tienKhachDuaObj.toString());
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ResponseObject<>(null, "Số tiền khách đưa không hợp lệ!"));
        }
        
        HoaDonResponse response = banHangTaiQuayFacade.thanhToanCOD(hoaDonUUID, tienKhachDua);
        return ResponseEntity.ok(new ResponseObject<>(response, "Thanh toán COD thành công!"));
    }

    /**
     * API 5: Lấy Danh Sách Hóa Đơn Chờ
     * Endpoint: GET /api/v1/ban-hang/hoa-don/cho
     */
    @GetMapping("/hoa-don/cho")
    public ResponseEntity<ResponseObject<List<HoaDonResponse>>> getDanhSachHoaDonCho() {
        List<HoaDonResponse> response = banHangTaiQuayFacade.getDanhSachHoaDonCho();
        return ResponseEntity.ok(new ResponseObject<>(response, "Lấy danh sách hóa đơn chờ thành công"));
    }

    /**
     * API 6: Xác Thực Serial Number
     * Endpoint: POST /api/v1/ban-hang/hoa-don/xac-thuc-serial
     * 
     * Validate serial number trước khi thanh toán
     */
    @PostMapping("/hoa-don/xac-thuc-serial")
    public ResponseEntity<ResponseObject<XacThucSerialResponse>> xacThucSerial(
            @Valid @RequestBody XacThucSerialRequest request) {
        XacThucSerialResponse response = banHangTaiQuayFacade.xacThucSerial(request);
        
        if (response.isValid()) {
            return ResponseEntity.ok(new ResponseObject<>(response, "Serial hợp lệ"));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseObject<>(false, response, response.getMessage()));
        }
    }

    /**
     * API 7: Xóa Hóa Đơn Chờ
     * Endpoint: DELETE /api/v1/ban-hang/hoa-don/{idHoaDon}
     */
    @DeleteMapping("/hoa-don/{idHoaDon}")
    public ResponseEntity<ResponseObject<String>> xoaHoaDonCho(@PathVariable String idHoaDon) {
        UUID hoaDonUUID = parseHoaDonId(idHoaDon);
        banHangTaiQuayFacade.xoaHoaDonCho(hoaDonUUID);
        return ResponseEntity.ok(new ResponseObject<>("Xóa hóa đơn chờ thành công", "Xóa hóa đơn chờ thành công"));
    }

    /**
     * API: Cập nhật khách hàng cho hóa đơn (thêm ID khách hàng hoặc set null để khách lẻ)
     * Endpoint: PUT /api/v1/ban-hang/hoa-don/{idHoaDon}/khach-hang
     */
    @PutMapping("/hoa-don/{idHoaDon}/khach-hang")
    public ResponseEntity<ResponseObject<HoaDonResponse>> capNhatKhachHang(
            @PathVariable String idHoaDon,
            @RequestBody CapNhatKhachHangRequest request) {
        UUID hoaDonUUID = parseHoaDonId(idHoaDon);
        HoaDonResponse response = banHangTaiQuayFacade.capNhatKhachHang(hoaDonUUID, request);
        return ResponseEntity.ok(new ResponseObject<>(response, "Cập nhật khách hàng cho hóa đơn thành công"));
    }

    /**
     * API: Gợi ý Voucher/Khuyến mãi cho hóa đơn hiện tại
     * Endpoint: GET /api/v1/ban-hang/hoa-don/{idHoaDon}/goi-y-voucher
     * 
     * Trả về danh sách các voucher/khuyến mãi hợp lệ có thể áp dụng cho hóa đơn này
     */
    @GetMapping("/hoa-don/{idHoaDon}/goi-y-voucher")
    public ResponseEntity<ResponseObject<List<VoucherSuggestionResponse>>> getVoucherSuggestions(
            @PathVariable String idHoaDon) {
        UUID hoaDonUUID = parseHoaDonId(idHoaDon);
        List<VoucherSuggestionResponse> suggestions = banHangTaiQuayFacade.getVoucherSuggestions(hoaDonUUID);
        return ResponseEntity.ok(new ResponseObject<>(suggestions, "Lấy danh sách gợi ý voucher thành công"));
    }

    /**
     * API: Áp dụng Voucher/Phiếu giảm giá vào hóa đơn
     * Endpoint: POST /api/v1/ban-hang/hoa-don/{idHoaDon}/ap-dung-voucher
     */
    @PostMapping("/hoa-don/{idHoaDon}/ap-dung-voucher")
    public ResponseEntity<ResponseObject<HoaDonResponse>> apDungVoucher(
            @PathVariable String idHoaDon,
            @Valid @RequestBody ApDungVoucherRequest request) {
        UUID hoaDonUUID = parseHoaDonId(idHoaDon);
        HoaDonResponse response = banHangTaiQuayFacade.apDungVoucher(hoaDonUUID, request);
        return ResponseEntity.ok(new ResponseObject<>(response, "Áp dụng voucher thành công"));
    }

    /**
     * API: Xóa Voucher khỏi hóa đơn
     * Endpoint: DELETE /api/v1/ban-hang/hoa-don/{idHoaDon}/voucher
     */
    @DeleteMapping("/hoa-don/{idHoaDon}/voucher")
    public ResponseEntity<ResponseObject<HoaDonResponse>> xoaVoucher(@PathVariable String idHoaDon) {
        UUID hoaDonUUID = parseHoaDonId(idHoaDon);
        HoaDonResponse response = banHangTaiQuayFacade.xoaVoucher(hoaDonUUID);
        return ResponseEntity.ok(new ResponseObject<>(response, "Xóa voucher thành công"));
    }

    /**
     * API: Cập nhật số lượng sản phẩm trong hóa đơn
     * Endpoint: PUT /api/v1/ban-hang/hoa-don/cap-nhat-so-luong/{idHoaDonChiTiet}
     */
    @PutMapping("/hoa-don/cap-nhat-so-luong/{idHoaDonChiTiet}")
    public ResponseEntity<ResponseObject<HoaDonResponse>> capNhatSoLuongSanPham(
            @PathVariable UUID idHoaDonChiTiet,
            @Valid @RequestBody CapNhatSoLuongRequest request) {
        HoaDonResponse response = banHangTaiQuayFacade.capNhatSoLuongSanPham(idHoaDonChiTiet, request.getSoLuong());
        return ResponseEntity.ok(new ResponseObject<>(response, "Cập nhật số lượng sản phẩm thành công"));
    }

    /**
     * API: Kiểm tra và cập nhật giá sản phẩm trước khi thanh toán
     * Endpoint: POST /api/v1/ban-hang/hoa-don/{idHoaDon}/kiem-tra-cap-nhat-gia
     * 
     * Kiểm tra giá sản phẩm có thay đổi không (do đợt giảm giá)
     * Nếu có thay đổi, tự động cập nhật giá và trả về thông tin thay đổi
     */
    @PostMapping("/hoa-don/{idHoaDon}/kiem-tra-cap-nhat-gia")
    public ResponseEntity<ResponseObject<com.example.backendlaptop.dto.banhang.CapNhatGiaResponse>> kiemTraVaCapNhatGia(
            @PathVariable String idHoaDon) {
        UUID hoaDonUUID = parseHoaDonId(idHoaDon);
        com.example.backendlaptop.dto.banhang.CapNhatGiaResponse response = banHangTaiQuayFacade.kiemTraVaCapNhatGia(hoaDonUUID);
        
        String message = response.isCoThayDoi() 
            ? String.format("Đã cập nhật giá của %d sản phẩm", response.getSoSanPhamThayDoi())
            : "Giá sản phẩm không có thay đổi";
        
        return ResponseEntity.ok(new ResponseObject<>(response, message));
    }

    /**
     * API: Kiểm tra toàn bộ (giá, voucher, điểm) trước khi xác nhận thanh toán
     * Endpoint: POST /api/v1/ban-hang/hoa-don/{idHoaDon}/kiem-tra-truoc-thanh-toan
     * 
     * Kiểm tra giá sản phẩm, voucher, và điểm tích lũy có thay đổi không
     * Nếu có thay đổi, tự động cập nhật hóa đơn và trả về thông tin thay đổi
     * Frontend sẽ hiển thị thông báo và yêu cầu người dùng xác nhận lại
     */
    @PostMapping("/hoa-don/{idHoaDon}/kiem-tra-truoc-thanh-toan")
    public ResponseEntity<ResponseObject<com.example.backendlaptop.dto.banhang.KiemTraTruocThanhToanResponse>> kiemTraTruocThanhToan(
            @PathVariable String idHoaDon) {
        UUID hoaDonUUID = parseHoaDonId(idHoaDon);
        com.example.backendlaptop.dto.banhang.KiemTraTruocThanhToanResponse response = banHangTaiQuayFacade.kiemTraTruocThanhToan(hoaDonUUID);
        return ResponseEntity.ok(new ResponseObject<>(response, response.getMessage()));
    }

    /**
     * API: In hóa đơn
     * Endpoint: GET /api/v1/ban-hang/hoa-don/{idHoaDon}/in
     * 
     * Trả về HTML invoice để in hoặc xuất PDF
     */
    @GetMapping("/hoa-don/{idHoaDon}/in")
    public ResponseEntity<String> inHoaDon(@PathVariable String idHoaDon) {
        UUID hoaDonUUID = parseHoaDonId(idHoaDon);
        String html = banHangTaiQuayFacade.inHoaDon(hoaDonUUID);
        return ResponseEntity.ok()
                .header("Content-Type", "text/html; charset=UTF-8")
                .body(html);
    }

    /**
     * Helper method: Parse hóa đơn ID từ String sang UUID
     * Cho phép cả TEMP ID và UUID thật
     */
    private UUID parseHoaDonId(String idHoaDon) {
        // Nếu là TEMP ID (bắt đầu bằng "TEMP_"), tìm hóa đơn theo mã
        if (idHoaDon != null && idHoaDon.startsWith("TEMP_")) {
            throw new IllegalArgumentException("TEMP ID không được hỗ trợ cho endpoint này. Vui lòng tạo hóa đơn thực trước.");
        }
        
        // Parse UUID
        try {
            return UUID.fromString(idHoaDon);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid hóa đơn ID: " + idHoaDon);
        }
    }
}
