package com.example.backendlaptop.dto.review;

import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
public class ReviewResponse {
    
    private UUID id;
    private Integer soSao;
    private String noiDung;
    private Instant ngayDanhGia;
    private Integer trangThaiDanhGia;
    
    // Các trường mới
    private Boolean isVerifiedPurchase;
    private Integer helpfulCount;
    private String reviewTitle;
    private List<String> pros;
    private List<String> cons;
    
    // Thông tin khách hàng
    private CustomerInfo khachHang;
    
    // Media đánh giá (hình ảnh + video)
    private List<MediaInfo> mediaList;
    
    // Phản hồi từ admin/nhân viên
    private ReplyInfo adminReply;
    
    // Vote của user hiện tại (nếu đã đăng nhập)
    private Boolean currentUserVote; // null = chưa vote, true = helpful, false = not helpful
    
    @Data
    public static class CustomerInfo {
        private UUID id;
        private String tenKhachHang;
        private String avatarUrl;
    }
    
    @Data
    public static class MediaInfo {
        private UUID id;
        private Integer loaiMedia; // 1 = image, 2 = video
        private String url;
        private Long kichThuocFile;
        private Integer thoiLuongVideo; // chỉ cho video
        private Integer thuTuHienThi;
    }
    
    @Data
    public static class ReplyInfo {
        private UUID id;
        private String noiDung;
        private Instant ngayPhanHoi;
        private String tenNhanVien;
    }
}


