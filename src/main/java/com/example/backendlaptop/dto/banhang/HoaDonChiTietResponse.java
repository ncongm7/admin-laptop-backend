package com.example.backendlaptop.dto.banhang;

import com.example.backendlaptop.entity.HoaDonChiTiet;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
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
    private Integer soLuongTon; // Số lượng tồn kho hiện tại (từ ChiTietSanPham)
    private List<String> serialNumbers; // Danh sách serial đã quét (từ SerialDaBan)

    public HoaDonChiTietResponse(HoaDonChiTiet hoaDonChiTiet) {
        this(hoaDonChiTiet, null);
    }

    /**
     * Constructor với serial numbers (để map từ service)
     */
    public HoaDonChiTietResponse(HoaDonChiTiet hoaDonChiTiet, List<String> serialNumbers) {
        this.id = hoaDonChiTiet.getId();
        this.idDonHang = hoaDonChiTiet.getHoaDon() != null ? hoaDonChiTiet.getHoaDon().getId() : null;
        this.idChiTietSanPham = hoaDonChiTiet.getChiTietSanPham() != null ? hoaDonChiTiet.getChiTietSanPham().getId() : null;
        this.soLuong = hoaDonChiTiet.getSoLuong();
        this.donGia = hoaDonChiTiet.getDonGia();
        
        // Lấy thông tin sản phẩm
        if (hoaDonChiTiet.getChiTietSanPham() != null) {
            this.maChiTietSanPham = hoaDonChiTiet.getChiTietSanPham().getMaCtsp();
            this.soLuongTon = hoaDonChiTiet.getChiTietSanPham().getSoLuongTon(); // Lấy số lượng tồn
            if (hoaDonChiTiet.getChiTietSanPham().getSanPham() != null) {
                this.tenSanPham = hoaDonChiTiet.getChiTietSanPham().getSanPham().getTenSanPham();
            }
        }
        
        // Tính thành tiền
        if (this.soLuong != null && this.donGia != null) {
            this.thanhTien = this.donGia.multiply(new BigDecimal(this.soLuong));
        }
        
        // Map serial numbers
        this.serialNumbers = serialNumbers;
    }
}

