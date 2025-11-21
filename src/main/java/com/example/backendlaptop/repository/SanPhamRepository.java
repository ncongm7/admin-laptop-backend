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
    
    // Advanced search - Tìm kiếm theo mã hoặc tên
    @Query("SELECT DISTINCT s FROM SanPham s " +
           "WHERE (:keyword IS NULL OR :keyword = '' OR s.maSanPham LIKE %:keyword% OR s.tenSanPham LIKE %:keyword%)")
    List<SanPham> searchByMaOrTen(@Param("keyword") String keyword);
    
    @Query("SELECT DISTINCT s FROM SanPham s " +
           "WHERE (:keyword IS NULL OR :keyword = '' OR s.maSanPham LIKE %:keyword% OR s.tenSanPham LIKE %:keyword%)")
    Page<SanPham> searchByMaOrTen(@Param("keyword") String keyword, Pageable pageable);
    
    // Tìm kiếm nâng cao với tất cả các bộ lọc
    @Query("SELECT DISTINCT s FROM SanPham s " +
           "LEFT JOIN ChiTietSanPham ctsp ON ctsp.sanPham.id = s.id " +
           "WHERE (:keyword IS NULL OR :keyword = '' OR s.maSanPham LIKE %:keyword% OR s.tenSanPham LIKE %:keyword%) " +
           "AND (:trangThai IS NULL OR s.trangThai = :trangThai) " +
           "AND (:minPrice IS NULL OR ctsp.giaBan >= :minPrice) " +
           "AND (:maxPrice IS NULL OR ctsp.giaBan <= :maxPrice)")
    List<SanPham> advancedSearch(
        @Param("keyword") String keyword,
        @Param("trangThai") Integer trangThai,
        @Param("minPrice") Long minPrice,
        @Param("maxPrice") Long maxPrice
    );
    
    @Query("SELECT DISTINCT s FROM SanPham s " +
           "LEFT JOIN ChiTietSanPham ctsp ON ctsp.sanPham.id = s.id " +
           "WHERE (:keyword IS NULL OR :keyword = '' OR s.maSanPham LIKE %:keyword% OR s.tenSanPham LIKE %:keyword%) " +
           "AND (:trangThai IS NULL OR s.trangThai = :trangThai) " +
           "AND (:minPrice IS NULL OR ctsp.giaBan >= :minPrice) " +
           "AND (:maxPrice IS NULL OR ctsp.giaBan <= :maxPrice)")
    Page<SanPham> advancedSearch(
        @Param("keyword") String keyword,
        @Param("trangThai") Integer trangThai,
        @Param("minPrice") Long minPrice,
        @Param("maxPrice") Long maxPrice,
        Pageable pageable
    );
    
    // Lấy sản phẩm còn hàng (trạng thái = 1 và tồn kho > 0)
    @Query("SELECT DISTINCT s FROM SanPham s " +
           "INNER JOIN ChiTietSanPham ctsp ON ctsp.sanPham.id = s.id " +
           "WHERE s.trangThai = 1 " +
           "AND ctsp.soLuongTon > 0")
    List<SanPham> findSanPhamConHang();
    
    @Query("SELECT DISTINCT s FROM SanPham s " +
           "INNER JOIN ChiTietSanPham ctsp ON ctsp.sanPham.id = s.id " +
           "WHERE s.trangThai = 1 " +
           "AND ctsp.soLuongTon > 0")
    Page<SanPham> findSanPhamConHang(Pageable pageable);

    // Lấy sản phẩm mới nhất
    @Query("SELECT s FROM SanPham s WHERE s.trangThai = :trangThai ORDER BY s.ngayTao DESC")
    List<SanPham> findTopByTrangThaiOrderByNgayTaoDesc(@Param("trangThai") Integer trangThai, Pageable pageable);
}
