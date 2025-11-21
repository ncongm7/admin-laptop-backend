package com.example.backendlaptop.service.customer;

import com.example.backendlaptop.dto.sanpham.ProductDetailResponse;
import com.example.backendlaptop.entity.*;
import com.example.backendlaptop.expection.ApiException;
import com.example.backendlaptop.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;
import java.util.Comparator;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductDetailService {

    private final SanPhamRepository sanPhamRepository;
    private final ChiTietSanPhamRepository chiTietSanPhamRepository;
    private final HinhAnhRepository hinhAnhRepository;
    private final DotGiamGiaChiTietRepository dotGiamGiaChiTietRepository;
    private final DotGiamGiaRepository dotGiamGiaRepository;
    private final DanhGiaRepository danhGiaRepository;
    private final SanPhamDanhMucRepository sanPhamDanhMucRepository;
    private final DanhMucRepository danhMucRepository;

    public ProductDetailResponse getProductDetail(UUID productId) {
        SanPham sanPham = sanPhamRepository.findById(productId)
                .orElseThrow(() -> new ApiException("Không tìm thấy sản phẩm với ID: " + productId));

        ProductDetailResponse response = new ProductDetailResponse();
        
        // Thông tin cơ bản
        response.setId(sanPham.getId());
        response.setMaSanPham(sanPham.getMaSanPham());
        response.setTenSanPham(sanPham.getTenSanPham());
        response.setMoTa(sanPham.getMoTa());
        response.setTrangThai(sanPham.getTrangThai());
        response.setNgayTao(sanPham.getNgayTao());
        response.setNgaySua(sanPham.getNgaySua());
        
        // Variants
        response.setVariants(getVariants(sanPham.getId()));
        
        // Images
        response.setImages(getImages(sanPham.getId()));
        
        // Promotion
        response.setPromotion(getPromotion(sanPham.getId()));
        
        // Review Summary
        response.setReviewSummary(getReviewSummary(sanPham.getId()));
        
        // Categories
        response.setCategories(getCategories(sanPham.getId()));
        
        return response;
    }

    public List<com.example.backendlaptop.dto.sanpham.SanPhamResponse> getRelatedProducts(UUID productId, Integer limit) {
        // Lấy sản phẩm hiện tại
        SanPham currentProduct = sanPhamRepository.findById(productId)
                .orElseThrow(() -> new ApiException("Không tìm thấy sản phẩm với ID: " + productId));
        
        // Lấy danh mục của sản phẩm hiện tại
        List<com.example.backendlaptop.entity.SanphamDanhmuc> productCategories = sanPhamDanhMucRepository.findByIdSanPham(productId);
        List<UUID> categoryIds = productCategories.stream()
                .map(pc -> pc.getIdDanhMuc().getId())
                .collect(Collectors.toList());
        
        if (categoryIds.isEmpty()) {
            // Nếu không có danh mục, trả về sản phẩm mới nhất
            return sanPhamRepository.findTopByTrangThaiOrderByNgayTaoDesc(
                    1, org.springframework.data.domain.PageRequest.of(0, limit != null ? limit : 8))
                    .stream()
                    .filter(sp -> !sp.getId().equals(productId))
                    .limit(limit != null ? limit : 8)
                    .map(this::convertToSanPhamResponse)
                    .collect(Collectors.toList());
        }
        
        // Tìm sản phẩm cùng danh mục
        List<com.example.backendlaptop.entity.SanphamDanhmuc> relatedProductCategories = sanPhamDanhMucRepository.findByIdDanhMuc(categoryIds.get(0));
        List<UUID> relatedProductIds = relatedProductCategories.stream()
                .map(pc -> pc.getIdSanPham().getId())
                .filter(id -> !id.equals(productId))
                .distinct()
                .limit(limit != null ? limit : 8)
                .collect(Collectors.toList());
        
        if (relatedProductIds.isEmpty()) {
            // Fallback: trả về sản phẩm mới nhất
            return sanPhamRepository.findTopByTrangThaiOrderByNgayTaoDesc(
                    1, org.springframework.data.domain.PageRequest.of(0, limit != null ? limit : 8))
                    .stream()
                    .filter(sp -> !sp.getId().equals(productId))
                    .limit(limit != null ? limit : 8)
                    .map(this::convertToSanPhamResponse)
                    .collect(Collectors.toList());
        }
        
        return relatedProductIds.stream()
                .map(id -> sanPhamRepository.findById(id))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(sp -> sp.getTrangThai() != null && sp.getTrangThai() == 1)
                .map(this::convertToSanPhamResponse)
                .collect(Collectors.toList());
    }

    private com.example.backendlaptop.dto.sanpham.SanPhamResponse convertToSanPhamResponse(SanPham sanPham) {
        com.example.backendlaptop.dto.sanpham.SanPhamResponse response = new com.example.backendlaptop.dto.sanpham.SanPhamResponse();
        response.setId(sanPham.getId());
        response.setMaSanPham(sanPham.getMaSanPham());
        response.setTenSanPham(sanPham.getTenSanPham());
        response.setMoTa(sanPham.getMoTa());
        response.setTrangThai(sanPham.getTrangThai());
        response.setNgayTao(sanPham.getNgayTao());
        response.setNgaySua(sanPham.getNgaySua());
        return response;
    }

    private List<ProductDetailResponse.VariantDetailResponse> getVariants(UUID productId) {
        List<ChiTietSanPham> variants = chiTietSanPhamRepository.findBySanPham_Id(productId);
        
        return variants.stream()
                .filter(v -> v.getTrangThai() != null && v.getTrangThai() == 1)
                .map(variant -> {
                    ProductDetailResponse.VariantDetailResponse v = new ProductDetailResponse.VariantDetailResponse();
                    v.setId(variant.getId());
                    v.setMaCtsp(variant.getMaCtsp());
                    v.setGiaBan(variant.getGiaBan());
                    v.setSoLuongTon(variant.getSoLuongTon());
                    v.setTrangThai(variant.getTrangThai());
                    
                    // Thông số kỹ thuật
                    if (variant.getCpu() != null) v.setCpu(variant.getCpu().getTenCpu());
                    if (variant.getGpu() != null) v.setGpu(variant.getGpu().getTenGpu());
                    if (variant.getRam() != null) v.setRam(variant.getRam().getTenRam());
                    if (variant.getOCung() != null) v.setOCung(variant.getOCung().getDungLuong());
                    if (variant.getMauSac() != null) v.setMauSac(variant.getMauSac().getTenMau());
                    if (variant.getLoaiManHinh() != null) v.setKichThuocManHinh(variant.getLoaiManHinh().getKichThuoc());
                    if (variant.getPin() != null) v.setDungLuongPin(variant.getPin().getDungLuongPin());
                    
                    // Hình ảnh của variant
                    List<String> imageUrls = hinhAnhRepository.findByIdSpctId(variant.getId())
                            .stream()
                            .sorted(Comparator.comparing((HinhAnh h) -> h.getAnhChinhDaiDien() != null && h.getAnhChinhDaiDien() ? 0 : 1)
                                    .thenComparing(h -> h.getNgayTao() != null ? h.getNgayTao() : Instant.MIN))
                            .map(HinhAnh::getUrl)
                            .collect(Collectors.toList());
                    v.setImageUrls(imageUrls);
                    
                    // Áp dụng giảm giá
                    applyDiscountToVariant(variant, v);
                    
                    return v;
                })
                .collect(Collectors.toList());
    }

    private void applyDiscountToVariant(ChiTietSanPham variant, ProductDetailResponse.VariantDetailResponse v) {
        Instant now = Instant.now();
        List<DotGiamGiaChiTiet> discounts = dotGiamGiaChiTietRepository.findAll();
        
        Optional<DotGiamGiaChiTiet> activeDiscount = discounts.stream()
                .filter(d -> d.getIdCtsp() != null && d.getIdCtsp().getId().equals(variant.getId()))
                .filter(d -> d.getDotGiamGia() != null && d.getDotGiamGia().getTrangThai() == 1)
                .filter(d -> {
                    DotGiamGia promo = d.getDotGiamGia();
                    return promo.getNgayBatDau() != null && promo.getNgayKetThuc() != null
                            && !now.isBefore(promo.getNgayBatDau())
                            && !now.isAfter(promo.getNgayKetThuc());
                })
                .findFirst();
        
        if (activeDiscount.isPresent()) {
            DotGiamGiaChiTiet discount = activeDiscount.get();
            v.setCoGiamGia(true);
            v.setGiaGoc(discount.getGiaBanDau());
            v.setGiaGiam(discount.getGiaSauKhiGiam());
            
            if (discount.getGiaBanDau() != null && discount.getGiaSauKhiGiam() != null
                    && discount.getGiaBanDau().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal discountAmount = discount.getGiaBanDau().subtract(discount.getGiaSauKhiGiam());
                BigDecimal discountPercent = discountAmount.divide(discount.getGiaBanDau(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                v.setPhanTramGiam(discountPercent.intValue());
            }
        } else {
            v.setCoGiamGia(false);
            v.setGiaGoc(variant.getGiaBan());
            v.setGiaGiam(variant.getGiaBan());
        }
    }

    private List<ProductDetailResponse.ImageResponse> getImages(UUID productId) {
        // Lấy tất cả hình ảnh từ các variants của sản phẩm
        List<ChiTietSanPham> variants = chiTietSanPhamRepository.findBySanPham_Id(productId);
        Set<HinhAnh> allImages = new LinkedHashSet<>();
        
        for (ChiTietSanPham variant : variants) {
            List<HinhAnh> variantImages = hinhAnhRepository.findByIdSpctId(variant.getId());
            allImages.addAll(variantImages);
        }
        
        return allImages.stream()
                .sorted(Comparator.comparing((HinhAnh h) -> h.getAnhChinhDaiDien() != null && h.getAnhChinhDaiDien() ? 0 : 1)
                        .thenComparing(h -> h.getNgayTao() != null ? h.getNgayTao() : Instant.MIN))
                .map(img -> {
                    ProductDetailResponse.ImageResponse image = new ProductDetailResponse.ImageResponse();
                    image.setId(img.getId());
                    image.setUrl(img.getUrl());
                    image.setIsMain(img.getAnhChinhDaiDien() != null && img.getAnhChinhDaiDien());
                    image.setThuTu(img.getAnhChinhDaiDien() != null && img.getAnhChinhDaiDien() ? 0 : 1);
                    return image;
                })
                .collect(Collectors.toList());
    }

    private ProductDetailResponse.PromotionInfo getPromotion(UUID productId) {
        Instant now = Instant.now();
        List<ChiTietSanPham> variants = chiTietSanPhamRepository.findBySanPham_Id(productId);
        
        for (ChiTietSanPham variant : variants) {
            List<DotGiamGiaChiTiet> discounts = dotGiamGiaChiTietRepository.findAll();
            Optional<DotGiamGiaChiTiet> activeDiscount = discounts.stream()
                    .filter(d -> d.getIdCtsp() != null && d.getIdCtsp().getId().equals(variant.getId()))
                    .filter(d -> d.getDotGiamGia() != null && d.getDotGiamGia().getTrangThai() == 1)
                    .filter(d -> {
                        DotGiamGia promo = d.getDotGiamGia();
                        return promo.getNgayBatDau() != null && promo.getNgayKetThuc() != null
                                && !now.isBefore(promo.getNgayBatDau())
                                && !now.isAfter(promo.getNgayKetThuc());
                    })
                    .findFirst();
            
            if (activeDiscount.isPresent()) {
                DotGiamGia promo = activeDiscount.get().getDotGiamGia();
                ProductDetailResponse.PromotionInfo promotion = new ProductDetailResponse.PromotionInfo();
                promotion.setId(promo.getId());
                promotion.setTenKm(promo.getTenKm());
                promotion.setMoTa(promo.getMoTa());
                // Tính phần trăm giảm từ loaiDotGiamGia và giaTri
                if (promo.getLoaiDotGiamGia() != null && promo.getLoaiDotGiamGia() == 1 && promo.getGiaTri() != null) {
                    promotion.setPhanTramGiam(promo.getGiaTri().intValue());
                } else {
                    promotion.setPhanTramGiam(0);
                }
                promotion.setSoTienGiamToiDa(promo.getSoTienGiamToiDa());
                promotion.setNgayBatDau(promo.getNgayBatDau());
                promotion.setNgayKetThuc(promo.getNgayKetThuc());
                promotion.setBannerImageUrl(promo.getBannerImageUrl());
                promotion.setIsFlashSale(promo.getLoaiDotGiamGia() != null && promo.getLoaiDotGiamGia() == 2);
                return promotion;
            }
        }
        
        return null;
    }

    private ProductDetailResponse.ReviewSummary getReviewSummary(UUID productId) {
        List<ChiTietSanPham> variants = chiTietSanPhamRepository.findBySanPham_Id(productId);
        List<UUID> variantIds = variants.stream().map(ChiTietSanPham::getId).collect(Collectors.toList());
        
        List<DanhGia> allReviews = danhGiaRepository.findByTrangThaiDanhGia(1);
        List<DanhGia> reviews = allReviews.stream()
                .filter(r -> r.getSanPhamChiTiet() != null && variantIds.contains(r.getSanPhamChiTiet().getId()))
                .collect(Collectors.toList());
        
        if (reviews.isEmpty()) {
            ProductDetailResponse.ReviewSummary summary = new ProductDetailResponse.ReviewSummary();
            summary.setAverageRating(0.0);
            summary.setTotalReviews(0);
            summary.setRatingDistribution(new HashMap<>());
            return summary;
        }
        
        double avgRating = reviews.stream()
                .mapToInt(DanhGia::getSoSao)
                .average()
                .orElse(0.0);
        
        Map<Integer, Integer> distribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            final int rating = i;
            long count = reviews.stream().filter(r -> r.getSoSao() != null && r.getSoSao() == rating).count();
            distribution.put(rating, (int) count);
        }
        
        ProductDetailResponse.ReviewSummary summary = new ProductDetailResponse.ReviewSummary();
        summary.setAverageRating(Math.round(avgRating * 10.0) / 10.0);
        summary.setTotalReviews(reviews.size());
        summary.setRatingDistribution(distribution);
        
        return summary;
    }

    private List<ProductDetailResponse.CategoryInfo> getCategories(UUID productId) {
        List<com.example.backendlaptop.entity.SanphamDanhmuc> productCategories = sanPhamDanhMucRepository.findByIdSanPham(productId);
        
        return productCategories.stream()
                .map(pc -> {
                    Optional<DanhMuc> category = danhMucRepository.findById(pc.getIdDanhMuc().getId());
                    if (category.isPresent()) {
                        ProductDetailResponse.CategoryInfo cat = new ProductDetailResponse.CategoryInfo();
                        cat.setId(category.get().getId());
                        cat.setName(category.get().getTenDanhMuc());
                        cat.setSlug(category.get().getSlug() != null ? category.get().getSlug() : "");
                        return cat;
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}

