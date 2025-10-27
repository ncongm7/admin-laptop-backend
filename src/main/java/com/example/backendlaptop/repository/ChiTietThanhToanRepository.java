package com.example.backendlaptop.repository;

import com.example.backendlaptop.entity.ChiTietThanhToan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ChiTietThanhToanRepository extends JpaRepository<ChiTietThanhToan, UUID> {
}

