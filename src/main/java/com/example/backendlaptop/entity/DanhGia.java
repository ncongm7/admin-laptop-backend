package com.example.backendlaptop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "danh_gia")
public class DanhGia {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "khach_hang_id")
    private KhachHang khachHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "san_pham_chi_tiet_id")
    private ChiTietSanPham sanPhamChiTiet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hoa_don_chi_tiet_id")
    private HoaDonChiTiet hoaDonChiTiet;

    @Column(name = "so_sao")
    private Integer soSao;

    @Nationalized
    @Lob
    @Column(name = "noi_dung")
    private String noiDung;

    @Column(name = "ngay_danh_gia")
    private Instant ngayDanhGia;

    @Column(name = "trang_thai_danh_gia")
    private Integer trangThaiDanhGia;

    // Các cột mới cho hệ thống đánh giá nâng cao
    @Column(name = "is_verified_purchase")
    private Boolean isVerifiedPurchase;

    @Column(name = "helpful_count")
    private Integer helpfulCount;

    @Nationalized
    @Column(name = "review_title", length = 255)
    private String reviewTitle;

    @Nationalized
    @Lob
    @Column(name = "pros")
    private String pros; // JSON array: ["Hiệu năng tốt", "Pin trâu"]

    @Nationalized
    @Lob
    @Column(name = "cons")
    private String cons; // JSON array: ["Hơi nặng", "Giá cao"]

    // Relationships
    @OneToMany(mappedBy = "danhGia", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MediaDanhGia> mediaList;

    @OneToMany(mappedBy = "danhGia", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PhanHoiDanhGia> phanHoiList;

    @OneToMany(mappedBy = "danhGia", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HelpfulVote> helpfulVotes;

    @PrePersist
    public void prePersist() {
        if (helpfulCount == null) {
            helpfulCount = 0;
        }
        if (isVerifiedPurchase == null) {
            isVerifiedPurchase = false;
        }
    }
}