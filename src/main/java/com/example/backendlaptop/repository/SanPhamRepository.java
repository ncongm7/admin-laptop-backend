package com.example.backendlaptop.repository;

import com.example.backendlaptop.entity.SanPham;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SanPhamRepository extends JpaRepository<SanPham, UUID> {
    
    Optional<SanPham> findByMaSanPham(String maSanPham);
    
    boolean existsByMaSanPham(String maSanPham);
    
    @Query("SELECT s FROM SanPham s WHERE s.trangThai = :trangThai")
    List<SanPham> findByTrangThai(@Param("trangThai") Integer trangThai);
    
    @Query("SELECT s FROM SanPham s WHERE s.trangThai = :trangThai")
    Page<SanPham> findByTrangThai(@Param("trangThai") Integer trangThai, Pageable pageable);
    
    @Query("SELECT s FROM SanPham s WHERE s.tenSanPham LIKE %:tenSanPham%")
    List<SanPham> findByTenSanPhamContaining(@Param("tenSanPham") String tenSanPham);
    
    @Query("SELECT s FROM SanPham s WHERE s.tenSanPham LIKE %:tenSanPham%")
    Page<SanPham> findByTenSanPhamContaining(@Param("tenSanPham") String tenSanPham, Pageable pageable);
    
    @Query("SELECT s FROM SanPham s WHERE s.trangThai = :trangThai AND s.tenSanPham LIKE %:tenSanPham%")
    List<SanPham> findByTrangThaiAndTenSanPhamContaining(@Param("trangThai") Integer trangThai, @Param("tenSanPham") String tenSanPham);
    
    @Query("SELECT s FROM SanPham s WHERE s.trangThai = :trangThai AND s.tenSanPham LIKE %:tenSanPham%")
    Page<SanPham> findByTrangThaiAndTenSanPhamContaining(@Param("trangThai") Integer trangThai, @Param("tenSanPham") String tenSanPham, Pageable pageable);
}
