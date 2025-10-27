package com.example.backendlaptop.dto.banhang;

import com.example.backendlaptop.entity.HoaDonChiTiet;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class HoaDonChiTietResponse {
    private UUID id;
    private UUID idDonHang;
    private UUID idChiTietSanPham;
    private String tenSanPham;
    private String maChiTietSanPham;
    private Integer soLuong;
    private BigDecimal donGia;
    private BigDecimal thanhTien;

    public HoaDonChiTietResponse(HoaDonChiTiet hoaDonChiTiet) {
        this.id = hoaDonChiTiet.getId();
        this.idDonHang = hoaDonChiTiet.getHoaDon() != null ? hoaDonChiTiet.getHoaDon().getId() : null;
        this.idChiTietSanPham = hoaDonChiTiet.getChiTietSanPham() != null ? hoaDonChiTiet.getChiTietSanPham().getId() : null;
        this.soLuong = hoaDonChiTiet.getSoLuong();
        this.donGia = hoaDonChiTiet.getDonGia();
        
        // Lấy thông tin sản phẩm
        if (hoaDonChiTiet.getChiTietSanPham() != null) {
            this.maChiTietSanPham = hoaDonChiTiet.getChiTietSanPham().getMaCtsp();
            if (hoaDonChiTiet.getChiTietSanPham().getSp() != null) {
                this.tenSanPham = hoaDonChiTiet.getChiTietSanPham().getSp().getTenSanPham();
            }
        }
        
        // Tính thành tiền
        if (this.soLuong != null && this.donGia != null) {
            this.thanhTien = this.donGia.multiply(new BigDecimal(this.soLuong));
        }
    }
}

