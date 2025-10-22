package com.example.backendlaptop.controller;

import com.example.backendlaptop.dto.hinhanh.HinhAnhRequest;
import com.example.backendlaptop.dto.hinhanh.HinhAnhResponse;
import com.example.backendlaptop.service.HinhAnhService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/hinh-anh")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class HinhAnhController {
    
    private final HinhAnhService hinhAnhService;
    
    @PostMapping
    public ResponseEntity<HinhAnhResponse> createHinhAnh(@Valid @RequestBody HinhAnhRequest request) {
        HinhAnhResponse response = hinhAnhService.createHinhAnh(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PostMapping("/batch")
    public ResponseEntity<List<HinhAnhResponse>> createHinhAnhBatch(@Valid @RequestBody List<HinhAnhRequest> requests) {
        List<HinhAnhResponse> responses = hinhAnhService.createHinhAnhBatch(requests);
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }
    
    @GetMapping("/ctsp/{ctspId}")
    public ResponseEntity<List<HinhAnhResponse>> getHinhAnhByCtspId(@PathVariable UUID ctspId) {
        List<HinhAnhResponse> responses = hinhAnhService.getHinhAnhByCtspId(ctspId);
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/ctsp/{ctspId}/main")
    public ResponseEntity<HinhAnhResponse> getMainImageByCtspId(@PathVariable UUID ctspId) {
        HinhAnhResponse response = hinhAnhService.getMainImageByCtspId(ctspId);
        return response != null ? ResponseEntity.ok(response) : ResponseEntity.notFound().build();
    }
    
    @GetMapping("/ctsp/{ctspId}/gallery")
    public ResponseEntity<List<HinhAnhResponse>> getGalleryImagesByCtspId(@PathVariable UUID ctspId) {
        List<HinhAnhResponse> responses = hinhAnhService.getGalleryImagesByCtspId(ctspId);
        return ResponseEntity.ok(responses);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<HinhAnhResponse> updateHinhAnh(@PathVariable UUID id, @Valid @RequestBody HinhAnhRequest request) {
        HinhAnhResponse response = hinhAnhService.updateHinhAnh(id, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHinhAnh(@PathVariable UUID id) {
        hinhAnhService.deleteHinhAnh(id);
        return ResponseEntity.noContent().build();
    }
    
    @DeleteMapping("/ctsp/{ctspId}")
    public ResponseEntity<Void> deleteAllByCtspId(@PathVariable UUID ctspId) {
        hinhAnhService.deleteAllByCtspId(ctspId);
        return ResponseEntity.noContent().build();
    }
}
