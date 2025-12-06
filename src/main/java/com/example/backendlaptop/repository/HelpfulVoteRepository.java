package com.example.backendlaptop.repository;

import com.example.backendlaptop.entity.HelpfulVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface HelpfulVoteRepository extends JpaRepository<HelpfulVote, UUID> {
    
    /**
     * Tìm vote của khách hàng cho một đánh giá
     */
    Optional<HelpfulVote> findByDanhGia_IdAndKhachHang_Id(UUID danhGiaId, UUID khachHangId);
    
    /**
     * Kiểm tra khách hàng đã vote chưa
     */
    boolean existsByDanhGia_IdAndKhachHang_Id(UUID danhGiaId, UUID khachHangId);
    
    /**
     * Đếm số lượng vote helpful (isHelpful = true)
     */
    @Query("SELECT COUNT(h) FROM HelpfulVote h WHERE h.danhGia.id = :danhGiaId AND h.isHelpful = true")
    Long countHelpfulVotes(@Param("danhGiaId") UUID danhGiaId);
    
    /**
     * Đếm số lượng vote not helpful (isHelpful = false)
     */
    @Query("SELECT COUNT(h) FROM HelpfulVote h WHERE h.danhGia.id = :danhGiaId AND h.isHelpful = false")
    Long countNotHelpfulVotes(@Param("danhGiaId") UUID danhGiaId);
}
