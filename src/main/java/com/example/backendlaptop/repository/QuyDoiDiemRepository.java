package com.example.backendlaptop.repository;

import com.example.backendlaptop.entity.QuyDoiDiem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuyDoiDiemRepository extends JpaRepository<QuyDoiDiem, UUID> {
    
    /**
     * Tìm quy đổi điểm đang hoạt động (trangThai = 1)
     */
    List<QuyDoiDiem> findByTrangThai(Integer trangThai);
    
    /**
     * Tìm quy đổi điểm đang hoạt động đầu tiên
     */
    QuyDoiDiem findFirstByTrangThaiOrderByIdAsc(Integer trangThai);
}

