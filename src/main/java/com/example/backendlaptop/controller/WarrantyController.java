package com.example.backendlaptop.controller;

import com.example.backendlaptop.dto.auth.LoginResponse;
import com.example.backendlaptop.dto.warranty.WarrantyCreateRequest;
import com.example.backendlaptop.model.response.baohanh.PhieuBaoHanhResponse;
import com.example.backendlaptop.service.auth.AuthService;
import com.example.backendlaptop.service.baohanh.PhieuBaoHanhService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/warranties")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class WarrantyController {

    private final PhieuBaoHanhService service;
    private final AuthService authService;

    @GetMapping("/eligible-serials")
    public ResponseEntity<?> getEligibleSerials(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        LoginResponse.UserInfo user = authService.getCurrentUser(token);

        return ResponseEntity.ok(service.getEligibleSerials(user.getUserId()));
    }

    @PostMapping
    public ResponseEntity<PhieuBaoHanhResponse> create(
            @RequestBody WarrantyCreateRequest request,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        LoginResponse.UserInfo user = authService.getCurrentUser(token);

        return ResponseEntity.ok(service.createWarrantyRequest(request, user.getUserId()));
    }
}
