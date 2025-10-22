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
@Table(name = "loai_man_hinh")
public class LoaiManHinh {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Size(max = 50)
    @Column(name = "ma_loai_man_hinh", length = 50)
    private String maLoaiManHinh;

    @Size(max = 100)
    @Nationalized
    @Column(name = "kich_thuoc", length = 100)
    private String kichThuoc;

    @Nationalized
    @Lob
    @Column(name = "mo_ta")
    private String moTa;

    @Column(name = "trang_thai")
    private Integer trangThai;

}