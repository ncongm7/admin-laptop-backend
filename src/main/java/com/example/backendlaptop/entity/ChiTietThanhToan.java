package com.example.backendlaptop.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "chi_tiet_thanh_toan")
public class ChiTietThanhToan {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_hoa_don")
    private HoaDon idHoaDon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phuong_thuc_thanh_toan_id")
    private PhuongThucThanhToan phuongThucThanhToan;

    @Column(name = "so_tien_thanh_toan", precision = 18, scale = 2)
    private BigDecimal soTienThanhToan;

    @Size(max = 100)
    @Column(name = "ma_giao_dich", length = 100)
    private String maGiaoDich;

    @Nationalized
    @Lob
    @Column(name = "ghi_chu")
    private String ghiChu;

}