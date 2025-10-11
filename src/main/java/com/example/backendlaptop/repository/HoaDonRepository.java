package com.example.backendlaptop.repository;

import com.example.backendlaptop.entity.HoaDon;
import com.example.backendlaptop.model.TrangThaiHoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HoaDonRepository extends JpaRepository<HoaDon, UUID> {
    // Tìm các hóa đơn theo trạng thái
    List<HoaDon> findByTrangThai(TrangThaiHoaDon trangThai);
}
