// FILE: src/main/java/com/example/backendlaptop/repository/PhieuGiamGiaKhachHangRepository.java
package com.example.backendlaptop.repository;

import com.example.backendlaptop.entity.PhieuGiamGiaKhachHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PhieuGiamGiaKhachHangRepository extends JpaRepository<PhieuGiamGiaKhachHang, Long> {
    
    List<PhieuGiamGiaKhachHang> findByPhieuGiamGia_Id(UUID phieuGiamGiaId);
    
    List<PhieuGiamGiaKhachHang> findByKhachHang_Id(UUID khachHangId);
    
    boolean existsByPhieuGiamGia_IdAndKhachHang_Id(UUID phieuGiamGiaId, UUID khachHangId);
    
    void deleteByPhieuGiamGia_IdAndKhachHang_Id(UUID phieuGiamGiaId, UUID khachHangId);
    
    @Query("SELECT pkgkh FROM PhieuGiamGiaKhachHang pkgkh WHERE pkgkh.khachHang.id = :khachHangId")
    List<PhieuGiamGiaKhachHang> findAllByKhachHangId(@Param("khachHangId") UUID khachHangId);
}

