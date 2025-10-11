package com.example.backendlaptop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "lich_su_diem")
public class LichSuDiem {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tich_diem_id")
    private TichDiem tichDiem;

    @Column(name = "hoa_don_id")
    private UUID hoaDonId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_quy_doi_diem")
    private QuyDoiDiem idQuyDoiDiem;

    @Column(name = "loai_diem")
    private Integer loaiDiem;

    @Nationalized
    @Lob
    @Column(name = "ghi_chu")
    private String ghiChu;

    @Column(name = "thoi_gian")
    private Instant thoiGian;

    @Column(name = "han_su_dung")
    private LocalDate hanSuDung;

    @Column(name = "so_diem_da_dung")
    private Integer soDiemDaDung;

    @Column(name = "so_diem_cong")
    private Integer soDiemCong;

    @Column(name = "trang_thai")
    private Integer trangThai;

}