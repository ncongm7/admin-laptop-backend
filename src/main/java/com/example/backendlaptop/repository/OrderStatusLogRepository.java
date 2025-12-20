package com.example.backendlaptop.repository;

import com.example.backendlaptop.entity.OrderStatusLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderStatusLogRepository extends JpaRepository<OrderStatusLog, UUID> {
    
    /**
     * Tìm tất cả status logs của một hóa đơn, sắp xếp theo thời gian thay đổi giảm dần
     */
    List<OrderStatusLog> findByHoaDon_IdOrderByChangedAtDesc(UUID hoaDonId);
    
    /**
     * Tìm status logs theo người thay đổi
     */
    List<OrderStatusLog> findByChangedByOrderByChangedAtDesc(UUID userId);
}
