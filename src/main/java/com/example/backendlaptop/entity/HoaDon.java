package com.example.backendlaptop.entity;

import com.example.backendlaptop.model.TrangThaiHoaDon;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "hoa_don")
public class HoaDon {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Size(max = 50)
    @Column(name = "ma", length = 50)
    private String ma;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_khach_hang")
    @JsonIgnore
    private KhachHang idKhachHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_nhan_vien")
    @JsonIgnore
    private NhanVien idNhanVien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_phieu_giam_gia")
    @JsonIgnore
    private PhieuGiamGia idPhieuGiamGia;

    @OneToMany(mappedBy = "hoaDon", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<HoaDonChiTiet> hoaDonChiTiets;

    @Size(max = 50)
    @Column(name = "ma_don_hang", length = 50)
    private String maDonHang;

    @Size(max = 255)
    @Nationalized
    @Column(name = "ten_khach_hang")
    private String tenKhachHang;

    @Size(max = 20)
    @Column(name = "sdt", length = 20)
    private String sdt;

    @Size(max = 500)
    @Nationalized
    @Column(name = "dia_chi", length = 500)
    private String diaChi;

    @Column(name = "tong_tien", precision = 18, scale = 2)
    private BigDecimal tongTien;

    @Column(name = "tien_duoc_giam", precision = 18, scale = 2)
    private BigDecimal tienDuocGiam;

    @Column(name = "tong_tien_sau_giam", precision = 18, scale = 2)
    private BigDecimal tongTienSauGiam;

    @Column(name = "loai_hoa_don")
    private Integer loaiHoaDon;

    @Nationalized
    @Lob
    @Column(name = "ghi_chu")
    private String ghiChu;

    @Column(name = "ngay_tao")
    private Instant ngayTao;

    @Column(name = "ngay_thanh_toan")
    private Instant ngayThanhToan;

    @Column(name = "trang_thai_thanh_toan")
    private Integer trangThaiThanhToan;

    @Convert(converter = com.example.backendlaptop.converter.TrangThaiHoaDonConverter.class)
    @Column(name = "trang_thai")
    private TrangThaiHoaDon trangThai;

    @Column(name = "so_diem_su_dung")
    private Integer soDiemSuDung;

    @Column(name = "so_tien_quy_doi", precision = 18, scale = 2)
    private BigDecimal soTienQuyDoi;

}