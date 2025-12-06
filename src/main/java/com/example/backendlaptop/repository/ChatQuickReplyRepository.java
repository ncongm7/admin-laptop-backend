package com.example.backendlaptop.repository;

import com.example.backendlaptop.entity.ChatQuickReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatQuickReplyRepository extends JpaRepository<ChatQuickReply, Integer> {
    
    /**
     * Tìm quick replies theo intent code
     */
    List<ChatQuickReply> findByIntentCodeAndIsActiveTrueOrderByDisplayOrderAsc(String intentCode);
    
    /**
     * Tìm tất cả quick replies active
     */
    List<ChatQuickReply> findByIsActiveTrueOrderByDisplayOrderAsc();
}
