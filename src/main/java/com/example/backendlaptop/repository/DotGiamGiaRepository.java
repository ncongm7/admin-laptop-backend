package com.example.backendlaptop.repository;

import com.example.backendlaptop.entity.DotGiamGia;
import com.example.backendlaptop.entity.DotGiamGiaChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DotGiamGiaRepository extends JpaRepository<DotGiamGia, UUID> {
    List<DotGiamGia> findByTenKm(String tenKm);
}
