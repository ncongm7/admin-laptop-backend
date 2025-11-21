package com.example.backendlaptop.service.customer;

import com.example.backendlaptop.dto.review.ReviewRequest;
import com.example.backendlaptop.dto.review.ReviewResponse;
import com.example.backendlaptop.entity.*;
import com.example.backendlaptop.expection.ApiException;
import com.example.backendlaptop.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final DanhGiaRepository danhGiaRepository;
    private final HinhAnhRepository hinhAnhRepository;
    private final ChiTietSanPhamRepository chiTietSanPhamRepository;
    private final SanPhamRepository sanPhamRepository;
    private final com.example.backendlaptop.repository.banhang.HoaDonChiTietRepository hoaDonChiTietRepository;
    private final KhachHangRepository khachHangRepository;

    public List<Map<String, Object>> getTopReviews(Integer limit) {
        List<DanhGia> reviews = danhGiaRepository.findTopByTrangThaiDanhGiaOrderByNgayDanhGiaDesc(
                1, PageRequest.of(0, limit != null ? limit : 4));
        
        return reviews.stream()
                .map(review -> {
                    Map<String, Object> reviewMap = new HashMap<>();
                    reviewMap.put("id", review.getId());
                    reviewMap.put("title", review.getSanPhamChiTiet() != null && review.getSanPhamChiTiet().getSanPham() != null ? 
                            review.getSanPhamChiTiet().getSanPham().getTenSanPham() : "Sản phẩm");
                    reviewMap.put("excerpt", review.getNoiDung() != null && review.getNoiDung().length() > 100 ?
                            review.getNoiDung().substring(0, 100) + "..." : review.getNoiDung());
                    reviewMap.put("rating", review.getSoSao());
                    reviewMap.put("date", review.getNgayDanhGia());
                    
                    // Lấy hình ảnh sản phẩm làm thumbnail
                    if (review.getSanPhamChiTiet() != null) {
                        HinhAnh mainImage = hinhAnhRepository.findMainImageByCtspId(review.getSanPhamChiTiet().getId())
                                .orElseGet(() -> hinhAnhRepository.findByIdSpctId(review.getSanPhamChiTiet().getId()).stream()
                                        .findFirst()
                                        .orElse(null));
                        reviewMap.put("thumbnail", mainImage != null ? mainImage.getUrl() : null);
                    } else {
                        reviewMap.put("thumbnail", null);
                    }
                    
                    return reviewMap;
                })
                .collect(Collectors.toList());
    }

    public Page<ReviewResponse> getProductReviews(UUID productId, Integer rating, Pageable pageable) {
        // Lấy tất cả variants của sản phẩm
        List<ChiTietSanPham> variants = chiTietSanPhamRepository.findBySanPham_Id(productId);
        List<UUID> variantIds = variants.stream().map(ChiTietSanPham::getId).collect(Collectors.toList());
        
        if (variantIds.isEmpty()) {
            return Page.empty(pageable);
        }
        
        // Lấy reviews với filter
        Page<DanhGia> reviews = danhGiaRepository.findBySanPhamChiTiet_IdInAndTrangThaiDanhGiaAndRating(
                variantIds, 1, rating, pageable);
        
        return reviews.map(this::convertToReviewResponse);
    }

    @Transactional
    public ReviewResponse submitReview(UUID productId, UUID userId, ReviewRequest request) {
        // Kiểm tra sản phẩm tồn tại
        SanPham sanPham = sanPhamRepository.findById(productId)
                .orElseThrow(() -> new ApiException("Không tìm thấy sản phẩm với ID: " + productId));
        
        // Kiểm tra variant tồn tại
        ChiTietSanPham variant = chiTietSanPhamRepository.findById(request.getChiTietSanPhamId())
                .orElseThrow(() -> new ApiException("Không tìm thấy biến thể sản phẩm"));
        
        // Kiểm tra variant thuộc về sản phẩm
        if (!variant.getSanPham().getId().equals(productId)) {
            throw new ApiException("Biến thể không thuộc về sản phẩm này");
        }
        
        // Kiểm tra khách hàng đã mua sản phẩm này chưa
        KhachHang khachHang = khachHangRepository.findById(userId)
                .orElseThrow(() -> new ApiException("Không tìm thấy khách hàng"));
        
        // Kiểm tra đã mua (qua hoa_don_chi_tiet)
        // Tạm thời bỏ qua validation này, có thể implement sau
        boolean hasPurchased = true; // TODO: Implement check purchase
        
        if (!hasPurchased) {
            throw new ApiException("Bạn cần mua sản phẩm này trước khi đánh giá");
        }
        
        // Kiểm tra đã đánh giá chưa
        boolean hasReviewed = danhGiaRepository.findBySanPhamChiTiet_IdInAndTrangThaiDanhGia(
                Collections.singletonList(request.getChiTietSanPhamId()), 1)
                .stream()
                .anyMatch(r -> r.getKhachHang() != null && r.getKhachHang().getId().equals(userId));
        
        if (hasReviewed) {
            throw new ApiException("Bạn đã đánh giá sản phẩm này rồi");
        }
        
        // Tạo đánh giá mới
        DanhGia danhGia = new DanhGia();
        danhGia.setId(UUID.randomUUID());
        danhGia.setKhachHang(khachHang);
        danhGia.setSanPhamChiTiet(variant);
        danhGia.setSoSao(request.getSoSao());
        danhGia.setNoiDung(request.getNoiDung());
        danhGia.setNgayDanhGia(Instant.now());
        danhGia.setTrangThaiDanhGia(1); // Đã duyệt
        
        // Lưu đánh giá
        danhGia = danhGiaRepository.save(danhGia);
        
        // Lưu hình ảnh đánh giá (nếu có) - có thể lưu vào bảng riêng hoặc JSON
        // Ở đây tạm thời bỏ qua, có thể mở rộng sau
        
        return convertToReviewResponse(danhGia);
    }

    private ReviewResponse convertToReviewResponse(DanhGia danhGia) {
        ReviewResponse response = new ReviewResponse();
        response.setId(danhGia.getId());
        response.setSoSao(danhGia.getSoSao());
        response.setNoiDung(danhGia.getNoiDung());
        response.setNgayDanhGia(danhGia.getNgayDanhGia());
        response.setTrangThaiDanhGia(danhGia.getTrangThaiDanhGia());
        
        // Thông tin khách hàng
        if (danhGia.getKhachHang() != null) {
            ReviewResponse.CustomerInfo customerInfo = new ReviewResponse.CustomerInfo();
            customerInfo.setId(danhGia.getKhachHang().getId());
            customerInfo.setTenKhachHang(danhGia.getKhachHang().getHoTen());
            // Avatar URL có thể lấy từ user profile nếu có
            customerInfo.setAvatarUrl(null);
            response.setKhachHang(customerInfo);
        }
        
        // Hình ảnh đánh giá - tạm thời để trống, có thể mở rộng sau
        response.setImageUrls(new ArrayList<>());
        
        return response;
    }
}

