package com.example.backendlaptop.repository;

import com.example.backendlaptop.entity.LichSuDiem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LichSuDiemRepository extends JpaRepository<LichSuDiem, UUID> {
    
    /**
     * Tìm lịch sử điểm theo user_id, sắp xếp theo thời gian giảm dần (mới nhất trước)
     */
    Page<LichSuDiem> findByTichDiem_User_IdOrderByThoiGianDesc(UUID userId, Pageable pageable);
    
    /**
     * Tìm lịch sử điểm theo hóa đơn
     */
    List<LichSuDiem> findByHoaDonId(UUID hoaDonId);
    
    /**
     * Tìm lịch sử điểm theo loại điểm và user_id
     */
    List<LichSuDiem> findByLoaiDiemAndTichDiem_User_Id(Integer loaiDiem, UUID userId);
    
    /**
     * Tìm lịch sử điểm theo trạng thái và user_id
     */
    List<LichSuDiem> findByTrangThaiAndTichDiem_User_Id(Integer trangThai, UUID userId);
}

