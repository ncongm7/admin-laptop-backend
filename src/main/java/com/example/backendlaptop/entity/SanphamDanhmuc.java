package com.example.backendlaptop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "sanpham_danhmuc")
public class SanphamDanhmuc {
    @EmbeddedId
    private SanphamDanhmucId id;

    @MapsId("idSanPham")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_san_pham", nullable = false)
    private SanPham idSanPham;

    @MapsId("idDanhMuc")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_danh_muc", nullable = false)
    private DanhMuc idDanhMuc;

}