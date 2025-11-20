package com.example.backendlaptop.repository;

import com.example.backendlaptop.entity.TichDiem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TichDiemRepository extends JpaRepository<TichDiem, UUID> {
    
    /**
     * Tìm điểm tích lũy theo user_id (khách hàng)
     */
    Optional<TichDiem> findByUser_Id(UUID userId);
}

