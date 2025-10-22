package com.example.backendlaptop.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "gpu")
public class Gpu {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Size(max = 50)
    @Column(name = "ma_gpu", length = 50)
    private String maGpu;

    @Size(max = 255)
    @Nationalized
    @Column(name = "ten_gpu")
    private String tenGpu;

    @Nationalized
    @Lob
    @Column(name = "mo_ta")
    private String moTa;

    @Column(name = "trang_thai")
    private Integer trangThai;

}