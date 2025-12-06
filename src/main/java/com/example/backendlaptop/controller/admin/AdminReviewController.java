package com.example.backendlaptop.controller.admin;

import com.example.backendlaptop.dto.review.AdminReplyRequest;
import com.example.backendlaptop.dto.review.ReviewResponse;
import com.example.backendlaptop.entity.DanhGia;
import com.example.backendlaptop.entity.PhanHoiDanhGia;
import com.example.backendlaptop.entity.NhanVien;
import com.example.backendlaptop.expection.ApiException;
import com.example.backendlaptop.model.response.ResponseObject;
import com.example.backendlaptop.repository.DanhGiaRepository;
import com.example.backendlaptop.repository.NhanVienRepository;
import com.example.backendlaptop.repository.PhanHoiDanhGiaRepository;
import com.example.backendlaptop.service.customer.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/reviews")
@CrossOrigin("*")
@RequiredArgsConstructor
public class AdminReviewController {

    private final DanhGiaRepository danhGiaRepository;
    private final PhanHoiDanhGiaRepository phanHoiDanhGiaRepository;
    private final NhanVienRepository nhanVienRepository;
    private final ReviewService reviewService;

    /**
     * Lấy tất cả đánh giá với filter
     */
    @GetMapping
    public ResponseObject<Page<ReviewResponse>> getAllReviews(
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "rating", required = false) Integer rating,
            @RequestParam(value = "verified", required = false) Boolean verified,
            Pageable pageable) {
        
        Page<DanhGia> reviews;
        
        // Filter logic
        if (status != null && rating != null) {
            reviews = danhGiaRepository.findByTrangThaiDanhGiaAndSoSao(status, rating, pageable);
        } else if (status != null) {
            reviews = danhGiaRepository.findByTrangThaiDanhGia(status, pageable);
        } else if (rating != null) {
            reviews = danhGiaRepository.findBySoSao(rating, pageable);
        } else {
            reviews = danhGiaRepository.findAll(pageable);
        }
        
        return new ResponseObject<>(reviews.map(review -> convertToResponse(review)));
    }

    /**
     * Thống kê đánh giá
     */
    @GetMapping("/statistics")
    public ResponseObject<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        long totalReviews = danhGiaRepository.count();
        long pendingReviews = danhGiaRepository.countByTrangThaiDanhGia(0);
        long approvedReviews = danhGiaRepository.countByTrangThaiDanhGia(1);
        long rejectedReviews = danhGiaRepository.countByTrangThaiDanhGia(2);
        
        stats.put("total", totalReviews);
        stats.put("pending", pendingReviews);
        stats.put("approved", approvedReviews);
        stats.put("rejected", rejectedReviews);
        
        // Tính trung bình rating
        Double avgRating = danhGiaRepository.findAll().stream()
                .filter(r -> r.getTrangThaiDanhGia() == 1)
                .mapToInt(DanhGia::getSoSao)
                .average()
                .orElse(0.0);
        stats.put("averageRating", Math.round(avgRating * 10.0) / 10.0);
        
        // Rating distribution
        Map<Integer, Long> ratingDistribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            long count = danhGiaRepository.countByTrangThaiDanhGiaAndSoSao(1, i);
            ratingDistribution.put(i, count);
        }
        stats.put("ratingDistribution", ratingDistribution);
        
        return new ResponseObject<>(stats);
    }

    /**
     * Duyệt đánh giá (approve)
     */
    @PutMapping("/{reviewId}/approve")
    @Transactional
    public ResponseObject<String> approveReview(@PathVariable UUID reviewId) {
        DanhGia review = danhGiaRepository.findById(reviewId)
                .orElseThrow(() -> new ApiException("Không tìm thấy đánh giá"));
        
        review.setTrangThaiDanhGia(1); // 1 = approved
        danhGiaRepository.save(review);
        
        return new ResponseObject<>("Đã duyệt đánh giá thành công");
    }

    /**
     * Từ chối đánh giá (reject)
     */
    @PutMapping("/{reviewId}/reject")
    @Transactional
    public ResponseObject<String> rejectReview(@PathVariable UUID reviewId) {
        DanhGia review = danhGiaRepository.findById(reviewId)
                .orElseThrow(() -> new ApiException("Không tìm thấy đánh giá"));
        
        review.setTrangThaiDanhGia(2); // 2 = rejected
        danhGiaRepository.save(review);
        
        return new ResponseObject<>("Đã từ chối đánh giá");
    }

    /**
     * Duyệt hàng loạt (bulk approve)
     */
    @PutMapping("/bulk-approve")
    @Transactional
    public ResponseObject<String> bulkApprove(@RequestBody List<UUID> reviewIds) {
        List<DanhGia> reviews = danhGiaRepository.findAllById(reviewIds);
        reviews.forEach(review -> review.setTrangThaiDanhGia(1));
        danhGiaRepository.saveAll(reviews);
        
        return new ResponseObject<>("Đã duyệt " + reviews.size() + " đánh giá");
    }

    /**
     * Từ chối hàng loạt (bulk reject)
     */
    @PutMapping("/bulk-reject")
    @Transactional
    public ResponseObject<String> bulkReject(@RequestBody List<UUID> reviewIds) {
        List<DanhGia> reviews = danhGiaRepository.findAllById(reviewIds);
        reviews.forEach(review -> review.setTrangThaiDanhGia(2));
        danhGiaRepository.saveAll(reviews);
        
        return new ResponseObject<>("Đã từ chối " + reviews.size() + " đánh giá");
    }

    /**
     * Phản hồi đánh giá từ admin
     */
    @PostMapping("/{reviewId}/reply")
    @Transactional
    public ResponseObject<String> replyToReview(
            @PathVariable UUID reviewId,
            @RequestHeader(value = "X-User-Id", required = false) UUID nhanVienId,
            @Valid @RequestBody AdminReplyRequest request) {
        
        if (nhanVienId == null) {
            throw new ApiException("Bạn cần đăng nhập với tài khoản nhân viên");
        }
        
        DanhGia review = danhGiaRepository.findById(reviewId)
                .orElseThrow(() -> new ApiException("Không tìm thấy đánh giá"));
        
        NhanVien nhanVien = nhanVienRepository.findById(nhanVienId)
                .orElseThrow(() -> new ApiException("Không tìm thấy nhân viên"));
        
        // Kiểm tra đã có phản hồi chưa
        if (phanHoiDanhGiaRepository.existsByDanhGia_Id(reviewId)) {
            throw new ApiException("Đánh giá này đã có phản hồi");
        }
        
        PhanHoiDanhGia phanHoi = new PhanHoiDanhGia();
        phanHoi.setDanhGia(review);
        phanHoi.setNhanVien(nhanVien);
        phanHoi.setNoiDung(request.getNoiDung());
        phanHoi.setNgayPhanHoi(Instant.now());
        
        phanHoiDanhGiaRepository.save(phanHoi);
        
        return new ResponseObject<>("Đã phản hồi đánh giá thành công");
    }

    /**
     * Xóa đánh giá
     */
    @DeleteMapping("/{reviewId}")
    @Transactional
    public ResponseObject<String> deleteReview(@PathVariable UUID reviewId) {
        DanhGia review = danhGiaRepository.findById(reviewId)
                .orElseThrow(() -> new ApiException("Không tìm thấy đánh giá"));
        
        danhGiaRepository.delete(review);
        
        return new ResponseObject<>("Đã xóa đánh giá");
    }

    // Helper method
    private ReviewResponse convertToResponse(DanhGia danhGia) {
        ReviewResponse response = new ReviewResponse();
        response.setId(danhGia.getId());
        response.setSoSao(danhGia.getSoSao());
        response.setNoiDung(danhGia.getNoiDung());
        response.setReviewTitle(danhGia.getReviewTitle());
        response.setNgayDanhGia(danhGia.getNgayDanhGia());
        response.setTrangThaiDanhGia(danhGia.getTrangThaiDanhGia());
        response.setIsVerifiedPurchase(danhGia.getIsVerifiedPurchase());
        response.setHelpfulCount(danhGia.getHelpfulCount());
        
        if (danhGia.getKhachHang() != null) {
            ReviewResponse.CustomerInfo customerInfo = new ReviewResponse.CustomerInfo();
            customerInfo.setId(danhGia.getKhachHang().getId());
            customerInfo.setTenKhachHang(danhGia.getKhachHang().getHoTen());
            response.setKhachHang(customerInfo);
        }
        
        return response;
    }
}
