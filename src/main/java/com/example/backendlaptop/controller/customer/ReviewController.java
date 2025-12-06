package com.example.backendlaptop.controller.customer;

import com.example.backendlaptop.dto.review.HelpfulVoteRequest;
import com.example.backendlaptop.dto.review.ReviewRequest;
import com.example.backendlaptop.dto.review.ReviewResponse;
import com.example.backendlaptop.model.response.ResponseObject;
import com.example.backendlaptop.service.customer.HelpfulVoteService;
import com.example.backendlaptop.service.customer.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final HelpfulVoteService helpfulVoteService;

    @GetMapping("/articles")
    public ResponseObject<?> getTopReviews(@RequestParam(value = "limit", defaultValue = "4") Integer limit) {
        return new ResponseObject<>(reviewService.getTopReviews(limit));
    }

    @GetMapping("/products/{productId}/reviews")
    public ResponseObject<Page<ReviewResponse>> getProductReviews(
            @PathVariable UUID productId,
            @RequestParam(value = "filter", required = false) Integer rating,
            @RequestHeader(value = "X-User-Id", required = false) UUID currentUserId,
            Pageable pageable) {
        Page<ReviewResponse> reviews = reviewService.getProductReviews(productId, rating, pageable, currentUserId);
        return new ResponseObject<>(reviews);
    }

    @PostMapping("/products/{productId}/reviews")
    public ResponseObject<ReviewResponse> submitReview(
            @PathVariable UUID productId,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @Valid @RequestBody ReviewRequest request) {
        // TODO: Lấy userId từ authentication token thay vì header
        if (userId == null) {
            throw new com.example.backendlaptop.expection.ApiException("Bạn cần đăng nhập để đánh giá");
        }
        ReviewResponse response = reviewService.submitReview(productId, userId, request);
        return new ResponseObject<>(response);
    }

    /**
     * Vote helpful/not helpful cho đánh giá
     */
    @PostMapping("/reviews/{reviewId}/vote")
    public ResponseObject<Map<String, Object>> voteReview(
            @PathVariable UUID reviewId,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @Valid @RequestBody HelpfulVoteRequest request) {
        if (userId == null) {
            throw new com.example.backendlaptop.expection.ApiException("Bạn cần đăng nhập để vote");
        }
        Map<String, Object> result = helpfulVoteService.voteReview(reviewId, userId, request);
        return new ResponseObject<>(result);
    }

    /**
     * Lấy thống kê vote của đánh giá
     */
    @GetMapping("/reviews/{reviewId}/vote-stats")
    public ResponseObject<Map<String, Long>> getVoteStats(@PathVariable UUID reviewId) {
        Map<String, Long> stats = helpfulVoteService.getVoteStats(reviewId);
        return new ResponseObject<>(stats);
    }
}


