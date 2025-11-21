package com.example.backendlaptop.controller;

import com.example.backendlaptop.dto.CategoryResponse;
import com.example.backendlaptop.service.DanhMucService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin("*")
@RequiredArgsConstructor
public class DanhMucController {

    private final DanhMucService danhMucService;

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories(
            @RequestParam(value = "featured", required = false) Boolean featured) {
        if (featured != null && featured) {
            return ResponseEntity.ok(danhMucService.getFeaturedCategories());
        }
        return ResponseEntity.ok(danhMucService.getAllCategories());
    }
}
