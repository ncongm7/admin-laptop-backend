package com.example.backendlaptop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "chi_tiet_lich_su_diem")
public class ChiTietLichSuDiem {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private KhachHang user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lich_su_diem_id")
    private LichSuDiem lichSuDiem;

    @Column(name = "so_diem_da_tru")
    private Integer soDiemDaTru;

    @Column(name = "ngay_tru")
    private Instant ngayTru;

}