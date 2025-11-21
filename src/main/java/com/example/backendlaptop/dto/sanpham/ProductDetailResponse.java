package com.example.backendlaptop.dto.sanpham;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
public class ProductDetailResponse {
    
    // Thông tin sản phẩm cơ bản
    private UUID id;
    private String maSanPham;
    private String tenSanPham;
    private String moTa;
    private Integer trangThai;
    private Instant ngayTao;
    private Instant ngaySua;
    
    // Danh sách biến thể (variants)
    private List<VariantDetailResponse> variants;
    
    // Danh sách hình ảnh
    private List<ImageResponse> images;
    
    // Thông tin khuyến mãi
    private PromotionInfo promotion;
    
    // Thông tin đánh giá tổng hợp
    private ReviewSummary reviewSummary;
    
    // Danh sách danh mục
    private List<CategoryInfo> categories;
    
    @Data
    public static class VariantDetailResponse {
        private UUID id;
        private String maCtsp;
        private BigDecimal giaBan;
        private BigDecimal giaGoc;
        private BigDecimal giaGiam;
        private Integer phanTramGiam;
        private Boolean coGiamGia;
        private Integer soLuongTon;
        private Integer trangThai;
        
        // Thông số kỹ thuật
        private String cpu;
        private String gpu;
        private String ram;
        private String oCung;
        private String mauSac;
        private String kichThuocManHinh;
        private String dungLuongPin;
        
        // Hình ảnh của variant này
        private List<String> imageUrls;
    }
    
    @Data
    public static class ImageResponse {
        private UUID id;
        private String url;
        private Boolean isMain;
        private Integer thuTu;
    }
    
    @Data
    public static class PromotionInfo {
        private UUID id;
        private String tenKm;
        private String moTa;
        private Integer phanTramGiam;
        private BigDecimal soTienGiamToiDa;
        private Instant ngayBatDau;
        private Instant ngayKetThuc;
        private String bannerImageUrl;
        private Boolean isFlashSale;
    }
    
    @Data
    public static class ReviewSummary {
        private Double averageRating;
        private Integer totalReviews;
        private Map<Integer, Integer> ratingDistribution; // {5: 10, 4: 5, ...}
    }
    
    @Data
    public static class CategoryInfo {
        private UUID id;
        private String name;
        private String slug;
    }
}

