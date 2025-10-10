package com.example.backendlaptop.controller;

import com.example.backendlaptop.entity.OCung;
import com.example.backendlaptop.repository.OCungRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/o-cung")
@RequiredArgsConstructor
public class OCungController {
    
    private final OCungRepository oCungRepository;
    
    @PostMapping
    public ResponseEntity<OCung> createOCung(@Valid @RequestBody OCung oCung) {
        oCung.setId(UUID.randomUUID());
        OCung savedOCung = oCungRepository.save(oCung);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedOCung);
    }
    
    @GetMapping
    public ResponseEntity<List<OCung>> getAllOCung() {
        List<OCung> oCungs = oCungRepository.findAll();
        return ResponseEntity.ok(oCungs);
    }
    
    @GetMapping("/trang-thai/{trangThai}")
    public ResponseEntity<List<OCung>> getOCungByTrangThai(@PathVariable Integer trangThai) {
        List<OCung> oCungs = oCungRepository.findByTrangThai(trangThai);
        return ResponseEntity.ok(oCungs);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<OCung> getOCungById(@PathVariable UUID id) {
        OCung oCung = oCungRepository.findById(id).orElse(null);
        if (oCung == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(oCung);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<OCung> updateOCung(@PathVariable UUID id, @Valid @RequestBody OCung oCung) {
        if (!oCungRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        oCung.setId(id);
        OCung savedOCung = oCungRepository.save(oCung);
        return ResponseEntity.ok(savedOCung);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOCung(@PathVariable UUID id) {
        if (!oCungRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        oCungRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
