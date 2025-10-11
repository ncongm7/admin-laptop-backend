package com.example.backendlaptop.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "nhan_vien")
public class NhanVien {
    @Id
    @Column(name = "user_id", nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_tai_khoan")
    private TaiKhoan maTaiKhoan;

    @Size(max = 50)
    @Column(name = "ma_nhan_vien", length = 50)
    private String maNhanVien;

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

    @Lob
    @Column(name = "anh_nhan_vien")
    private String anhNhanVien;

    @Size(max = 100)
    @Nationalized
    @Column(name = "chuc_vu", length = 100)
    private String chucVu;

    @Size(max = 500)
    @Nationalized
    @Column(name = "dia_chi", length = 500)
    private String diaChi;

    @Nationalized
    @Lob
    @Column(name = "danh_gia")
    private String danhGia;

    @Column(name = "trang_thai")
    private Integer trangThai;

}