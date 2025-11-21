package com.example.backendlaptop.service.customer;

import com.example.backendlaptop.entity.ChiTietSanPham;
import com.example.backendlaptop.entity.DotGiamGia;
import com.example.backendlaptop.entity.DotGiamGiaChiTiet;
import com.example.backendlaptop.entity.HinhAnh;
import com.example.backendlaptop.repository.DotGiamGiaChiTietRepository;
import com.example.backendlaptop.repository.DotGiamGiaRepository;
import com.example.backendlaptop.repository.HinhAnhRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FlashSaleService {

    private final DotGiamGiaRepository dotGiamGiaRepository;
    private final DotGiamGiaChiTietRepository dotGiamGiaChiTietRepository;
    private final HinhAnhRepository hinhAnhRepository;

    public List<Map<String, Object>> getFlashSaleProducts() {
        Instant now = Instant.now();
        
        // Lấy đợt giảm giá đang active
        List<DotGiamGia> activePromotions = dotGiamGiaRepository
                .findByTrangThaiAndNgayBatDauLessThanEqualAndNgayKetThucGreaterThanEqualAndBannerImageUrlIsNotNull(1, now, now);
        
        if (activePromotions.isEmpty()) {
            return List.of();
        }
        
        // Lấy đợt giảm giá đầu tiên (hoặc có thể lấy tất cả)
        DotGiamGia promotion = activePromotions.get(0);
        
        // Lấy danh sách sản phẩm trong đợt giảm giá
        List<DotGiamGiaChiTiet> promotionDetails = dotGiamGiaChiTietRepository
                .findByDotGiamGia_Id(promotion.getId());
        
        return promotionDetails.stream()
                .map(detail -> {
                    ChiTietSanPham ctsp = detail.getIdCtsp();
                    Map<String, Object> product = new HashMap<>();
                    product.put("id", ctsp.getId());
                    product.put("maCtsp", ctsp.getMaCtsp());
                    product.put("tenSanPham", ctsp.getSanPham() != null ? ctsp.getSanPham().getTenSanPham() : "Sản phẩm");
                    product.put("originalPrice", detail.getGiaBanDau());
                    product.put("discountPrice", detail.getGiaSauKhiGiam());
                    
                    // Tính phần trăm giảm giá
                    BigDecimal discount = detail.getGiaBanDau().subtract(detail.getGiaSauKhiGiam());
                    BigDecimal discountPercent = discount.divide(detail.getGiaBanDau(), 2, java.math.RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100));
                    product.put("discountPercent", discountPercent.intValue());
                    
                    // Lấy hình ảnh chính
                    HinhAnh mainImage = hinhAnhRepository.findMainImageByCtspId(ctsp.getId())
                            .orElseGet(() -> hinhAnhRepository.findByIdSpctId(ctsp.getId()).stream()
                                    .findFirst()
                                    .orElse(null));
                    product.put("image", mainImage != null ? mainImage.getUrl() : null);
                    
                    product.put("promotion", Map.of(
                            "id", promotion.getId(),
                            "name", promotion.getTenKm(),
                            "endDate", promotion.getNgayKetThuc()
                    ));
                    
                    return product;
                })
                .collect(Collectors.toList());
    }
}

