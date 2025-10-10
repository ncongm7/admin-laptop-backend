package com.example.backendlaptop.controller;

import com.example.backendlaptop.entity.Pin;
import com.example.backendlaptop.repository.PinRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pin")
@RequiredArgsConstructor
public class PinController {
    
    private final PinRepository pinRepository;
    
    @PostMapping
    public ResponseEntity<Pin> createPin(@Valid @RequestBody Pin pin) {
        pin.setId(UUID.randomUUID());
        Pin savedPin = pinRepository.save(pin);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedPin);
    }
    
    @GetMapping
    public ResponseEntity<List<Pin>> getAllPin() {
        List<Pin> pins = pinRepository.findAll();
        return ResponseEntity.ok(pins);
    }
    
    @GetMapping("/trang-thai/{trangThai}")
    public ResponseEntity<List<Pin>> getPinByTrangThai(@PathVariable Integer trangThai) {
        List<Pin> pins = pinRepository.findByTrangThai(trangThai);
        return ResponseEntity.ok(pins);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Pin> getPinById(@PathVariable UUID id) {
        Pin pin = pinRepository.findById(id).orElse(null);
        if (pin == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(pin);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Pin> updatePin(@PathVariable UUID id, @Valid @RequestBody Pin pin) {
        if (!pinRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        pin.setId(id);
        Pin savedPin = pinRepository.save(pin);
        return ResponseEntity.ok(savedPin);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePin(@PathVariable UUID id) {
        if (!pinRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        pinRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
