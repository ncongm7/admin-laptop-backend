package com.example.backendlaptop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "phieu_bao_hanh")
public class PhieuBaoHanh {
    @Id
    @ColumnDefault("newid()")
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_khach_hang")
    private KhachHang idKhachHang;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_serial_da_ban")
    private SerialDaBan idSerialDaBan;

    @Column(name = "ngay_bat_dau")
    private Instant ngayBatDau;

    @Column(name = "ngay_ket_thuc")
    private Instant ngayKetThuc;

    @Column(name = "trang_thai_bao_hanh")
    private Integer trangThaiBaoHanh;

    @Lob
    @Column(name = "hinh_anh")
    private String hinhAnh; // JSON array of URLs - Ví dụ: ["url1", "url2"]

    @Lob
    @Column(name = "mo_ta")
    private String moTa;

    @Column(name = "chi_phi")
    private java.math.BigDecimal chiPhi;

    @Column(name = "so_lan_sua_chua")
    private Integer soLanSuaChua;

    @Column(name = "ma_phieu_bao_hanh", length = 50, unique = true)
    private String maPhieuBaoHanh;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_hoa_don_chi_tiet")
    private HoaDonChiTiet idHoaDonChiTiet;

    @Column(name = "ngay_tao")
    private Instant ngayTao = Instant.now();

    @Column(name = "ngay_cap_nhat")
    private Instant ngayCapNhat;
}