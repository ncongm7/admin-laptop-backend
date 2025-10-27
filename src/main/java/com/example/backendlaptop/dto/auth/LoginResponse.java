package com.example.backendlaptop.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private String token;
    private UserInfo user;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserInfo {
        private UUID userId;
        private String tenDangNhap;
        private String hoTen;
        private String email;
        private String vaiTro;
        private Integer trangThai;
    }
}

