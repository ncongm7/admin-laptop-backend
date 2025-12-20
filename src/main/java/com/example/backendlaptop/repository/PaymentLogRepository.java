package com.example.backendlaptop.repository;

import com.example.backendlaptop.entity.PaymentLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentLogRepository extends JpaRepository<PaymentLog, UUID> {
    
    /**
     * Tìm tất cả payment logs của một hóa đơn, sắp xếp theo thời gian tạo giảm dần
     */
    List<PaymentLog> findByHoaDon_IdOrderByCreatedAtDesc(UUID hoaDonId);
    
    /**
     * Tìm payment logs theo status
     */
    List<PaymentLog> findByStatus(String status);
}
