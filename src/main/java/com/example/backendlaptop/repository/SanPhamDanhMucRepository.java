package com.example.backendlaptop.repository;

import com.example.backendlaptop.entity.SanPham;
import com.example.backendlaptop.entity.SanphamDanhmuc;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SanPhamDanhMucRepository extends JpaRepository<SanphamDanhmuc, UUID> {
    
    @Query("SELECT sd FROM SanphamDanhmuc sd WHERE sd.idSanPham.id = :idSanPham")
    List<SanphamDanhmuc> findByIdSanPham(@Param("idSanPham") UUID idSanPham);
    
    @Query("SELECT sd FROM SanphamDanhmuc sd WHERE sd.idDanhMuc.id = :idDanhMuc")
    List<SanphamDanhmuc> findByIdDanhMuc(@Param("idDanhMuc") UUID idDanhMuc);
    
    @Query("SELECT DISTINCT sd.idSanPham FROM SanphamDanhmuc sd " +
           "WHERE sd.idDanhMuc.id IN :categoryIds AND sd.idSanPham.id <> :currentProductId")
    List<SanPham> findRelatedProductsByCategoryIds(@Param("categoryIds") List<UUID> categoryIds,
                                                   @Param("currentProductId") UUID currentProductId,
                                                   Pageable pageable);
}

