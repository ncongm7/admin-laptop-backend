package com.example.backendlaptop.controller;

import com.example.backendlaptop.entity.PhuongThucThanhToan;
import com.example.backendlaptop.repository.PhuongThucThanhToanRepository;
import com.example.backendlaptop.model.response.ResponseObject;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/phuong-thuc-thanh-toan")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PhuongThucThanhToanController {

    private final PhuongThucThanhToanRepository phuongThucThanhToanRepository;


    @GetMapping
    public ResponseEntity<ResponseObject<List<PhuongThucThanhToan>>> getAllPhuongThucThanhToan() {
        List<PhuongThucThanhToan> danhSach = phuongThucThanhToanRepository.findAll();
        return ResponseEntity.ok(new ResponseObject<>(danhSach, "Lấy danh sách phương thức thanh toán thành công"));
    }
}

