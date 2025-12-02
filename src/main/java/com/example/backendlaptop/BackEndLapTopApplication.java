package com.example.backendlaptop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.http.client.HttpClientAutoConfiguration;

@SpringBootApplication(exclude = {HttpClientAutoConfiguration.class})
public class BackEndLapTopApplication {

    public static void main(String[] args) {
        // Ép Java dùng IPv4 để tránh lỗi kết nối SMTP qua IPv6
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("java.net.preferIPv6Addresses", "false");
        
        SpringApplication.run(BackEndLapTopApplication.class, args);
    }

}
