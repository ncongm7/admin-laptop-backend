package com.example.backendlaptop.repository.banhang;

import com.example.backendlaptop.entity.HoaDonChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface HoaDonChiTietRepository extends JpaRepository<HoaDonChiTiet, UUID> {
    
    @Query("SELECT COUNT(hdct) > 0 FROM HoaDonChiTiet hdct " +
           "WHERE hdct.chiTietSanPham.id = :chiTietSanPhamId " +
           "AND hdct.hoaDon.idKhachHang.id = :khachHangId")
    boolean existsByChiTietSanPham_IdAndIdDonHang_KhachHang_Id(
            @Param("chiTietSanPhamId") UUID chiTietSanPhamId,
            @Param("khachHangId") UUID khachHangId);
}
