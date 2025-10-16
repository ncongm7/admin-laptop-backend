package com.example.backendlaptop.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "chi_tiet_san_pham")
public class ChiTietSanPham {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne()
    @JoinColumn(name = "sp_id")
    private SanPham sanPham;

    @Size(max = 50)
    @Column(name = "ma_ctsp", length = 50)
    private String maCtsp;

    @Column(name = "gia_ban", precision = 18, scale = 2)
    private BigDecimal giaBan;

    @Nationalized
    @Lob
    @Column(name = "ghi_chu")
    private String ghiChu;

    @Column(name = "so_luong_ton")
    private Integer soLuongTon;

    @Column(name = "so_luong_tam_giu")
    private Integer soLuongTamGiu;

    @Column(name = "trang_thai")
    private Integer trangThai;

    @Version
    @Column(name = "version")
    private Long version;

    @ManyToOne()
    @JoinColumn(name = "cpu_id")
    private Cpu cpu;

    @ManyToOne()
    @JoinColumn(name = "gpu_id")
    private Gpu gpu;

    @ManyToOne()
    @JoinColumn(name = "ram_id")
    private Ram ram;

    @ManyToOne()
    @JoinColumn(name = "o_cung_id")
    private OCung oCung;

    @ManyToOne()
    @JoinColumn(name = "mau_sac_id")
    private MauSac mauSac;

    @ManyToOne()
    @JoinColumn(name = "loai_man_hinh_id")
    private LoaiManHinh loaiManHinh;

    @ManyToOne()
    @JoinColumn(name = "pin_id")
    private Pin pin;

    @Column(name = "ngay_tao")
    private Instant ngayTao;

    @Column(name = "ngay_sua")
    private Instant ngaySua;

}