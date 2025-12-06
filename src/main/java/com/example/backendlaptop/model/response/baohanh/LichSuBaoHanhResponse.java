package com.example.backendlaptop.model.response.baohanh;

import com.example.backendlaptop.entity.LichSuBaoHanh;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
@Getter
@Setter
public class LichSuBaoHanhResponse {
    private UUID id;
    private Instant ngayTiepNhan;
    private Instant ngayHoanThanh;
    private Integer trangThai;
    private String moTaLoi;
    private UUID idLyDoBaoHanh;
    private String tenLyDoBaoHanh;
    private UUID idPhieuHen;
    private String maPhieuHen;
    private UUID idNhanVienTiepNhan;
    private String tenNhanVienTiepNhan;
    private UUID idNhanVienSuaChua;
    private String tenNhanVienSuaChua;
    private List<String> hinhAnhTruoc;
    private List<String> hinhAnhSau;
    private java.math.BigDecimal chiPhiPhatSinh;
    private Boolean daThanhToan;
    private Instant ngayNhanHang;
    private Instant ngayBanGiao;
    private Boolean xacNhanKhachHang;
    private java.math.BigDecimal chiPhiSuaChua;
    private String phuongThucSuaChua;
    private String ghiChuNhanVien;

    public LichSuBaoHanhResponse(LichSuBaoHanh entity) {
        this.id = entity.getId();
        this.ngayTiepNhan = entity.getNgayTiepNhan();
        this.ngayHoanThanh = entity.getNgayHoanThanh();
        this.trangThai = entity.getTrangThai();
        this.moTaLoi = entity.getMoTaLoi();

        // Các trường mới
        this.idLyDoBaoHanh = entity.getIdLyDoBaoHanh() != null ? entity.getIdLyDoBaoHanh().getId() : null;
        this.tenLyDoBaoHanh = entity.getIdLyDoBaoHanh() != null ? entity.getIdLyDoBaoHanh().getTenLyDo() : null;
        this.idPhieuHen = entity.getIdPhieuHen() != null ? entity.getIdPhieuHen().getId() : null;
        this.maPhieuHen = entity.getIdPhieuHen() != null ? entity.getIdPhieuHen().getMaPhieuHen() : null;
        this.idNhanVienTiepNhan = entity.getIdNhanVienTiepNhan() != null ? entity.getIdNhanVienTiepNhan().getId() : null;
        this.tenNhanVienTiepNhan = entity.getIdNhanVienTiepNhan() != null ? entity.getIdNhanVienTiepNhan().getHoTen() : null;
        this.idNhanVienSuaChua = entity.getIdNhanVienSuaChua() != null ? entity.getIdNhanVienSuaChua().getId() : null;
        this.tenNhanVienSuaChua = entity.getIdNhanVienSuaChua() != null ? entity.getIdNhanVienSuaChua().getHoTen() : null;

        // Parse JSON arrays
        if (entity.getHinhAnhTruoc() != null && !entity.getHinhAnhTruoc().trim().isEmpty()) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                this.hinhAnhTruoc = mapper.readValue(entity.getHinhAnhTruoc(), new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {});
            } catch (Exception e) {
                this.hinhAnhTruoc = null;
            }
        }

        if (entity.getHinhAnhSau() != null && !entity.getHinhAnhSau().trim().isEmpty()) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                this.hinhAnhSau = mapper.readValue(entity.getHinhAnhSau(), new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {});
            } catch (Exception e) {
                this.hinhAnhSau = null;
            }
        }

        this.chiPhiPhatSinh = entity.getChiPhiPhatSinh();
        this.daThanhToan = entity.getDaThanhToan();
        this.ngayNhanHang = entity.getNgayNhanHang();
        this.ngayBanGiao = entity.getNgayBanGiao();
        this.xacNhanKhachHang = entity.getXacNhanKhachHang();
    }
}
