package com.example.backendlaptop.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "dia_diem_bao_hanh")
public class DiaDiemBaoHanh {
    @Id
    @UuidGenerator
    @Column(name = "id", columnDefinition = "uniqueidentifier", nullable = false)
    private UUID id;

    @Nationalized
    @Column(name = "ten", nullable = false)
    private String ten;

    @Nationalized
    @Column(name = "dia_chi", length = 500)
    private String diaChi;

    @Column(name = "so_dien_thoai", length = 20)
    private String soDienThoai;

    @Column(name = "email", length = 255)
    private String email;

    @Nationalized
    @Column(name = "gio_lam_viec", length = 255)
    private String gioLamViec;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "ngay_tao")
    private Instant ngayTao = Instant.now();
}

