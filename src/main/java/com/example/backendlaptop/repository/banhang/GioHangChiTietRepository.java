package com.example.backendlaptop.repository.banhang;

import com.example.backendlaptop.entity.GioHangChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GioHangChiTietRepository extends JpaRepository<GioHangChiTiet, UUID> {
    List<GioHangChiTiet> findByGioHangId(UUID gioHangId);
    
    Optional<GioHangChiTiet> findByGioHangIdAndChiTietSanPhamId(UUID gioHangId, UUID chiTietSanPhamId);
    
    void deleteByGioHangId(UUID gioHangId);
}
