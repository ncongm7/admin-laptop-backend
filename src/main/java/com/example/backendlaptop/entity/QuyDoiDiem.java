package com.example.backendlaptop.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "quy_doi_diem")
public class QuyDoiDiem {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "tien_tich_diem", precision = 18, scale = 2)
    private BigDecimal tienTichDiem;

    @Column(name = "tien_tieu_diem", precision = 18, scale = 2)
    private BigDecimal tienTieuDiem;

    @Column(name = "trang_thai")
    private Integer trangThai;

}