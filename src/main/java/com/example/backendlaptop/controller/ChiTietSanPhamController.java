package com.example.backendlaptop.controller;

import com.example.backendlaptop.dto.sanpham.ChiTietSanPhamRequest;
import com.example.backendlaptop.dto.sanpham.ChiTietSanPhamResponse;
import com.example.backendlaptop.dto.sanpham.TaoBienTheSanPhamRequest;
import com.example.backendlaptop.service.ChiTietSanPhamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chi-tiet-san-pham")
@RequiredArgsConstructor

public class ChiTietSanPhamController {
    
    private final ChiTietSanPhamService chiTietSanPhamService;
    
    @PostMapping
    public ResponseEntity<ChiTietSanPhamResponse> createChiTietSanPham(@Valid @RequestBody ChiTietSanPhamRequest request) {
        ChiTietSanPhamResponse response = chiTietSanPhamService.createChiTietSanPham(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PostMapping("/tao-bien-the")
    public ResponseEntity<List<ChiTietSanPhamResponse>> taoBienTheSanPham(@Valid @RequestBody TaoBienTheSanPhamRequest request) {
        List<ChiTietSanPhamResponse> responses = chiTietSanPhamService.taoBienTheSanPham(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ChiTietSanPhamResponse> updateChiTietSanPham(@PathVariable UUID id, @Valid @RequestBody ChiTietSanPhamRequest request) {
        ChiTietSanPhamResponse response = chiTietSanPhamService.updateChiTietSanPham(id, request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ChiTietSanPhamResponse> getChiTietSanPhamById(@PathVariable UUID id) {
        ChiTietSanPhamResponse response = chiTietSanPhamService.getChiTietSanPhamById(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/san-pham/{sanPhamId}")
    public ResponseEntity<List<ChiTietSanPhamResponse>> getChiTietSanPhamBySanPhamId(@PathVariable UUID sanPhamId) {
        List<ChiTietSanPhamResponse> responses = chiTietSanPhamService.getChiTietSanPhamBySanPhamId(sanPhamId);
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping
    public ResponseEntity<List<ChiTietSanPhamResponse>> getAllChiTietSanPham() {
        List<ChiTietSanPhamResponse> responses = chiTietSanPhamService.getAllChiTietSanPham();
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/page")
    public ResponseEntity<Page<ChiTietSanPhamResponse>> getAllChiTietSanPham(Pageable pageable) {
        Page<ChiTietSanPhamResponse> responses = chiTietSanPhamService.getAllChiTietSanPham(pageable);
        return ResponseEntity.ok(responses);
    }
    
    @PutMapping("/{id}/trang-thai")
    public ResponseEntity<Void> updateTrangThaiChiTietSanPham(@PathVariable UUID id, @RequestParam Integer trangThai) {
        chiTietSanPhamService.updateTrangThaiChiTietSanPham(id, trangThai);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChiTietSanPham(@PathVariable UUID id) {
        chiTietSanPhamService.deleteChiTietSanPham(id);
        return ResponseEntity.noContent().build();
    }
}
