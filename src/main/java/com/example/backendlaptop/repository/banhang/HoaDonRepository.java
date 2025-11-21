package com.example.backendlaptop.repository.banhang;

import com.example.backendlaptop.entity.HoaDon;
import com.example.backendlaptop.model.TrangThaiHoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HoaDonRepository extends JpaRepository<HoaDon, UUID>, JpaSpecificationExecutor<HoaDon> {
    // Tìm các hóa đơn theo trạng thái
    List<HoaDon> findByTrangThai(TrangThaiHoaDon trangThai);
    
    // Đếm số lượng hóa đơn theo trạng thái
    Long countByTrangThai(TrangThaiHoaDon trangThai);
}
