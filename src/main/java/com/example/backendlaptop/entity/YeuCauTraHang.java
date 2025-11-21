package com.example.backendlaptop.entity;

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
@Table(name = "yeu_cau_tra_hang")
public class YeuCauTraHang {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_hoa_don", nullable = false)
    private HoaDon idHoaDon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_khach_hang")
    private KhachHang idKhachHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_nhan_vien_xu_ly")
    private NhanVien idNhanVienXuLy;

    @Size(max = 50)
    @Column(name = "ma_yeu_cau", length = 50, unique = true)
    private String maYeuCau;

    @Nationalized
    @Lob
    @Column(name = "ly_do_tra_hang")
    private String lyDoTraHang;

    @Column(name = "ngay_mua")
    private Instant ngayMua;

    @Column(name = "ngay_yeu_cau")
    private Instant ngayYeuCau;

    @Column(name = "ngay_duyet")
    private Instant ngayDuyet;

    @Column(name = "ngay_hoan_tat")
    private Instant ngayHoanTat;

    @Column(name = "trang_thai")
    private Integer trangThai; // 0: Chờ duyệt, 1: Đã duyệt, 2: Từ chối, 3: Hoàn tất

    @Column(name = "so_ngay_sau_mua")
    private Integer soNgaySauMua;

    @Column(name = "loai_yeu_cau")
    private Integer loaiYeuCau; // 0: Đổi trả (hoàn tiền), 1: Bảo hành (chuyển sang bảo hành)

    @Column(name = "hinh_thuc_hoan_tien")
    private Integer hinhThucHoanTien; // 0: Theo phương thức gốc, 1: Tiền mặt, 2: Chuyển khoản

    @Column(name = "so_tien_hoan", precision = 18, scale = 2)
    private BigDecimal soTienHoan;

    @Nationalized
    @Lob
    @Column(name = "ly_do_tu_choi")
    private String lyDoTuChoi;

    @Nationalized
    @Lob
    @Column(name = "ghi_chu")
    private String ghiChu;

    @Column(name = "ngay_tao")
    private Instant ngayTao;

    @Column(name = "ngay_sua")
    private Instant ngaySua;

    @OneToMany(mappedBy = "idYeuCauTraHang", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ChiTietTraHang> chiTietTraHangs;

    @OneToMany(mappedBy = "idYeuCauTraHang", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<LichSuTraHang> lichSuTraHangs;
}
