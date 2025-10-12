package com.example.backendlaptop.repository;

import com.example.backendlaptop.entity.DotGiamGiaChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DotGiamGiaChiTietRepository extends JpaRepository<DotGiamGiaChiTiet, UUID> {
    List<DotGiamGiaChiTiet> findAllByDotGiamGia_Id(UUID dotGiamGiaId);
}
