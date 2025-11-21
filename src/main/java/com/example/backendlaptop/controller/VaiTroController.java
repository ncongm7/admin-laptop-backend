package com.example.backendlaptop.controller;

import com.example.backendlaptop.entity.VaiTro;
import com.example.backendlaptop.repository.VaiTroRepository;
import com.example.backendlaptop.model.response.ResponseObject;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vai-tro")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class VaiTroController {

    private final VaiTroRepository vaiTroRepository;

    @GetMapping
    public ResponseEntity<ResponseObject<List<VaiTro>>> getAllVaiTro() {
        try {
            List<VaiTro> vaiTroList = vaiTroRepository.findAll();
            return ResponseEntity.ok(new ResponseObject<>(vaiTroList, "Lấy danh sách vai trò thành công"));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ResponseObject<>(false, null, "Lỗi khi lấy danh sách vai trò: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseObject<VaiTro>> getVaiTroById(@PathVariable java.util.UUID id) {
        try {
            VaiTro vaiTro = vaiTroRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Vai trò không tồn tại"));
            return ResponseEntity.ok(new ResponseObject<>(vaiTro, "Lấy thông tin vai trò thành công"));
        } catch (Exception e) {
            return ResponseEntity.status(404)
                    .body(new ResponseObject<>(false, null, "Lỗi: " + e.getMessage()));
        }
    }
}

