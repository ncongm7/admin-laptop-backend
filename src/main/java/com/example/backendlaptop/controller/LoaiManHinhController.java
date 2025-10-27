package com.example.backendlaptop.controller;

import com.example.backendlaptop.entity.LoaiManHinh;
import com.example.backendlaptop.repository.LoaiManHinhRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/loai-man-hinh")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LoaiManHinhController {
    
    private final LoaiManHinhRepository loaiManHinhRepository;
    
    @PostMapping
    public ResponseEntity<LoaiManHinh> createLoaiManHinh(@Valid @RequestBody LoaiManHinh loaiManHinh) {
        loaiManHinh.setId(UUID.randomUUID());
        LoaiManHinh savedLoaiManHinh = loaiManHinhRepository.save(loaiManHinh);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedLoaiManHinh);
    }
    
    @GetMapping
    public ResponseEntity<List<LoaiManHinh>> getAllLoaiManHinh() {
        List<LoaiManHinh> loaiManHinhs = loaiManHinhRepository.findAll();
        return ResponseEntity.ok(loaiManHinhs);
    }
    
    @GetMapping("/trang-thai/{trangThai}")
    public ResponseEntity<List<LoaiManHinh>> getLoaiManHinhByTrangThai(@PathVariable Integer trangThai) {
        List<LoaiManHinh> loaiManHinhs = loaiManHinhRepository.findByTrangThai(trangThai);
        return ResponseEntity.ok(loaiManHinhs);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<LoaiManHinh> getLoaiManHinhById(@PathVariable UUID id) {
        LoaiManHinh loaiManHinh = loaiManHinhRepository.findById(id).orElse(null);
        if (loaiManHinh == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(loaiManHinh);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<LoaiManHinh> updateLoaiManHinh(@PathVariable UUID id, @Valid @RequestBody LoaiManHinh loaiManHinh) {
        if (!loaiManHinhRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        loaiManHinh.setId(id);
        LoaiManHinh savedLoaiManHinh = loaiManHinhRepository.save(loaiManHinh);
        return ResponseEntity.ok(savedLoaiManHinh);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLoaiManHinh(@PathVariable UUID id) {
        if (!loaiManHinhRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        loaiManHinhRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
