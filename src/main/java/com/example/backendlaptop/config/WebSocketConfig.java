package com.example.backendlaptop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket Configuration cho STOMP messaging
 * Hỗ trợ real-time chat giữa admin và khách hàng
 * 
 * Cải tiến:
 * - Heartbeat configuration tốt hơn để duy trì kết nối ổn định
 * - Connection timeout handling
 * - SockJS fallback cho các trình duyệt không hỗ trợ WebSocket
 * - TaskScheduler cho heartbeat mechanism
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * TaskScheduler cho heartbeat mechanism
     * Cần thiết khi sử dụng setHeartbeatValue()
     */
    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("websocket-heartbeat-");
        scheduler.initialize();
        return scheduler;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable a simple in-memory message broker to carry messages back to the client
        // on destinations prefixed with "/topic" and "/queue"
        config.enableSimpleBroker("/topic", "/queue")
                // Heartbeat: Gửi heartbeat mỗi 10 giây để duy trì kết nối
                .setHeartbeatValue(new long[]{10000, 10000})
                // Set TaskScheduler cho heartbeat
                .setTaskScheduler(taskScheduler());
        
        // Prefix for messages that are bound to methods annotated with @MessageMapping
        config.setApplicationDestinationPrefixes("/app");
        
        // Prefix for user-specific destinations
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register "/ws" endpoint, enabling SockJS fallback options
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // Allow all origins (adjust for production)
                // SockJS options để cải thiện reconnection
                .withSockJS()
                .setHeartbeatTime(25000) // Heartbeat interval: 25 seconds
                .setDisconnectDelay(5000) // Delay trước khi disconnect: 5 seconds
                .setSessionCookieNeeded(false); // Không cần session cookie
    }
}

