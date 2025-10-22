package com.example.backendlaptop.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "serial")
public class Serial {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ctsp_id")
    private ChiTietSanPham ctsp;

    @Size(max = 100)
    @Column(name = "serial_no", length = 100)
    private String serialNo;

    @Column(name = "trang_thai")
    private Integer trangThai;

    @Column(name = "ngay_nhap")
    private Instant ngayNhap;

}