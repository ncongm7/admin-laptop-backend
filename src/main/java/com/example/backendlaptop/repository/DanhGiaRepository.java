package com.example.backendlaptop.repository;

import com.example.backendlaptop.entity.DanhGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DanhGiaRepository extends JpaRepository<DanhGia, UUID> {
    
    List<DanhGia> findByTrangThaiDanhGia(Integer trangThaiDanhGia);
    
    @Query("SELECT d FROM DanhGia d WHERE d.trangThaiDanhGia = :trangThai ORDER BY d.ngayDanhGia DESC")
    List<DanhGia> findTopByTrangThaiDanhGiaOrderByNgayDanhGiaDesc(@Param("trangThai") Integer trangThai, org.springframework.data.domain.Pageable pageable);
    
    @Query("SELECT d FROM DanhGia d WHERE d.sanPhamChiTiet.id IN :variantIds AND d.trangThaiDanhGia = :trangThai")
    List<DanhGia> findBySanPhamChiTiet_IdInAndTrangThaiDanhGia(@Param("variantIds") List<java.util.UUID> variantIds, @Param("trangThai") Integer trangThai);
    
    @Query("SELECT d FROM DanhGia d WHERE d.sanPhamChiTiet.id IN :variantIds AND d.trangThaiDanhGia = :trangThai AND (:rating IS NULL OR d.soSao = :rating) ORDER BY d.ngayDanhGia DESC")
    org.springframework.data.domain.Page<DanhGia> findBySanPhamChiTiet_IdInAndTrangThaiDanhGiaAndRating(
            @Param("variantIds") List<java.util.UUID> variantIds, 
            @Param("trangThai") Integer trangThai,
            @Param("rating") Integer rating,
            org.springframework.data.domain.Pageable pageable);
}

