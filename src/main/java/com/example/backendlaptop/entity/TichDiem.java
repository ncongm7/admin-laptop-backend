package com.example.backendlaptop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "tich_diem")
public class TichDiem {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private KhachHang user;

    @Column(name = "diem_da_dung")
    private Integer diemDaDung;

    @Column(name = "diem_da_cong")
    private Integer diemDaCong;

    @Column(name = "tong_diem")
    private Integer tongDiem;

    @Column(name = "trang_thai")
    private Integer trangThai = 1; // 1 = Active, 0 = Inactive

}