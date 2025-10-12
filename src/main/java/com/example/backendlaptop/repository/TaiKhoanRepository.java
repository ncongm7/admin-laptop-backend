package com.example.backendlaptop.repository;

import com.example.backendlaptop.entity.TaiKhoan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TaiKhoanRepository extends JpaRepository<TaiKhoan, UUID> {
}
