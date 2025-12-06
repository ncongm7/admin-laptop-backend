package com.example.backendlaptop.repository;

import com.example.backendlaptop.entity.DiaDiemBaoHanh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DiaDiemBaoHanhRepository extends JpaRepository<DiaDiemBaoHanh, UUID> {
    List<DiaDiemBaoHanh> findByIsActiveTrue();
}

