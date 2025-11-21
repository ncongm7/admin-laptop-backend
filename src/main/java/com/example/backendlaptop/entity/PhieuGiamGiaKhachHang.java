// FILE: src/main/java/com/example/backendlaptop/entity/PhieuGiamGiaKhachHang.java
package com.example.backendlaptop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "phieu_giam_gia_khach_hang", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"phieu_giam_gia_id", "khach_hang_id"}))
public class PhieuGiamGiaKhachHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phieu_giam_gia_id", nullable = false)
    private PhieuGiamGia phieuGiamGia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "khach_hang_id", nullable = false, referencedColumnName = "user_id")
    private KhachHang khachHang;
}

