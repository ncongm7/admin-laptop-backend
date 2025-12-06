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
@Table(name = "email_log")
public class EmailLog {
    @Id
    @UuidGenerator
    @Column(name = "id", columnDefinition = "uniqueidentifier", nullable = false)
    private UUID id;

    @Column(name = "loai_email", length = 50)
    private String loaiEmail; // 'XAC_NHAN_BAO_HANH', 'PHIEU_HEN', 'HOAN_THANH', 'CHI_PHI'

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_bao_hanh")
    private PhieuBaoHanh idBaoHanh;

    @Nationalized
    @Column(name = "email_nguoi_nhan", length = 255)
    private String emailNguoiNhan;

    @Nationalized
    @Column(name = "tieu_de", length = 500)
    private String tieuDe;

    @Nationalized
    @Lob
    @Column(name = "noi_dung")
    private String noiDung;

    @Column(name = "trang_thai")
    private Integer trangThai = 0; // 0: Chưa gửi, 1: Đã gửi, 2: Lỗi

    @Column(name = "ngay_gui")
    private Instant ngayGui;

    @Nationalized
    @Lob
    @Column(name = "loi_gui")
    private String loiGui;
}

