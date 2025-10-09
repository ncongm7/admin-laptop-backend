package com.example.backendlaptop.repository;

import com.example.backendlaptop.entity.GioHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GioHangRepository extends JpaRepository<GioHang, UUID> {
    @Query("SELECT gh FROM GioHang gh JOIN FETCH gh.khachHang")
    List<GioHang> findAllWithKhachHang();
}
