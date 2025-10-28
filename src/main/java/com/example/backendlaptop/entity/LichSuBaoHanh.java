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

}