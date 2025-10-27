package com.example.backendlaptop.repository;

import com.example.backendlaptop.entity.LoaiManHinh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LoaiManHinhRepository extends JpaRepository<LoaiManHinh, UUID> {
    
    Optional<LoaiManHinh> findByMaLoaiManHinh(String maLoaiManHinh);
    
    boolean existsByMaLoaiManHinh(String maLoaiManHinh);
    
    List<LoaiManHinh> findByTrangThai(Integer trangThai);
}
