package com.example.backendlaptop.dto.hoadon;

import com.example.backendlaptop.entity.HoaDon;
import com.example.backendlaptop.model.TrangThaiHoaDon;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * DTO Response cho danh sách hóa đơn (dùng trong bảng)
 */
@Data
public class HoaDonListResponse {
    private UUID id;
    private String ma;
    private String tenKhachHang;
    private String sdt;
    private String tenNhanVien;          
    private String maNhanVien;
    private Instant ngayTao;
    private Instant ngayThanhToan;
    private BigDecimal tongTien;
    private BigDecimal tienDuocGiam;
    private BigDecimal tongTienSauGiam;
    private Integer loaiHoaDon;          // 0: Tại quầy, 1: Online
    private TrangThaiHoaDon trangThai;   // Enum
    private Integer trangThaiThanhToan;  // 0: Chưa, 1: Đã
    private String ghiChu;

    public HoaDonListResponse(HoaDon hoaDon) {
        try {
            this.id = hoaDon.getId();
            this.ma = hoaDon.getMa();
            this.tenKhachHang = hoaDon.getTenKhachHang();
            this.sdt = hoaDon.getSdt();
            this.ngayTao = hoaDon.getNgayTao();
            this.ngayThanhToan = hoaDon.getNgayThanhToan();
            this.tongTien = hoaDon.getTongTien();
            this.tienDuocGiam = hoaDon.getTienDuocGiam();
            this.tongTienSauGiam = hoaDon.getTongTienSauGiam();
            this.loaiHoaDon = hoaDon.getLoaiHoaDon();
            
            // Xử lý an toàn cho enum trangThai (có thể null hoặc giá trị không hợp lệ)
            try {
                this.trangThai = hoaDon.getTrangThai();
            } catch (Exception e) {
                System.err.println("⚠️ [HoaDonListResponse] Lỗi khi lấy trangThai, set null: " + e.getMessage());
                this.trangThai = null;
            }
            
            this.trangThaiThanhToan = hoaDon.getTrangThaiThanhToan();
            this.ghiChu = hoaDon.getGhiChu();
            
            // Lấy thông tin nhân viên nếu có
            if (hoaDon.getIdNhanVien() != null) {
                this.tenNhanVien = hoaDon.getIdNhanVien().getHoTen();
                this.maNhanVien = hoaDon.getIdNhanVien().getMaNhanVien();
            }
        } catch (Exception e) {
            System.err.println("❌ [HoaDonListResponse] Lỗi khi tạo response từ HoaDon:");
            System.err.println("  - HoaDon ID: " + (hoaDon != null ? hoaDon.getId() : "null"));
            System.err.println("  - Error: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}

