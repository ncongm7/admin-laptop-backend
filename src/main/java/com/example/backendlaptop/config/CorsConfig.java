package com.example.backendlaptop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        
        // Cho phép tất cả các origin
        config.addAllowedOriginPattern("*");
        
        // Cho phép tất cả các HTTP methods
        config.addAllowedMethod("*");
        
        // Cho phép tất cả các headers
        config.addAllowedHeader("*");
        
        // Cho phép gửi credentials (cookies, authorization headers, etc.)
        config.setAllowCredentials(true);
        
        // Expose các headers cho client
        config.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));
        
        // Cache preflight request trong 1 giờ
        config.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
}

