package com.example.backendlaptop.controller;

import com.example.backendlaptop.entity.Gpu;
import com.example.backendlaptop.repository.GpuRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/gpu")
@RequiredArgsConstructor
public class GpuController {
    
    private final GpuRepository gpuRepository;
    
    @PostMapping
    public ResponseEntity<Gpu> createGpu(@Valid @RequestBody Gpu gpu) {
        gpu.setId(UUID.randomUUID());
        Gpu savedGpu = gpuRepository.save(gpu);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedGpu);
    }
    
    @GetMapping
    public ResponseEntity<List<Gpu>> getAllGpu() {
        List<Gpu> gpus = gpuRepository.findAll();
        return ResponseEntity.ok(gpus);
    }
    
    @GetMapping("/trang-thai/{trangThai}")
    public ResponseEntity<List<Gpu>> getGpuByTrangThai(@PathVariable Integer trangThai) {
        List<Gpu> gpus = gpuRepository.findByTrangThai(trangThai);
        return ResponseEntity.ok(gpus);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Gpu> getGpuById(@PathVariable UUID id) {
        Gpu gpu = gpuRepository.findById(id).orElse(null);
        if (gpu == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(gpu);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Gpu> updateGpu(@PathVariable UUID id, @Valid @RequestBody Gpu gpu) {
        if (!gpuRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        gpu.setId(id);
        Gpu savedGpu = gpuRepository.save(gpu);
        return ResponseEntity.ok(savedGpu);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGpu(@PathVariable UUID id) {
        if (!gpuRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        gpuRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
