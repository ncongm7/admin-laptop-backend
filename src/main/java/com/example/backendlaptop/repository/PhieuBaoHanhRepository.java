package com.example.backendlaptop.repository;

import com.example.backendlaptop.entity.PhieuBaoHanh;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PhieuBaoHanhRepository extends JpaRepository<PhieuBaoHanh, UUID> {
}
