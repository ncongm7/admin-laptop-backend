package com.example.backendlaptop.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "phuong_thuc_thanh_toan")
public class PhuongThucThanhToan {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Size(max = 255)
    @Nationalized
    @Column(name = "ten_phuong_thuc")
    private String tenPhuongThuc;

    @Size(max = 100)
    @Nationalized
    @Column(name = "loai_phuong_thuc", length = 100)
    private String loaiPhuongThuc;

}