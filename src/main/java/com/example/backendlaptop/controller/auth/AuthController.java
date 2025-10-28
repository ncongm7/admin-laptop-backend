package com.example.backendlaptop.controller.auth;

import com.example.backendlaptop.dto.auth.LoginRequest;
import com.example.backendlaptop.dto.auth.LoginResponse;
import com.example.backendlaptop.model.response.ResponseObject;
import com.example.backendlaptop.service.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@Tag(name = "Authentication", description = "API xác thực người dùng - Đăng nhập, đăng xuất")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * API Đăng nhập
     * Endpoint: POST /api/auth/login
     */
    @Operation(summary = "Đăng nhập", description = "Đăng nhập bằng tên đăng nhập và mật khẩu")
    @PostMapping("/login")
    public ResponseEntity<ResponseObject<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(new ResponseObject<>(response, "Đăng nhập thành công"));
    }

    /**
     * API Lấy thông tin user hiện tại
     * Endpoint: GET /api/auth/me
     */
    @Operation(summary = "Lấy thông tin user", description = "Lấy thông tin user hiện tại từ token")
    @GetMapping("/me")
    public ResponseEntity<ResponseObject<LoginResponse.UserInfo>> getCurrentUser(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ResponseObject<>(false, null, "Token không hợp lệ"));
        }
        
        String token = authHeader.substring(7);
        LoginResponse.UserInfo userInfo = authService.getCurrentUser(token);
        return ResponseEntity.ok(new ResponseObject<>(userInfo, "Lấy thông tin user thành công"));
    }

    /**
     * API Đăng xuất
     * Endpoint: POST /api/auth/logout
     */
    @Operation(summary = "Đăng xuất", description = "Đăng xuất và hủy token")
    @PostMapping("/logout")
    public ResponseEntity<ResponseObject<String>> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            authService.logout(token);
        }
        
        return ResponseEntity.ok(new ResponseObject<>("Đăng xuất thành công", "Đăng xuất thành công"));
    }
}

