package com.example.backendlaptop.repository;

import com.example.backendlaptop.entity.HoaDon;
import com.example.backendlaptop.entity.LichSuHoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LichSuHoaDonRepository extends JpaRepository<LichSuHoaDon, UUID> {

    // Xóa lịch sử theo hóa đơn
    @Modifying
    @Query("DELETE FROM LichSuHoaDon l WHERE l.idHoaDon = :hoaDon")
    void deleteByIdHoaDon(HoaDon hoaDon);
}
