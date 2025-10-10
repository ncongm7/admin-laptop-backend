package com.example.backendlaptop.repository;

import com.example.backendlaptop.entity.ChiTietSanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChiTietSanPhamRepository extends JpaRepository<ChiTietSanPham, UUID> {
    
    List<ChiTietSanPham> findBySanPham_Id(UUID sanPhamId);
    
    boolean existsByMaCtsp(String maCtsp);
}
