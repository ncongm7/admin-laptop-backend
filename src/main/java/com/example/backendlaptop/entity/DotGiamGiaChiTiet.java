package com.example.backendlaptop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "dot_giam_gia_chi_tiet")
public class DotGiamGiaChiTiet {
    @Id
    @ColumnDefault("newid()")
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_km")
    private DotGiamGia dotGiamGia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ctsp")
    private ChiTietSanPham idCtsp;

    @Column(name = "gia_ban_dau", precision = 18, scale = 2)
    private BigDecimal giaBanDau;

    @Column(name = "gia_sau_khi_giam", precision = 18, scale = 2)
    private BigDecimal giaSauKhiGiam;

}