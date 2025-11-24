package com.example.backendlaptop.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket Configuration cho STOMP messaging
 * Hỗ trợ real-time notifications cho pending orders
 * 
 * Lưu ý: 
 * - Cần đảm bảo dependency spring-boot-starter-websocket đã được tải đúng cách
 * - Chạy: mvn clean install hoặc rebuild project trong IDE
 * - Nếu gặp lỗi "WebSocketMessageBrokerConfigurer.class cannot be opened", 
 *   hãy rebuild project hoặc tải lại dependencies
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable a simple in-memory message broker to carry messages back to the client
        // on destinations prefixed with "/topic" and "/queue"
        config.enableSimpleBroker("/topic", "/queue");
        
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
                .withSockJS();
    }
}

