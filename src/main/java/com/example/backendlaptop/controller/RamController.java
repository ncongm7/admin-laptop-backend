package com.example.backendlaptop.controller;

import com.example.backendlaptop.entity.Ram;
import com.example.backendlaptop.repository.RamRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/ram")
@RequiredArgsConstructor
public class RamController {
    
    private final RamRepository ramRepository;
    
    @PostMapping
    public ResponseEntity<Ram> createRam(@Valid @RequestBody Ram ram) {
        ram.setId(UUID.randomUUID());
        Ram savedRam = ramRepository.save(ram);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedRam);
    }
    
    @GetMapping
    public ResponseEntity<List<Ram>> getAllRam() {
        List<Ram> rams = ramRepository.findAll();
        return ResponseEntity.ok(rams);
    }
    
    @GetMapping("/trang-thai/{trangThai}")
    public ResponseEntity<List<Ram>> getRamByTrangThai(@PathVariable Integer trangThai) {
        List<Ram> rams = ramRepository.findByTrangThai(trangThai);
        return ResponseEntity.ok(rams);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Ram> getRamById(@PathVariable UUID id) {
        Ram ram = ramRepository.findById(id).orElse(null);
        if (ram == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ram);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Ram> updateRam(@PathVariable UUID id, @Valid @RequestBody Ram ram) {
        if (!ramRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        ram.setId(id);
        Ram savedRam = ramRepository.save(ram);
        return ResponseEntity.ok(savedRam);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRam(@PathVariable UUID id) {
        if (!ramRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        ramRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
