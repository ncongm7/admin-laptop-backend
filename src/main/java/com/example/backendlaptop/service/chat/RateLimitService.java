package com.example.backendlaptop.service.chat;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Rate limiting service for chat messages
 * Uses in-memory cache with sliding window
 */
@Service
@Slf4j
public class RateLimitService {

    // Store message counts per user (userId -> count in current window)
    private final Map<UUID, MessageWindow> userMessageCounts = new ConcurrentHashMap<>();
    
    // Cleanup scheduler
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    // Rate limits
    private static final int CUSTOMER_LIMIT = 10; // 10 messages per minute
    private static final int STAFF_LIMIT = 30; // 30 messages per minute
    private static final long WINDOW_SIZE_SECONDS = 60; // 1 minute window

    public RateLimitService() {
        // Cleanup expired entries every 5 minutes
        scheduler.scheduleAtFixedRate(this::cleanupExpiredEntries, 5, 5, TimeUnit.MINUTES);
    }

    /**
     * Check if user can send message
     * @param userId User ID
     * @param isCustomer true if customer, false if staff
     * @return true if allowed, false if rate limit exceeded
     */
    public boolean isAllowed(UUID userId, boolean isCustomer) {
        int limit = isCustomer ? CUSTOMER_LIMIT : STAFF_LIMIT;
        
        MessageWindow window = userMessageCounts.computeIfAbsent(userId, k -> new MessageWindow());
        
        // Remove old entries outside the window
        Instant cutoff = Instant.now().minusSeconds(WINDOW_SIZE_SECONDS);
        window.cleanup(cutoff);
        
        // Check if limit exceeded
        if (window.getCount() >= limit) {
            log.warn("Rate limit exceeded for user {} ({} messages in {} seconds)", 
                    userId, window.getCount(), WINDOW_SIZE_SECONDS);
            return false;
        }
        
        // Add current message timestamp
        window.addMessage(Instant.now());
        return true;
    }

    /**
     * Get remaining messages for user
     */
    public int getRemainingMessages(UUID userId, boolean isCustomer) {
        int limit = isCustomer ? CUSTOMER_LIMIT : STAFF_LIMIT;
        MessageWindow window = userMessageCounts.get(userId);
        
        if (window == null) {
            return limit;
        }
        
        Instant cutoff = Instant.now().minusSeconds(WINDOW_SIZE_SECONDS);
        window.cleanup(cutoff);
        
        return Math.max(0, limit - window.getCount());
    }

    /**
     * Cleanup expired entries
     */
    private void cleanupExpiredEntries() {
        Instant cutoff = Instant.now().minusSeconds(WINDOW_SIZE_SECONDS * 2); // Keep 2 windows
        
        userMessageCounts.entrySet().removeIf(entry -> {
            entry.getValue().cleanup(cutoff);
            return entry.getValue().isEmpty();
        });
    }

    /**
     * Inner class to track message window
     */
    private static class MessageWindow {
        private final java.util.List<Instant> timestamps = new java.util.ArrayList<>();

        public synchronized void addMessage(Instant timestamp) {
            timestamps.add(timestamp);
        }

        public synchronized void cleanup(Instant cutoff) {
            timestamps.removeIf(ts -> ts.isBefore(cutoff));
        }

        public synchronized int getCount() {
            return timestamps.size();
        }

        public synchronized boolean isEmpty() {
            return timestamps.isEmpty();
        }
    }
}

