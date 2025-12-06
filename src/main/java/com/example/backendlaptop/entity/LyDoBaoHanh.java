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
@Table(name = "ly_do_bao_hanh")
public class LyDoBaoHanh {
    @Id
    @UuidGenerator
    @Column(name = "id", columnDefinition = "uniqueidentifier", nullable = false)
    private UUID id;

    @Column(name = "ma_ly_do", length = 50, unique = true, nullable = false)
    private String maLyDo;

    @Nationalized
    @Column(name = "ten_ly_do", nullable = false)
    private String tenLyDo;

    @Nationalized
    @Lob
    @Column(name = "mo_ta")
    private String moTa;

    @Nationalized
    @Column(name = "loai_ly_do", length = 50)
    private String loaiLyDo; // 'PHAN_CUNG', 'PHAN_MEM', 'PHU_KIEN', 'KHAC'

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "thu_tu")
    private Integer thuTu = 0;

    @Column(name = "ngay_tao")
    private Instant ngayTao = Instant.now();
}

