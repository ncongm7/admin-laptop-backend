package com.example.backendlaptop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "hinh_anh")
public class HinhAnh {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_spct")
    private ChiTietSanPham idSpct;

    @Lob
    @Column(name = "url")
    private String url;

    @Column(name = "anh_chinh_dai_dien")
    private Boolean anhChinhDaiDien;

    @Column(name = "ngay_tao")
    private Instant ngayTao;

    @Column(name = "ngay_sua")
    private Instant ngaySua;

}