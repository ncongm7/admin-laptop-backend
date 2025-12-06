package com.example.backendlaptop.model.response.baohanh;

import com.example.backendlaptop.entity.PhieuHenBaoHanh;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
public class PhieuHenBaoHanhResponse {
    private UUID id;
    private UUID idBaoHanh;
    private UUID idNhanVienTiepNhan;
    private String tenNhanVienTiepNhan;
    private String maPhieuHen;
    private Instant ngayHen;
    private LocalTime gioHen;
    private String diaDiem;
    private String ghiChu;
    private Integer trangThai;
    private Boolean emailDaGui;
    private Instant ngayTao;

    public PhieuHenBaoHanhResponse(PhieuHenBaoHanh entity) {
        this.id = entity.getId();
        this.idBaoHanh = entity.getIdBaoHanh() != null ? entity.getIdBaoHanh().getId() : null;
        this.idNhanVienTiepNhan = entity.getIdNhanVienTiepNhan() != null ? entity.getIdNhanVienTiepNhan().getId() : null;
        this.tenNhanVienTiepNhan = entity.getIdNhanVienTiepNhan() != null ? entity.getIdNhanVienTiepNhan().getHoTen() : null;
        this.maPhieuHen = entity.getMaPhieuHen();
        this.ngayHen = entity.getNgayHen();
        this.gioHen = entity.getGioHen();
        this.diaDiem = entity.getDiaDiem();
        this.ghiChu = entity.getGhiChu();
        this.trangThai = entity.getTrangThai();
        this.emailDaGui = entity.getEmailDaGui();
        this.ngayTao = entity.getNgayTao();
    }
}

