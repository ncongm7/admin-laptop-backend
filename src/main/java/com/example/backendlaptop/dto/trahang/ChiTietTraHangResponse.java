package com.example.backendlaptop.dto.trahang;

import com.example.backendlaptop.entity.ChiTietTraHang;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
public class ChiTietTraHangResponse {
    private UUID id;
    private UUID idYeuCauTraHang;
    private UUID idHoaDonChiTiet;
    private UUID idSerialDaBan;
    private String tenSanPham;
    private String maCtsp;
    private Integer soLuong;
    private BigDecimal donGia;
    private BigDecimal thanhTien;
    private String tinhTrangLucTra;
    private String moTaTinhTrang;
    private String[] hinhAnh; // Array of URLs
    private Instant ngayTao;

    public ChiTietTraHangResponse(ChiTietTraHang ctt) {
        this.id = ctt.getId();
        if (ctt.getIdYeuCauTraHang() != null) {
            this.idYeuCauTraHang = ctt.getIdYeuCauTraHang().getId();
        }
        if (ctt.getIdHoaDonChiTiet() != null) {
            this.idHoaDonChiTiet = ctt.getIdHoaDonChiTiet().getId();
            if (ctt.getIdHoaDonChiTiet().getChiTietSanPham() != null) {
                this.maCtsp = ctt.getIdHoaDonChiTiet().getChiTietSanPham().getMaCtsp();
                if (ctt.getIdHoaDonChiTiet().getChiTietSanPham().getSanPham() != null) {
                    this.tenSanPham = ctt.getIdHoaDonChiTiet().getChiTietSanPham().getSanPham().getTenSanPham();
                }
            }
        }
        if (ctt.getIdSerialDaBan() != null) {
            this.idSerialDaBan = ctt.getIdSerialDaBan().getId();
        }
        this.soLuong = ctt.getSoLuong();
        this.donGia = ctt.getDonGia();
        this.thanhTien = ctt.getThanhTien();
        this.tinhTrangLucTra = ctt.getTinhTrangLucTra();
        this.moTaTinhTrang = ctt.getMoTaTinhTrang();
        
        // Parse JSON array of URLs
        if (ctt.getHinhAnh() != null && !ctt.getHinhAnh().isEmpty()) {
            try {
                // Simple parsing - assuming format: ["url1","url2"] or "url1,url2"
                String hinhAnhStr = ctt.getHinhAnh().trim();
                if (hinhAnhStr.startsWith("[")) {
                    // JSON array format
                    hinhAnhStr = hinhAnhStr.substring(1, hinhAnhStr.length() - 1);
                    this.hinhAnh = hinhAnhStr.split(",");
                } else {
                    // Comma-separated format
                    this.hinhAnh = hinhAnhStr.split(",");
                }
            } catch (Exception e) {
                this.hinhAnh = new String[]{ctt.getHinhAnh()};
            }
        }
        
        this.ngayTao = ctt.getNgayTao();
    }
}

