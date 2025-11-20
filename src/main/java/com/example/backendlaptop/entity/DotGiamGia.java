package com.example.backendlaptop.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "dot_giam_gia")
public class DotGiamGia {
    @Id
    @UuidGenerator // 👈 TẠO GIÁ TRỊ UUID TRÊN PHÍA JAVA
    @Column(name = "id", columnDefinition = "uniqueidentifier", nullable = false) // 👈 ÁNH XẠ CHÍNH XÁC VỚI DB
    private UUID id;

    @Size(max = 255)
    @Nationalized
    @Column(name = "ten_km")
    private String tenKm;

    @Column(name = "loai_dot_giam_gia")
    private Integer loaiDotGiamGia; // 1: Giảm theo %, 2: Giảm theo số tiền (VND)

    @Column(name = "gia_tri", precision = 18, scale = 2)
    private java.math.BigDecimal giaTri; // Giá trị giảm: % (0-100) hoặc số tiền VND

    @Column(name = "so_tien_giam_toi_da", precision = 18, scale = 2)
    private java.math.BigDecimal soTienGiamToiDa; // Giới hạn số tiền giảm tối đa (chỉ dùng khi loai = 1 - %)

    @Nationalized
    @Lob
    @Column(name = "mo_ta")
    private String moTa;

    @Column(name = "ngayBatDau")
    private Instant ngayBatDau;

    @Column(name = "ngayKetThuc")
    private Instant ngayKetThuc;

    @Column(name = "trang_thai")
    private Integer trangThai;

    @Column(name = "bannerImageUrl", nullable = true)
    private String bannerImageUrl;
}