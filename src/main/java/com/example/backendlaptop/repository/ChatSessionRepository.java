package com.example.backendlaptop.repository;

import com.example.backendlaptop.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, UUID> {
    
    /**
     * Tìm session theo conversation ID
     */
    Optional<ChatSession> findByConversationId(UUID conversationId);
    
    /**
     * Tìm sessions của khách hàng
     */
    List<ChatSession> findByKhachHangIdOrderByStartedAtDesc(UUID khachHangId);
    
    /**
     * Tìm sessions đang được bot xử lý
     */
    @Query("SELECT s FROM ChatSession s WHERE s.isBotHandling = true AND s.lastActivity < :cutoff")
    List<ChatSession> findStaleBotSessions(@Param("cutoff") Instant cutoff);
    
    /**
     * Tìm sessions đã escalate
     */
    List<ChatSession> findByIsEscalatedTrueOrderByEscalatedAtDesc();
    
    /**
     * Tìm sessions được assign cho nhân viên
     */
    List<ChatSession> findByNhanVienIdOrderByLastActivityDesc(UUID nhanVienId);
    
    /**
     * Đếm sessions đang được bot xử lý
     */
    long countByIsBotHandlingTrue();
    
    /**
     * Đếm sessions đã escalate
     */
    long countByIsEscalatedTrue();
}
