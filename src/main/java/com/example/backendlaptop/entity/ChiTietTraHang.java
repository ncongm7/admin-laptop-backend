package com.example.backendlaptop.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "chi_tiet_tra_hang")
public class ChiTietTraHang {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_yeu_cau_tra_hang", nullable = false)
    private YeuCauTraHang idYeuCauTraHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_hoa_don_chi_tiet", nullable = false)
    private HoaDonChiTiet idHoaDonChiTiet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_serial_da_ban")
    private SerialDaBan idSerialDaBan;

    @Column(name = "so_luong")
    private Integer soLuong;

    @Column(name = "don_gia", precision = 18, scale = 2)
    private BigDecimal donGia;

    @Column(name = "thanh_tien", precision = 18, scale = 2)
    private BigDecimal thanhTien;

    @Size(max = 100)
    @Nationalized
    @Column(name = "tinh_trang_luc_tra", length = 100)
    private String tinhTrangLucTra; // Tốt, Hỏng, Trầy xước, Khác

    @Nationalized
    @Lob
    @Column(name = "mo_ta_tinh_trang")
    private String moTaTinhTrang;

    @Lob
    @Column(name = "hinh_anh")
    private String hinhAnh; // JSON array of URLs

    @Column(name = "ngay_tao")
    private Instant ngayTao;
}

