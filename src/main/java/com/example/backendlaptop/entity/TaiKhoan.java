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
@Table(name = "tai_khoan")
public class TaiKhoan {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_vai_tro")
    private VaiTro maVaiTro;

    @Size(max = 100)
    @Column(name = "ten_dang_nhap", length = 100)
    private String tenDangNhap;

    @Size(max = 255)
    @Nationalized
    @Column(name = "mat_khau")
    private String matKhau;

    @Size(max = 100)
    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "trang_thai")
    private Integer trangThai;

    @Column(name = "ngay_tao")
    private Instant ngayTao;

    @Column(name = "lan_dang_nhap_cuoi")
    private Instant lanDangNhapCuoi;

}