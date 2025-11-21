package com.example.backendlaptop.repository;

import com.example.backendlaptop.entity.LichSuTraHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LichSuTraHangRepository extends JpaRepository<LichSuTraHang, UUID> {
    
    /**
     * Tìm lịch sử theo yêu cầu trả hàng, sắp xếp theo thời gian
     */
    @Query("SELECT l FROM LichSuTraHang l WHERE l.idYeuCauTraHang.id = :idYeuCauTraHang ORDER BY l.thoiGian DESC")
    List<LichSuTraHang> findByIdYeuCauTraHangOrderByThoiGianDesc(@Param("idYeuCauTraHang") UUID idYeuCauTraHang);
    
    /**
     * Tìm lịch sử theo nhân viên
     */
    List<LichSuTraHang> findByIdNhanVien_Id(UUID idNhanVien);
}

