package com.example.backendlaptop.controller;

import com.example.backendlaptop.dto.serial.SerialRequest;
import com.example.backendlaptop.dto.serial.SerialResponse;
import com.example.backendlaptop.service.SerialService;
import com.example.backendlaptop.service.auth.AuthService;
import com.example.backendlaptop.dto.auth.LoginResponse;
import com.example.backendlaptop.repository.TaiKhoanRepository;
import com.example.backendlaptop.entity.TaiKhoan;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/serial")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SerialController {
    private final SerialService serialService;
    private final AuthService authService;
    private final TaiKhoanRepository taiKhoanRepository;

    @GetMapping("/my-warranties")
    public ResponseEntity<List<SerialResponse>> getMyWarranties(@RequestHeader("Authorization") String authHeader) {
        try {
            LoginResponse.UserInfo user = getCurrentUserFromToken(authHeader);

            // User ID from JWT is actually the TaiKhoan ID
            // We need to get customer from KhachHang table using this TaiKhoan ID
            UUID userId = user.getUserId();

            List<SerialResponse> serials = serialService.getSerialsByUserId(userId);
            return ResponseEntity.ok(serials);
        } catch (Exception e) {
            System.err.println("Error in getMyWarranties: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    private LoginResponse.UserInfo getCurrentUserFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Token không hợp lệ");
        }
        String token = authHeader.substring(7);
        return authService.getCurrentUser(token);
    }

    @PostMapping
    public ResponseEntity<SerialResponse> createSerial(@Valid @RequestBody SerialRequest request) {
        SerialResponse response = serialService.createSerial(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/batch")
    public ResponseEntity<List<SerialResponse>> createSerialsBatch(@Valid @RequestBody List<SerialRequest> requests) {
        List<SerialResponse> responses = serialService.createSerialsBatch(requests);
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    @PostMapping("/import-excel/{ctspId}")
    public ResponseEntity<List<SerialResponse>> importSerialsFromExcel(
            @PathVariable UUID ctspId,
            @RequestParam("file") MultipartFile file) {
        try {
            List<SerialResponse> responses = serialService.importSerialsFromExcel(ctspId, file);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<SerialResponse>> getAllSerial() {
        List<SerialResponse> responses = serialService.getAllSerial();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/ctsp/{ctspId}")
    public ResponseEntity<List<SerialResponse>> getSerialsByCtspId(@PathVariable UUID ctspId) {
        List<SerialResponse> responses = serialService.getSerialsByCtspId(ctspId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SerialResponse> getSerialById(@PathVariable UUID id) {
        SerialResponse response = serialService.getSerialById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SerialResponse> updateSerial(@PathVariable UUID id,
            @Valid @RequestBody SerialRequest request) {
        SerialResponse response = serialService.updateSerial(id, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/trang-thai")
    public ResponseEntity<Void> updateSerialStatus(@PathVariable UUID id, @RequestParam Integer trangThai) {
        serialService.updateSerialStatus(id, trangThai);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSerial(@PathVariable UUID id) {
        serialService.deleteSerial(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/sold")
    public ResponseEntity<List<com.example.backendlaptop.dto.serial.SoldSerialResponse>> searchSoldSerials(
            @RequestParam String keyword) {
        return ResponseEntity.ok(serialService.searchSoldSerials(keyword));
    }
}
