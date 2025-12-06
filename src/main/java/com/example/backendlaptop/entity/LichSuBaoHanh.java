package com.example.backendlaptop.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "lich_su_bao_hanh")
public class LichSuBaoHanh {
    @Id
    @UuidGenerator // 👈 TẠO GIÁ TRỊ UUID TRÊN PHÍA JAVA
    @Column(name = "id", columnDefinition = "uniqueidentifier", nullable = false) // 👈 ÁNH XẠ CHÍNH XÁC VỚI DB
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_bao_hanh")
    private PhieuBaoHanh idBaoHanh;

    @Column(name = "ngay_tiep_nhan")
    private Instant ngayTiepNhan;

    @Column(name = "ngay_hoan_thanh")
    private Instant ngayHoanThanh;

    @Nationalized
    @Lob
    @Column(name = "mo_ta_loi")
    private String moTaLoi;

    @Column(name = "trang_thai")
    private Integer trangThai;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ly_do_bao_hanh")
    private LyDoBaoHanh idLyDoBaoHanh;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_phieu_hen")
    private PhieuHenBaoHanh idPhieuHen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_nhan_vien_tiep_nhan")
    private NhanVien idNhanVienTiepNhan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_nhan_vien_sua_chua")
    private NhanVien idNhanVienSuaChua;

    @Nationalized
    @Lob
    @Column(name = "hinh_anh_truoc")
    private String hinhAnhTruoc; // JSON array of URLs

    @Nationalized
    @Lob
    @Column(name = "hinh_anh_sau")
    private String hinhAnhSau; // JSON array of URLs

    @Column(name = "chi_phi_phat_sinh", precision = 18, scale = 2)
    private java.math.BigDecimal chiPhiPhatSinh;

    @Column(name = "da_thanh_toan")
    private Boolean daThanhToan = false;

    @Column(name = "ngay_nhan_hang")
    private Instant ngayNhanHang;

    @Column(name = "ngay_ban_giao")
    private Instant ngayBanGiao;

    @Column(name = "xac_nhan_khach_hang")
    private Boolean xacNhanKhachHang = false;
}