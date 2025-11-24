package com.example.backendlaptop.dto.hoadon;

import com.example.backendlaptop.entity.*;
import com.example.backendlaptop.model.TrangThaiHoaDon;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * DTO Response cho chi tiết hóa đơn (dùng trong Modal/Page chi tiết)
 */
@Data
public class HoaDonDetailResponse {
    // Thông tin cơ bản
    private UUID id;
    private String ma;
    private Instant ngayTao;
    private Instant ngayThanhToan;
    private BigDecimal tongTien;
    private BigDecimal tienDuocGiam;
    private BigDecimal tongTienSauGiam;
    private Integer loaiHoaDon;
    private TrangThaiHoaDon trangThai;
    private Integer trangThaiThanhToan;
    private String ghiChu;
    private Integer soDiemSuDung;
    private BigDecimal soTienQuyDoi;
    
    // Fields hỗ trợ frontend
    private String trangThaiDisplay; // Text hiển thị trạng thái
    private Boolean canCancel; // Có thể hủy không (chỉ khi CHO_THANH_TOAN)
    private Boolean canTrack; // Có thể theo dõi không (khi đã xác nhận trở đi)
    
    // Thông tin khách hàng
    private KhachHangInfo khachHang;
    
    // Thông tin nhân viên
    private NhanVienInfo nhanVien;
    
    // Danh sách sản phẩm
    private List<SanPhamInfo> chiTietList = new ArrayList<>();
    
    // Thông tin thanh toán
    private List<ThanhToanInfo> thanhToanList = new ArrayList<>();
    
    public HoaDonDetailResponse(HoaDon hoaDon) {
        this.id = hoaDon.getId();
        this.ma = hoaDon.getMa();
        this.ngayTao = hoaDon.getNgayTao();
        this.ngayThanhToan = hoaDon.getNgayThanhToan();
        this.tongTien = hoaDon.getTongTien();
        this.tienDuocGiam = hoaDon.getTienDuocGiam();
        this.tongTienSauGiam = hoaDon.getTongTienSauGiam();
        this.loaiHoaDon = hoaDon.getLoaiHoaDon();
        this.trangThai = hoaDon.getTrangThai();
        this.trangThaiThanhToan = hoaDon.getTrangThaiThanhToan();
        this.ghiChu = hoaDon.getGhiChu();
        this.soDiemSuDung = hoaDon.getSoDiemSuDung();
        this.soTienQuyDoi = hoaDon.getSoTienQuyDoi();
        
        // Tính toán các field hỗ trợ frontend
        this.trangThaiDisplay = getTrangThaiDisplay(hoaDon.getTrangThai());
        this.canCancel = hoaDon.getTrangThai() == TrangThaiHoaDon.CHO_THANH_TOAN;
        this.canTrack = hoaDon.getTrangThai() != TrangThaiHoaDon.CHO_THANH_TOAN 
                && hoaDon.getTrangThai() != TrangThaiHoaDon.DA_HUY;
        
        // Map thông tin khách hàng
        if (hoaDon.getIdKhachHang() != null) {
            this.khachHang = new KhachHangInfo(hoaDon.getIdKhachHang());
        } else {
            // Khách vãng lai
            this.khachHang = new KhachHangInfo(
                hoaDon.getTenKhachHang(),
                hoaDon.getSdt(),
                hoaDon.getDiaChi()
            );
        }
        
        // Map thông tin nhân viên
        if (hoaDon.getIdNhanVien() != null) {
            this.nhanVien = new NhanVienInfo(hoaDon.getIdNhanVien());
        }
        
        // Map danh sách sản phẩm
        if (hoaDon.getHoaDonChiTiets() != null) {
            this.chiTietList = hoaDon.getHoaDonChiTiets().stream()
                .map(SanPhamInfo::new)
                .collect(Collectors.toList());
        }
        
        // NOTE: ChiTietThanhToan không có relationship trong HoaDon entity
        // Cần query riêng nếu cần hiển thị thông tin thanh toán
        // TODO: Có thể thêm sau bằng cách inject ChiTietThanhToanRepository vào Service
    }
    
    // ===== HELPER METHODS =====
    
    /**
     * Lấy text hiển thị cho trạng thái
     */
    private String getTrangThaiDisplay(TrangThaiHoaDon trangThai) {
        if (trangThai == null) {
            return "Không xác định";
        }
        switch (trangThai) {
            case CHO_THANH_TOAN:
                return "Chờ thanh toán";
            case DA_THANH_TOAN:
                return "Đã thanh toán";
            case DA_HUY:
                return "Đã hủy";
            case DANG_GIAO:
                return "Đang giao hàng";
            case HOAN_THANH:
                return "Hoàn thành";
            default:
                return trangThai.toString();
        }
    }
    
    // ===== INNER CLASSES =====
    
    @Data
    public static class KhachHangInfo {
        private UUID id;
        private String hoTen;
        private String soDienThoai;
        private String email;
        private String diaChi;
        
        public KhachHangInfo(KhachHang kh) {
            this.id = kh.getId(); // FIXED: Dùng getId() thay vì getUserId()
            this.hoTen = kh.getHoTen();
            this.soDienThoai = kh.getSoDienThoai();
            this.email = kh.getEmail();
            // Địa chỉ có thể lấy từ bảng dia_chi hoặc từ hóa đơn
        }
        
        public KhachHangInfo(String hoTen, String sdt, String diaChi) {
            this.hoTen = hoTen;
            this.soDienThoai = sdt;
            this.diaChi = diaChi;
        }
    }
    
    @Data
    public static class NhanVienInfo {
        private UUID id;
        private String hoTen;
        private String maNhanVien;
        private String chucVu;
        
        public NhanVienInfo(NhanVien nv) {
            this.id = nv.getId(); // FIXED: Dùng getId() thay vì getUserId()
            this.hoTen = nv.getHoTen();
            this.maNhanVien = nv.getMaNhanVien();
            this.chucVu = nv.getChucVu();
        }
    }
    
    @Data
    public static class SanPhamInfo {
        private UUID id;
        private String tenSanPham;
        private String maCtsp;
        private Integer soLuong;
        private BigDecimal donGia;
        private BigDecimal thanhTien;
        private List<String> serialNumbers; // Danh sách serial đã quét (từ SerialDaBan)
        
        public SanPhamInfo(HoaDonChiTiet hdct) {
            this(hdct, null);
        }
        
        public SanPhamInfo(HoaDonChiTiet hdct, List<String> serialNumbers) {
            this.id = hdct.getId();
            this.soLuong = hdct.getSoLuong();
            this.donGia = hdct.getDonGia();
            
            // Tính thành tiền
            if (this.soLuong != null && this.donGia != null) {
                this.thanhTien = this.donGia.multiply(new BigDecimal(this.soLuong));
            }
            
            // Lấy thông tin sản phẩm
            if (hdct.getChiTietSanPham() != null) {
                this.maCtsp = hdct.getChiTietSanPham().getMaCtsp();
                if (hdct.getChiTietSanPham().getSanPham() != null) {
                    this.tenSanPham = hdct.getChiTietSanPham().getSanPham().getTenSanPham();
                }
            }
            
            // Map serial numbers
            this.serialNumbers = serialNumbers;
        }
    }
    
    @Data
    public static class ThanhToanInfo {
        private UUID id;
        private String tenPhuongThuc;
        private BigDecimal soTienThanhToan;
        private String maGiaoDich;
        private String ghiChu;
        
        public ThanhToanInfo(ChiTietThanhToan ctt) {
            this.id = ctt.getId();
            this.soTienThanhToan = ctt.getSoTienThanhToan();
            this.maGiaoDich = ctt.getMaGiaoDich();
            this.ghiChu = ctt.getGhiChu();
            
            // Lấy tên phương thức thanh toán
            // FIXED: Dùng getPhuongThucThanhToan() thay vì getPhuongThucThanhToanId()
            if (ctt.getPhuongThucThanhToan() != null) {
                this.tenPhuongThuc = ctt.getPhuongThucThanhToan().getTenPhuongThuc();
            }
        }
    }
}

