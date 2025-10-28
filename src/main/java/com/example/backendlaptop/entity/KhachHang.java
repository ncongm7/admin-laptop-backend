package com.example.backendlaptop.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "khach_hang")
public class KhachHang {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "user_id", columnDefinition = "uniqueidentifier")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_tai_khoan")
    private TaiKhoan maTaiKhoan;

    @Size(max = 50)
    @Column(name = "ma_khach_hang", length = 50)
    private String maKhachHang;

    @Size(max = 255)
    @Nationalized
    @Column(name = "ho_ten")
    private String hoTen;

    @Size(max = 20)
    @Column(name = "so_dien_thoai", length = 20)
    private String soDienThoai;

    @Size(max = 100)
    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "gioi_tinh")
    private Integer gioiTinh;

    @Column(name = "ngay_sinh")
    private LocalDate ngaySinh;

    @Column(name = "trang_thai")
    private Integer trangThai;

}