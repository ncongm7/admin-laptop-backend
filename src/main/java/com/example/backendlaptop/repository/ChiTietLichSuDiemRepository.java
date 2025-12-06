package com.example.backendlaptop.repository;

import com.example.backendlaptop.entity.ChiTietLichSuDiem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChiTietLichSuDiemRepository extends JpaRepository<ChiTietLichSuDiem, UUID> {
    
    /**
     * Tìm chi tiết lịch sử điểm theo lich_su_diem_id
     */
    List<ChiTietLichSuDiem> findByLichSuDiem_Id(UUID lichSuDiemId);
    
    /**
     * Tìm chi tiết lịch sử điểm theo user_id, sắp xếp theo ngày trừ giảm dần
     */
    List<ChiTietLichSuDiem> findByUser_IdOrderByNgayTruDesc(UUID userId);
}

