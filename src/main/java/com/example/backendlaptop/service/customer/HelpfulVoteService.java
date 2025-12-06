package com.example.backendlaptop.service.customer;

import com.example.backendlaptop.dto.review.HelpfulVoteRequest;
import com.example.backendlaptop.entity.DanhGia;
import com.example.backendlaptop.entity.HelpfulVote;
import com.example.backendlaptop.entity.KhachHang;
import com.example.backendlaptop.expection.ApiException;
import com.example.backendlaptop.repository.DanhGiaRepository;
import com.example.backendlaptop.repository.HelpfulVoteRepository;
import com.example.backendlaptop.repository.KhachHangRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HelpfulVoteService {

    private final HelpfulVoteRepository helpfulVoteRepository;
    private final DanhGiaRepository danhGiaRepository;
    private final KhachHangRepository khachHangRepository;

    /**
     * Vote cho một đánh giá (helpful hoặc not helpful)
     */
    @Transactional
    public Map<String, Object> voteReview(UUID reviewId, UUID userId, HelpfulVoteRequest request) {
        // Validate
        if (request.getIsHelpful() == null) {
            throw new ApiException("Vui lòng chọn vote helpful hoặc not helpful");
        }

        // Kiểm tra đánh giá tồn tại
        DanhGia danhGia = danhGiaRepository.findById(reviewId)
                .orElseThrow(() -> new ApiException("Không tìm thấy đánh giá"));

        // Kiểm tra user tồn tại
        KhachHang khachHang = khachHangRepository.findById(userId)
                .orElseThrow(() -> new ApiException("Không tìm thấy khách hàng"));

        // Kiểm tra user đã vote chưa
        HelpfulVote existingVote = helpfulVoteRepository.findByDanhGia_IdAndKhachHang_Id(reviewId, userId)
                .orElse(null);

        if (existingVote != null) {
            // Nếu vote cùng loại -> xóa vote (toggle)
            if (existingVote.getIsHelpful().equals(request.getIsHelpful())) {
                helpfulVoteRepository.delete(existingVote);
                updateHelpfulCount(danhGia);
                
                Map<String, Object> response = new HashMap<>();
                response.put("action", "removed");
                response.put("helpfulCount", danhGia.getHelpfulCount());
                return response;
            } else {
                // Nếu vote khác loại -> update vote
                existingVote.setIsHelpful(request.getIsHelpful());
                helpfulVoteRepository.save(existingVote);
                updateHelpfulCount(danhGia);
                
                Map<String, Object> response = new HashMap<>();
                response.put("action", "updated");
                response.put("isHelpful", request.getIsHelpful());
                response.put("helpfulCount", danhGia.getHelpfulCount());
                return response;
            }
        } else {
            // Tạo vote mới
            HelpfulVote newVote = new HelpfulVote();
            newVote.setDanhGia(danhGia);
            newVote.setKhachHang(khachHang);
            newVote.setIsHelpful(request.getIsHelpful());
            helpfulVoteRepository.save(newVote);
            updateHelpfulCount(danhGia);
            
            Map<String, Object> response = new HashMap<>();
            response.put("action", "created");
            response.put("isHelpful", request.getIsHelpful());
            response.put("helpfulCount", danhGia.getHelpfulCount());
            return response;
        }
    }

    /**
     * Cập nhật helpful_count cho đánh giá
     */
    private void updateHelpfulCount(DanhGia danhGia) {
        Long helpfulCount = helpfulVoteRepository.countHelpfulVotes(danhGia.getId());
        danhGia.setHelpfulCount(helpfulCount.intValue());
        danhGiaRepository.save(danhGia);
    }

    /**
     * Lấy thống kê vote của một đánh giá
     */
    public Map<String, Long> getVoteStats(UUID reviewId) {
        Long helpfulCount = helpfulVoteRepository.countHelpfulVotes(reviewId);
        Long notHelpfulCount = helpfulVoteRepository.countNotHelpfulVotes(reviewId);
        
        Map<String, Long> stats = new HashMap<>();
        stats.put("helpful", helpfulCount);
        stats.put("notHelpful", notHelpfulCount);
        stats.put("total", helpfulCount + notHelpfulCount);
        return stats;
    }
}
