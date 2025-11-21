package com.example.backendlaptop.repository;

import com.example.backendlaptop.entity.DotGiamGia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface DotGiamGiaRepository extends JpaRepository<DotGiamGia, UUID> {
    List<DotGiamGia> findByTenKm(String tenKm);
    Page<DotGiamGia> findByTrangThai(Integer trangThai, Pageable pageable);
    
    // Tìm các chương trình đang diễn ra (active)
    Page<DotGiamGia> findByTrangThaiAndNgayBatDauLessThanEqualAndNgayKetThucGreaterThanEqual(
        Integer trangThai, Instant ngayBatDau, Instant ngayKetThuc, Pageable pageable);
    
    // Tìm các chương trình sắp diễn ra (upcoming)
    Page<DotGiamGia> findByTrangThaiAndNgayBatDauAfter(
        Integer trangThai, Instant ngayBatDau, Pageable pageable);
    
    // Tìm các chương trình đang diễn ra có banner (không phân trang)
    List<DotGiamGia> findByTrangThaiAndNgayBatDauLessThanEqualAndNgayKetThucGreaterThanEqualAndBannerImageUrlIsNotNull(
        Integer trangThai, Instant ngayBatDau, Instant ngayKetThuc);
}
