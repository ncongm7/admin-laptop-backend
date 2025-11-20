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
            @PathVariable UUID idHoaDon,
            @Valid @RequestBody ThemSanPhamRequest request) {
        HoaDonResponse response = banHangTaiQuayFacade.themSanPhamVaoHoaDon(idHoaDon, request);
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
            @PathVariable UUID idHoaDon,
            @Valid @RequestBody ThanhToanRequest request) {
        HoaDonResponse response = banHangTaiQuayFacade.thanhToanHoaDon(idHoaDon, request);
        return ResponseEntity.ok(new ResponseObject<>(response, "Thanh toán hóa đơn thành công"));
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
    public ResponseEntity<ResponseObject<String>> xoaHoaDonCho(@PathVariable UUID idHoaDon) {
        banHangTaiQuayFacade.xoaHoaDonCho(idHoaDon);
        return ResponseEntity.ok(new ResponseObject<>("Xóa hóa đơn chờ thành công", "Xóa hóa đơn chờ thành công"));
    }

    /**
     * API: Cập nhật khách hàng cho hóa đơn (thêm ID khách hàng hoặc set null để khách lẻ)
     * Endpoint: PUT /api/v1/ban-hang/hoa-don/{idHoaDon}/khach-hang
     */
    @PutMapping("/hoa-don/{idHoaDon}/khach-hang")
    public ResponseEntity<ResponseObject<HoaDonResponse>> capNhatKhachHang(
            @PathVariable UUID idHoaDon,
            @RequestBody CapNhatKhachHangRequest request) {
        HoaDonResponse response = banHangTaiQuayFacade.capNhatKhachHang(idHoaDon, request);
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
            @PathVariable UUID idHoaDon) {
        List<VoucherSuggestionResponse> suggestions = banHangTaiQuayFacade.getVoucherSuggestions(idHoaDon);
        return ResponseEntity.ok(new ResponseObject<>(suggestions, "Lấy danh sách gợi ý voucher thành công"));
    }

    /**
     * API: Áp dụng Voucher/Phiếu giảm giá vào hóa đơn
     * Endpoint: POST /api/v1/ban-hang/hoa-don/{idHoaDon}/ap-dung-voucher
     */
    @PostMapping("/hoa-don/{idHoaDon}/ap-dung-voucher")
    public ResponseEntity<ResponseObject<HoaDonResponse>> apDungVoucher(
            @PathVariable UUID idHoaDon,
            @Valid @RequestBody ApDungVoucherRequest request) {
        HoaDonResponse response = banHangTaiQuayFacade.apDungVoucher(idHoaDon, request);
        return ResponseEntity.ok(new ResponseObject<>(response, "Áp dụng voucher thành công"));
    }

    /**
     * API: Xóa Voucher khỏi hóa đơn
     * Endpoint: DELETE /api/v1/ban-hang/hoa-don/{idHoaDon}/voucher
     */
    @DeleteMapping("/hoa-don/{idHoaDon}/voucher")
    public ResponseEntity<ResponseObject<HoaDonResponse>> xoaVoucher(@PathVariable UUID idHoaDon) {
        HoaDonResponse response = banHangTaiQuayFacade.xoaVoucher(idHoaDon);
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
     * API: In hóa đơn
     * Endpoint: GET /api/v1/ban-hang/hoa-don/{idHoaDon}/in
     * 
     * Trả về HTML invoice để in hoặc xuất PDF
     */
    @GetMapping("/hoa-don/{idHoaDon}/in")
    public ResponseEntity<String> inHoaDon(@PathVariable UUID idHoaDon) {
        String html = banHangTaiQuayFacade.inHoaDon(idHoaDon);
        return ResponseEntity.ok()
                .header("Content-Type", "text/html; charset=UTF-8")
                .body(html);
    }
}
