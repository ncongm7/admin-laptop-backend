package com.example.backendlaptop.repository;

import com.example.backendlaptop.entity.MediaDanhGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MediaDanhGiaRepository extends JpaRepository<MediaDanhGia, UUID> {
    
    /**
     * Tìm tất cả media của một đánh giá
     */
    List<MediaDanhGia> findByDanhGia_IdAndTrangThaiMediaDanhGia(UUID danhGiaId, Integer trangThai);
    
    /**
     * Tìm media theo loại (1=image, 2=video)
     */
    List<MediaDanhGia> findByDanhGia_IdAndLoaiMediaAndTrangThaiMediaDanhGia(
            UUID danhGiaId, Integer loaiMedia, Integer trangThai);
    
    /**
     * Đếm số lượng media của một đánh giá
     */
    @Query("SELECT COUNT(m) FROM MediaDanhGia m WHERE m.danhGia.id = :danhGiaId AND m.trangThaiMediaDanhGia = 1")
    Long countByDanhGiaId(@Param("danhGiaId") UUID danhGiaId);
}
