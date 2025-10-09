package com.example.backendlaptop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "lich_su_bao_hanh")
public class LichSuBaoHanh {
    @Id
    @Column(name = "id", nullable = false)
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

}