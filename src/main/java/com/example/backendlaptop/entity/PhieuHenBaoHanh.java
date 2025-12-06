package com.example.backendlaptop.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "phieu_hen_bao_hanh")
public class PhieuHenBaoHanh {
    @Id
    @UuidGenerator
    @Column(name = "id", columnDefinition = "uniqueidentifier", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_bao_hanh", nullable = false)
    private PhieuBaoHanh idBaoHanh;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_nhan_vien_tiep_nhan")
    private NhanVien idNhanVienTiepNhan;

    @Column(name = "ma_phieu_hen", length = 50, unique = true, nullable = false)
    private String maPhieuHen;

    @Column(name = "ngay_hen", nullable = false)
    private Instant ngayHen;

    @Column(name = "gio_hen", nullable = false)
    private LocalTime gioHen;

    @Nationalized
    @Column(name = "dia_diem", length = 500)
    private String diaDiem;

    @Nationalized
    @Lob
    @Column(name = "ghi_chu")
    private String ghiChu;

    @Column(name = "trang_thai")
    private Integer trangThai = 0; // 0: Chờ xác nhận, 1: Đã xác nhận, 2: Đã hủy

    @Column(name = "email_da_gui")
    private Boolean emailDaGui = false;

    @Column(name = "ngay_tao")
    private Instant ngayTao = Instant.now();
}

