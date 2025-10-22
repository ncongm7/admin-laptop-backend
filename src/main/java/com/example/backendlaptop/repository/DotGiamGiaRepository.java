package com.example.backendlaptop.repository;

import com.example.backendlaptop.entity.DotGiamGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DotGiamGiaRepository extends JpaRepository<DotGiamGia, UUID> {
    List<DotGiamGia> findByTenKm(String tenKm);
}
