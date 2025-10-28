package com.example.backendlaptop.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "phieu_giam_gia")
public class PhieuGiamGia {
    @Id
    @UuidGenerator // 👈 TẠO GIÁ TRỊ UUID TRÊN PHÍA JAVA
    @Column(name = "id", columnDefinition = "uniqueidentifier", nullable = false) // 👈 ÁNH XẠ CHÍNH XÁC VỚI DB
    private UUID id;

    @Size(max = 50)
    @Column(name = "ma", length = 50)
    private String ma;

    @Size(max = 255)
    @Nationalized
    @Column(name = "ten_phieu_giam_gia")
    private String tenPhieuGiamGia;

    @Column(name = "loai_phieu_giam_gia")
    private Integer loaiPhieuGiamGia;

    @Column(name = "gia_tri_giam_gia", precision = 18, scale = 2)
    private BigDecimal giaTriGiamGia;

    @Column(name = "so_tien_giam_toi_da", precision = 18, scale = 2)
    private BigDecimal soTienGiamToiDa;

    @Column(name = "hoa_don_toi_thieu", precision = 18, scale = 2)
    private BigDecimal hoaDonToiThieu;

    @Column(name = "so_luong_dung")
    private Integer soLuongDung;

    @Column(name = "ngay_bat_dau")
    private Instant ngayBatDau;

    @Column(name = "ngay_ket_thuc")
    private Instant ngayKetThuc;

    @Column(name = "rieng_tu")
    private Boolean riengTu;

    @Nationalized
    @Lob
    @Column(name = "mo_ta")
    private String moTa;

    @Column(name = "trang_thai")
    private Integer trangThai;

}