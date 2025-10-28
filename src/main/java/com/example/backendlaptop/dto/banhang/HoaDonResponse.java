package com.example.backendlaptop.dto.banhang;

import com.example.backendlaptop.entity.HoaDon;
import com.example.backendlaptop.model.TrangThaiHoaDon;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
public class HoaDonResponse {
    private UUID id;
    private String ma;
    private UUID idKhachHang;
    private UUID idNhanVien;
    private UUID idPhieuGiamGia;
    private String maDonHang;
    private String tenKhachHang;
    private String sdt;
    private String diaChi;
    private BigDecimal tongTien;
    private BigDecimal tienDuocGiam;
    private BigDecimal tongTienSauGiam;
    private Integer loaiHoaDon;
    private String ghiChu;
    private Instant ngayTao;
    private Instant ngayThanhToan;
    private Integer trangThaiThanhToan;
    private TrangThaiHoaDon trangThai;
    private Integer soDiemSuDung;
    private BigDecimal soTienQuyDoi;
    private List<HoaDonChiTietResponse> chiTietList;

    public HoaDonResponse(HoaDon hoaDon) {
        this.id = hoaDon.getId();
        this.ma = hoaDon.getMa();
        this.idKhachHang = hoaDon.getIdKhachHang() != null ? hoaDon.getIdKhachHang().getId() : null;
        this.idNhanVien = hoaDon.getIdNhanVien() != null ? hoaDon.getIdNhanVien().getId() : null;
        this.idPhieuGiamGia = hoaDon.getIdPhieuGiamGia() != null ? hoaDon.getIdPhieuGiamGia().getId() : null;
        this.maDonHang = hoaDon.getMaDonHang();
        this.tenKhachHang = hoaDon.getTenKhachHang();
        this.sdt = hoaDon.getSdt();
        this.diaChi = hoaDon.getDiaChi();
        this.tongTien = hoaDon.getTongTien();
        this.tienDuocGiam = hoaDon.getTienDuocGiam();
        this.tongTienSauGiam = hoaDon.getTongTienSauGiam();
        this.loaiHoaDon = hoaDon.getLoaiHoaDon();
        this.ghiChu = hoaDon.getGhiChu();
        this.ngayTao = hoaDon.getNgayTao();
        this.ngayThanhToan = hoaDon.getNgayThanhToan();
        this.trangThaiThanhToan = hoaDon.getTrangThaiThanhToan();
        this.trangThai = hoaDon.getTrangThai();
        this.soDiemSuDung = hoaDon.getSoDiemSuDung();
        this.soTienQuyDoi = hoaDon.getSoTienQuyDoi();
        
        // Map chi tiết hóa đơn
        if (hoaDon.getHoaDonChiTiets() != null) {
            this.chiTietList = hoaDon.getHoaDonChiTiets().stream()
                    .map(HoaDonChiTietResponse::new)
                    .collect(Collectors.toList());
        }
    }
}

