package com.example.backendlaptop.service.customer;

import com.example.backendlaptop.dto.review.ReviewRequest;
import com.example.backendlaptop.dto.review.ReviewResponse;
import com.example.backendlaptop.entity.*;
import com.example.backendlaptop.expection.ApiException;
import com.example.backendlaptop.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final MediaDanhGiaRepository mediaDanhGiaRepository;
    private final PhanHoiDanhGiaRepository phanHoiDanhGiaRepository;
    private final HelpfulVoteRepository helpfulVoteRepository;
    private final ObjectMapper objectMapper;

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
        return getProductReviews(productId, rating, pageable, null);
    }

    /**
     * Lấy reviews với thông tin user hiện tại để hiển thị vote
     */
    public Page<ReviewResponse> getProductReviews(UUID productId, Integer rating, Pageable pageable, UUID currentUserId) {
        // Lấy tất cả variants của sản phẩm
        List<ChiTietSanPham> variants = chiTietSanPhamRepository.findBySanPham_Id(productId);
        List<UUID> variantIds = variants.stream().map(ChiTietSanPham::getId).collect(Collectors.toList());
        
        if (variantIds.isEmpty()) {
            return Page.empty(pageable);
        }
        
        // Lấy reviews với filter
        Page<DanhGia> reviews = danhGiaRepository.findBySanPhamChiTiet_IdInAndTrangThaiDanhGiaAndRating(
                variantIds, 1, rating, pageable);
        
        return reviews.map(review -> convertToReviewResponse(review, currentUserId));
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
        
        // Kiểm tra đã mua (qua hoa_don_chi_tiet) - verified purchase
        boolean hasPurchased = hoaDonChiTietRepository.existsByChiTietSanPham_IdAndIdDonHang_KhachHang_Id(
                request.getChiTietSanPhamId(), userId);
        
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
        danhGia.setReviewTitle(request.getReviewTitle());
        danhGia.setNgayDanhGia(Instant.now());
        danhGia.setTrangThaiDanhGia(0); // Chờ duyệt (0), admin sẽ duyệt sau
        danhGia.setIsVerifiedPurchase(hasPurchased); // Đánh dấu đã mua hàng
        danhGia.setHelpfulCount(0);
        
        // Lưu pros/cons dạng JSON
        try {
            if (request.getPros() != null && !request.getPros().isEmpty()) {
                danhGia.setPros(objectMapper.writeValueAsString(request.getPros()));
            }
            if (request.getCons() != null && !request.getCons().isEmpty()) {
                danhGia.setCons(objectMapper.writeValueAsString(request.getCons()));
            }
        } catch (Exception e) {
            throw new ApiException("Lỗi khi lưu pros/cons: " + e.getMessage());
        }
        
        // Lưu đánh giá
        danhGia = danhGiaRepository.save(danhGia);
        
        return convertToReviewResponse(danhGia, userId);
    }

    private ReviewResponse convertToReviewResponse(DanhGia danhGia) {
        return convertToReviewResponse(danhGia, null);
    }

    private ReviewResponse convertToReviewResponse(DanhGia danhGia, UUID currentUserId) {
        ReviewResponse response = new ReviewResponse();
        response.setId(danhGia.getId());
        response.setSoSao(danhGia.getSoSao());
        response.setNoiDung(danhGia.getNoiDung());
        response.setReviewTitle(danhGia.getReviewTitle());
        response.setNgayDanhGia(danhGia.getNgayDanhGia());
        response.setTrangThaiDanhGia(danhGia.getTrangThaiDanhGia());
        response.setIsVerifiedPurchase(danhGia.getIsVerifiedPurchase());
        response.setHelpfulCount(danhGia.getHelpfulCount());
        
        // Parse pros/cons từ JSON
        try {
            if (danhGia.getPros() != null) {
                response.setPros(objectMapper.readValue(danhGia.getPros(), new TypeReference<List<String>>() {}));
            }
            if (danhGia.getCons() != null) {
                response.setCons(objectMapper.readValue(danhGia.getCons(), new TypeReference<List<String>>() {}));
            }
        } catch (Exception e) {
            // Ignore JSON parse errors
        }
        
        // Thông tin khách hàng
        if (danhGia.getKhachHang() != null) {
            ReviewResponse.CustomerInfo customerInfo = new ReviewResponse.CustomerInfo();
            customerInfo.setId(danhGia.getKhachHang().getId());
            customerInfo.setTenKhachHang(danhGia.getKhachHang().getHoTen());
            customerInfo.setAvatarUrl(null); // TODO: Get from user profile
            response.setKhachHang(customerInfo);
        }
        
        // Media list (hình ảnh + video)
        List<MediaDanhGia> mediaList = mediaDanhGiaRepository.findByDanhGia_IdAndTrangThaiMediaDanhGia(
                danhGia.getId(), 1);
        response.setMediaList(mediaList.stream().map(media -> {
            ReviewResponse.MediaInfo mediaInfo = new ReviewResponse.MediaInfo();
            mediaInfo.setId(media.getId());
            mediaInfo.setLoaiMedia(media.getLoaiMedia());
            mediaInfo.setUrl(media.getUrlMedia());
            mediaInfo.setKichThuocFile(media.getKichThuocFile());
            mediaInfo.setThoiLuongVideo(media.getThoiLuongVideo());
            mediaInfo.setThuTuHienThi(media.getThuTuHienThi());
            return mediaInfo;
        }).collect(Collectors.toList()));
        
        // Admin reply
        phanHoiDanhGiaRepository.findFirstByDanhGia_IdOrderByNgayPhanHoiAsc(danhGia.getId())
                .ifPresent(reply -> {
                    ReviewResponse.ReplyInfo replyInfo = new ReviewResponse.ReplyInfo();
                    replyInfo.setId(reply.getId());
                    replyInfo.setNoiDung(reply.getNoiDung());
                    replyInfo.setNgayPhanHoi(reply.getNgayPhanHoi());
                    if (reply.getNhanVien() != null) {
                        replyInfo.setTenNhanVien(reply.getNhanVien().getHoTen());
                    }
                    response.setAdminReply(replyInfo);
                });
        
        // Current user vote (nếu đã đăng nhập)
        if (currentUserId != null) {
            helpfulVoteRepository.findByDanhGia_IdAndKhachHang_Id(danhGia.getId(), currentUserId)
                    .ifPresent(vote -> response.setCurrentUserVote(vote.getIsHelpful()));
        }
        
        return response;
    }
}

