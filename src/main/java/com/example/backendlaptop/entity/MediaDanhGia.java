package com.example.backendlaptop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "media_danh_gia")
public class MediaDanhGia {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "danh_gia_id")
    private DanhGia danhGia;

    @Column(name = "loai_media")
    private Integer loaiMedia;

    @Lob
    @Column(name = "url_media")
    private String urlMedia;

    @Column(name = "kich_thuoc_file")
    private Long kichThuocFile;

    @Column(name = "thoi_luong_video")
    private Integer thoiLuongVideo;

    @Column(name = "thu_tu_hien_thi")
    private Integer thuTuHienThi;

    @Column(name = "ngay_upload")
    private Instant ngayUpload;

    @Column(name = "trang_thai_media_danh_gia")
    private Integer trangThaiMediaDanhGia;

}