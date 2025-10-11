package com.example.backendlaptop.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "dot_giam_gia")
public class DotGiamGia {
    @Id
    @org.hibernate.annotations.UuidGenerator
    @Column(name = "id", columnDefinition = "uniqueidentifier")
    private UUID id;

    @Size(max = 255)
    @Nationalized
    @Column(name = "ten_km")
    private String tenKm;

    @Column(name = "gia_tri")
    private Integer giaTri;

    @Nationalized
    @Lob
    @Column(name = "mo_ta")
    private String moTa;

    @Column(name = "ngayBatDau")
    private Instant ngayBatDau;

    @Column(name = "ngayKetThuc")
    private Instant ngayKetThuc;

    @Column(name = "trang_thai")
    private Integer trangThai;

}
