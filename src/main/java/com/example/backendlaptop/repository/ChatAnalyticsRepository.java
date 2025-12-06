package com.example.backendlaptop.repository;

import com.example.backendlaptop.entity.ChatAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
public interface ChatAnalyticsRepository extends JpaRepository<ChatAnalytics, Long> {
    
    /**
     * Tìm analytics theo conversation
     */
    List<ChatAnalytics> findByConversationIdOrderByCreatedAtDesc(UUID conversationId);
    
    /**
     * Thống kê top intents
     */
    @Query("SELECT c.intentDetected as intent, COUNT(c) as count " +
           "FROM ChatAnalytics c " +
           "WHERE c.createdAt >= :startDate AND c.intentDetected IS NOT NULL " +
           "GROUP BY c.intentDetected " +
           "ORDER BY COUNT(c) DESC")
    List<Map<String, Object>> getTopIntents(@Param("startDate") Instant startDate);
    
    /**
     * Tính trung bình response time
     */
    @Query("SELECT AVG(c.responseTimeMs) FROM ChatAnalytics c WHERE c.createdAt >= :startDate")
    Double getAverageResponseTime(@Param("startDate") Instant startDate);
    
    /**
     * Đếm số tin nhắn tự động
     */
    long countByWasAutoRespondedTrueAndCreatedAtGreaterThanEqual(Instant startDate);
    
    /**
     * Đếm tổng tin nhắn
     */
    long countByCreatedAtGreaterThanEqual(Instant startDate);
}
