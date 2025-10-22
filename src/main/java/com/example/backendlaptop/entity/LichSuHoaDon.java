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
@Table(name = "lich_su_hoa_don")
public class LichSuHoaDon {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_hoa_don")
    private HoaDon idHoaDon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_nhan_vien")
    private NhanVien idNhanVien;

    @Size(max = 50)
    @Column(name = "ma", length = 50)
    private String ma;

    @Nationalized
    @Lob
    @Column(name = "hanh_dong")
    private String hanhDong;

    @Column(name = "thoi_gian")
    private Instant thoiGian;

    @Column(name = "deleted")
    private Boolean deleted;

}