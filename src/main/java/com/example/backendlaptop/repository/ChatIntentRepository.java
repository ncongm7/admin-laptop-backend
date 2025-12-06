package com.example.backendlaptop.repository;

import com.example.backendlaptop.entity.ChatIntent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatIntentRepository extends JpaRepository<ChatIntent, Integer> {
    
    /**
     * Tìm tất cả intent đang active, sắp xếp theo priority giảm dần
     */
    List<ChatIntent> findByIsActiveTrueOrderByPriorityDesc();
    
    /**
     * Tìm intent theo category
     */
    List<ChatIntent> findByCategoryAndIsActiveTrue(String category);
    
    /**
     * Tìm intent theo code
     */
    Optional<ChatIntent> findByIntentCode(String intentCode);
    
    /**
     * Tìm intent theo code và active
     */
    Optional<ChatIntent> findByIntentCodeAndIsActiveTrue(String intentCode);
    
    /**
     * Đếm số intent theo category
     */
    long countByCategoryAndIsActiveTrue(String category);
}
