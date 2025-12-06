package com.example.backendlaptop.repository;

import com.example.backendlaptop.entity.PhanHoiDanhGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PhanHoiDanhGiaRepository extends JpaRepository<PhanHoiDanhGia, UUID> {
    
    /**
     * Tìm tất cả phản hồi của một đánh giá
     */
    List<PhanHoiDanhGia> findByDanhGia_Id(UUID danhGiaId);
    
    /**
     * Tìm phản hồi đầu tiên của một đánh giá (thường là phản hồi từ admin)
     */
    Optional<PhanHoiDanhGia> findFirstByDanhGia_IdOrderByNgayPhanHoiAsc(UUID danhGiaId);
    
    /**
     * Kiểm tra đánh giá đã có phản hồi chưa
     */
    boolean existsByDanhGia_Id(UUID danhGiaId);
}
