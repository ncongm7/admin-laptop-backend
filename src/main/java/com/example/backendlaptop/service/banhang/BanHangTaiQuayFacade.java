package com.example.backendlaptop.service.banhang;

import com.example.backendlaptop.dto.banhang.*;
import com.example.backendlaptop.entity.HoaDon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Facade Pattern: Lớp điều phối chính cho chức năng bán hàng tại quầy
 * 
 * Mục đích: 
 * - Đơn giản hóa interface cho Controller
 * - Điều phối các service con (HoaDonService, SanPhamTrongHoaDonService, etc.)
 * - Không chứa logic nghiệp vụ phức tạp, chỉ gọi đến các service con
 * 
 * Tất cả các endpoint của Controller sẽ gọi đến Facade này,
 * và Facade sẽ phân phối công việc đến các service chuyên biệt tương ứng.
 */
@Service
public class BanHangTaiQuayFacade {

    @Autowired
    private BanHangHoaDonService hoaDonService;

    @Autowired
    private SanPhamTrongHoaDonService sanPhamTrongHoaDonService;

    @Autowired
    private KhuyenMaiService khuyenMaiService;

    @Autowired
    private ThanhToanService thanhToanService;

    @Autowired
    private InHoaDonService inHoaDonService;

    // ==================== QUẢN LÝ HÓA ĐƠN ====================

    /**
     * API 1: Tạo Hóa Đơn Chờ Mới
     * Endpoint: POST /api/v1/ban-hang/hoa-don/tao-moi
     */
    public HoaDonResponse taoHoaDonChoiMoi(TaoHoaDonRequest request) {
        return hoaDonService.taoHoaDonChoMoi(request);
    }

    /**
     * API 5: Lấy Danh Sách Hóa Đơn Chờ
     * Endpoint: GET /api/v1/ban-hang/hoa-don/cho
     */
    public List<HoaDonResponse> getDanhSachHoaDonCho() {
        return hoaDonService.getDanhSachHoaDonCho();
    }

    /**
     * API 7: Xóa Hóa Đơn Chờ
     * Endpoint: DELETE /api/v1/ban-hang/hoa-don/{idHoaDon}
     */
    public void xoaHoaDonCho(UUID idHoaDon) {
        // Cần giải phóng tồn kho tạm giữ trước khi xóa
        // Lấy hóa đơn trước để có thông tin
        HoaDon hoaDon = hoaDonService.findById(idHoaDon);
        
        // Giải phóng tồn kho tạm giữ
        sanPhamTrongHoaDonService.giaiPhongTonKhoTamGiu(hoaDon);
        
        // Xóa hóa đơn
        hoaDonService.xoaHoaDonCho(idHoaDon);
    }

    /**
     * API: Cập nhật khách hàng cho hóa đơn
     * Endpoint: PUT /api/v1/ban-hang/hoa-don/{idHoaDon}/khach-hang
     */
    public HoaDonResponse capNhatKhachHang(UUID idHoaDon, CapNhatKhachHangRequest request) {
        return hoaDonService.capNhatKhachHang(idHoaDon, request);
    }

    // ==================== QUẢN LÝ SẢN PHẨM TRONG HÓA ĐƠN ====================

    /**
     * API 2: Thêm Sản Phẩm Vào Hóa Đơn Chờ
     * Endpoint: POST /api/v1/ban-hang/hoa-don/{idHoaDon}/them-san-pham
     */
    public HoaDonResponse themSanPhamVaoHoaDon(UUID idHoaDon, ThemSanPhamRequest request) {
        return sanPhamTrongHoaDonService.themSanPhamVaoHoaDon(idHoaDon, request);
    }

    /**
     * API 3: Xóa Sản Phẩm Khỏi Hóa Đơn Chờ
     * Endpoint: DELETE /api/v1/ban-hang/hoa-don/xoa-san-pham/{idHoaDonChiTiet}
     */
    public HoaDonResponse xoaSanPhamKhoiHoaDon(UUID idHoaDonChiTiet) {
        return sanPhamTrongHoaDonService.xoaSanPhamKhoiHoaDon(idHoaDonChiTiet);
    }

    /**
     * API: Cập nhật số lượng sản phẩm trong hóa đơn
     * Endpoint: PUT /api/v1/ban-hang/hoa-don/cap-nhat-so-luong/{idHoaDonChiTiet}
     */
    public HoaDonResponse capNhatSoLuongSanPham(UUID idHoaDonChiTiet, Integer soLuong) {
        return sanPhamTrongHoaDonService.capNhatSoLuongSanPham(idHoaDonChiTiet, soLuong);
    }

    // ==================== QUẢN LÝ KHUYẾN MÃI/VOUCHER ====================

    /**
     * API: Gợi ý Voucher/Khuyến mãi cho hóa đơn hiện tại
     * Endpoint: GET /api/v1/ban-hang/hoa-don/{idHoaDon}/goi-y-voucher
     */
    public List<VoucherSuggestionResponse> getVoucherSuggestions(UUID idHoaDon) {
        return khuyenMaiService.getVoucherSuggestions(idHoaDon);
    }

    /**
     * API: Áp dụng Voucher/Phiếu giảm giá vào hóa đơn
     * Endpoint: POST /api/v1/ban-hang/hoa-don/{idHoaDon}/ap-dung-voucher
     */
    public HoaDonResponse apDungVoucher(UUID idHoaDon, ApDungVoucherRequest request) {
        return khuyenMaiService.apDungVoucher(idHoaDon, request);
    }

    /**
     * API: Xóa Voucher khỏi hóa đơn
     * Endpoint: DELETE /api/v1/ban-hang/hoa-don/{idHoaDon}/voucher
     */
    public HoaDonResponse xoaVoucher(UUID idHoaDon) {
        return khuyenMaiService.xoaVoucher(idHoaDon);
    }

    // ==================== QUẢN LÝ THANH TOÁN ====================

    /**
     * API 4: Hoàn Tất Thanh Toán Hóa Đơn
     * Endpoint: POST /api/v1/ban-hang/hoa-don/{idHoaDon}/thanh-toan
     */
    public HoaDonResponse thanhToanHoaDon(UUID idHoaDon, ThanhToanRequest request) {
        return thanhToanService.thanhToanHoaDon(idHoaDon, request);
    }

    /**
     * API: Thanh toán COD (Cash on Delivery) khi giao hàng thành công
     * Endpoint: POST /api/v1/ban-hang/hoa-don/{idHoaDon}/thanh-toan-cod
     */
    public HoaDonResponse thanhToanCOD(UUID idHoaDon, java.math.BigDecimal tienKhachDua) {
        return thanhToanService.thanhToanCOD(idHoaDon, tienKhachDua);
    }

    /**
     * API: Kiểm tra và cập nhật giá sản phẩm trước khi thanh toán
     * Endpoint: POST /api/v1/ban-hang/hoa-don/{idHoaDon}/kiem-tra-cap-nhat-gia
     */
    public com.example.backendlaptop.dto.banhang.CapNhatGiaResponse kiemTraVaCapNhatGia(UUID idHoaDon) {
        return thanhToanService.kiemTraVaCapNhatGia(idHoaDon);
    }

    /**
     * API: Kiểm tra toàn bộ (giá, voucher, điểm) trước khi xác nhận thanh toán
     * Endpoint: POST /api/v1/ban-hang/hoa-don/{idHoaDon}/kiem-tra-truoc-thanh-toan
     */
    public com.example.backendlaptop.dto.banhang.KiemTraTruocThanhToanResponse kiemTraTruocThanhToan(UUID idHoaDon) {
        return thanhToanService.kiemTraTruocThanhToan(idHoaDon);
    }

    /**
     * API 6: Xác Thực Serial Number
     * Endpoint: POST /api/v1/ban-hang/hoa-don/xac-thuc-serial
     */
    public XacThucSerialResponse xacThucSerial(XacThucSerialRequest request) {
        return thanhToanService.xacThucSerial(request);
    }

    // ==================== IN HÓA ĐƠN ====================

    /**
     * API: In hóa đơn
     * Endpoint: GET /api/v1/ban-hang/hoa-don/{idHoaDon}/in
     */
    public String inHoaDon(UUID idHoaDon) {
        return inHoaDonService.generateInvoiceHTML(idHoaDon);
    }
}

