package com.example.backendlaptop.service.dotgiamgia.customer;

import com.example.backendlaptop.dto.dotgiamgia.customer.DotGiamGiaDTOCustomer;
import com.example.backendlaptop.entity.DotGiamGia;
import com.example.backendlaptop.repository.DotGiamGiaChiTietRepository;
import com.example.backendlaptop.repository.DotGiamGiaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class DotGiamGiaCustomerService {
    @Autowired
    private DotGiamGiaRepository repository;

    @Autowired
    private DotGiamGiaChiTietRepository dotGiamGiaChiTietRepository;

    public Page<DotGiamGiaDTOCustomer> getAll(String status, Integer pageNo, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize != null ? pageSize : 5);
        Page<DotGiamGia> dotGiamGias;
        
        Instant now = Instant.now();
        
        // Lọc theo status: active (đang diễn ra) hoặc upcoming (sắp diễn ra)
        if (status != null && status.equalsIgnoreCase("upcoming")) {
            // Sắp diễn ra: ngayBatDau > now và trangThai = 1
            dotGiamGias = repository.findByTrangThaiAndNgayBatDauAfter(1, now, pageable);
        } else {
            // Mặc định: active - đang diễn ra (ngayBatDau <= now <= ngayKetThuc và trangThai = 1)
            dotGiamGias = repository.findByTrangThaiAndNgayBatDauLessThanEqualAndNgayKetThucGreaterThanEqual(1, now, now, pageable);
        }
        
        return dotGiamGias.map(DotGiamGiaDTOCustomer::new);
    }

    public Page<UUID> getIdCtspByIdDotGiamGia(UUID idDotGiamGia, Integer pageNo) {
        Pageable pageable = PageRequest.of(pageNo, 10); // Or any other page size
        return dotGiamGiaChiTietRepository.findIdCtspByIdDotGiamGia(idDotGiamGia, pageable);
    }
    
    /**
     * Lấy thông tin chi tiết campaign và danh sách idCTSP
     */
    public Object getCampaignDetail(UUID idDotGiamGia, Integer pageNo) {
        // Lấy thông tin campaign
        DotGiamGia campaign = repository.findById(idDotGiamGia).orElse(null);
        if (campaign == null) {
            return null;
        }
        
        // Lấy danh sách idCTSP
        Pageable pageable = PageRequest.of(pageNo, 10);
        Page<UUID> idCtspPage = dotGiamGiaChiTietRepository.findIdCtspByIdDotGiamGia(idDotGiamGia, pageable);
        
        // Tạo response object
        Map<String, Object> response = new HashMap<>();
        response.put("campaignInfo", new DotGiamGiaDTOCustomer(campaign));
        response.put("content", idCtspPage.getContent());
        response.put("number", idCtspPage.getNumber());
        response.put("size", idCtspPage.getSize());
        response.put("totalPages", idCtspPage.getTotalPages());
        response.put("totalElements", idCtspPage.getTotalElements());
        
        return response;
    }

    /**
     * Lấy danh sách banners cho homepage slider
     * @param type Loại banner (main-slider, etc.)
     * @return Danh sách banners với bannerImageUrl và đang active
     */
    public List<Map<String, Object>> getBanners(String type) {
        Instant now = Instant.now();
        List<DotGiamGia> activePromotions = repository.findByTrangThaiAndNgayBatDauLessThanEqualAndNgayKetThucGreaterThanEqualAndBannerImageUrlIsNotNull(1, now, now);
        
        return activePromotions.stream()
                .map(promo -> {
                    Map<String, Object> banner = new HashMap<>();
                    banner.put("id", promo.getId());
                    banner.put("title", promo.getTenKm());
                    banner.put("description", promo.getMoTa());
                    banner.put("image", promo.getBannerImageUrl());
                    banner.put("link", "/promotions/campaigns/" + promo.getId());
                    banner.put("buttonText", "Xem ngay");
                    return banner;
                })
                .collect(java.util.stream.Collectors.toList());
    }
}
