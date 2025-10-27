package com.example.backendlaptop.controller;

import com.example.backendlaptop.entity.MauSac;
import com.example.backendlaptop.repository.MauSacRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/mau-sac")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MauSacController {
    
    private final MauSacRepository mauSacRepository;
    
    @PostMapping
    public ResponseEntity<MauSac> createMauSac(@Valid @RequestBody MauSac mauSac) {
        mauSac.setId(UUID.randomUUID());
        MauSac savedMauSac = mauSacRepository.save(mauSac);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedMauSac);
    }
    
    @GetMapping
    public ResponseEntity<List<MauSac>> getAllMauSac() {
        List<MauSac> mauSacs = mauSacRepository.findAll();
        return ResponseEntity.ok(mauSacs);
    }
    
    @GetMapping("/trang-thai/{trangThai}")
    public ResponseEntity<List<MauSac>> getMauSacByTrangThai(@PathVariable Integer trangThai) {
        List<MauSac> mauSacs = mauSacRepository.findByTrangThai(trangThai);
        return ResponseEntity.ok(mauSacs);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<MauSac> getMauSacById(@PathVariable UUID id) {
        MauSac mauSac = mauSacRepository.findById(id).orElse(null);
        if (mauSac == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(mauSac);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<MauSac> updateMauSac(@PathVariable UUID id, @Valid @RequestBody MauSac mauSac) {
        if (!mauSacRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        mauSac.setId(id);
        MauSac savedMauSac = mauSacRepository.save(mauSac);
        return ResponseEntity.ok(savedMauSac);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMauSac(@PathVariable UUID id) {
        if (!mauSacRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        mauSacRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
