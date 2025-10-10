package com.example.backendlaptop.repository;

import com.example.backendlaptop.entity.HoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HoaDonRepository extends JpaRepository<HoaDon, Long> {
    // Tìm các hóa đơn theo trạng thái
    List<HoaDon> findByTrangThai(Integer trangThai);
}
