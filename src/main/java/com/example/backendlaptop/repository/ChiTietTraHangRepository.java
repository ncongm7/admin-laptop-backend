package com.example.backendlaptop.repository;

import com.example.backendlaptop.entity.ChiTietTraHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChiTietTraHangRepository extends JpaRepository<ChiTietTraHang, UUID> {
    
    /**
     * Tìm chi tiết trả hàng theo yêu cầu trả hàng
     */
    List<ChiTietTraHang> findByIdYeuCauTraHang_Id(UUID idYeuCauTraHang);
    
    /**
     * Tìm chi tiết trả hàng theo hóa đơn chi tiết
     */
    List<ChiTietTraHang> findByIdHoaDonChiTiet_Id(UUID idHoaDonChiTiet);
}

