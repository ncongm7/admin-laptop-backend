package com.example.backendlaptop.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "san_pham")
public class SanPham {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Size(max = 50)
    @Column(name = "ma_san_pham", length = 50)
    private String maSanPham;

    @Size(max = 255)
    @Nationalized
    @Column(name = "ten_san_pham")
    private String tenSanPham;

    @Nationalized
    @Lob
    @Column(name = "mo_ta")
    private String moTa;

    @Column(name = "trang_thai")
    private Integer trangThai;

    @Column(name = "ngay_tao")
    private Instant ngayTao;

    @Column(name = "ngay_sua")
    private Instant ngaySua;

    @Size(max = 255)
    @Nationalized
    @Column(name = "nguoi_tao")
    private String nguoiTao;

    @Size(max = 255)
    @Nationalized
    @Column(name = "nguoi_sua")
    private String nguoiSua;

}