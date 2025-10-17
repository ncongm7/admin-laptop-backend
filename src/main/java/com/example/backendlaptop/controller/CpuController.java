package com.example.backendlaptop.controller;

import com.example.backendlaptop.entity.Cpu;
import com.example.backendlaptop.repository.CpuRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/cpu")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CpuController {
    
    private final CpuRepository cpuRepository;
    
    @PostMapping
    public ResponseEntity<Cpu> createCpu(@Valid @RequestBody Cpu cpu) {
        cpu.setId(UUID.randomUUID());
        Cpu savedCpu = cpuRepository.save(cpu);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCpu);
    }
    
    @GetMapping
    public ResponseEntity<List<Cpu>> getAllCpu() {
        List<Cpu> cpus = cpuRepository.findAll();
        return ResponseEntity.ok(cpus);
    }
    
    @GetMapping("/trang-thai/{trangThai}")
    public ResponseEntity<List<Cpu>> getCpuByTrangThai(@PathVariable Integer trangThai) {
        List<Cpu> cpus = cpuRepository.findByTrangThai(trangThai);
        return ResponseEntity.ok(cpus);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Cpu> getCpuById(@PathVariable UUID id) {
        Cpu cpu = cpuRepository.findById(id).orElse(null);
        if (cpu == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(cpu);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Cpu> updateCpu(@PathVariable UUID id, @Valid @RequestBody Cpu cpu) {
        if (!cpuRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        cpu.setId(id);
        Cpu savedCpu = cpuRepository.save(cpu);
        return ResponseEntity.ok(savedCpu);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCpu(@PathVariable UUID id) {
        if (!cpuRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        cpuRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
