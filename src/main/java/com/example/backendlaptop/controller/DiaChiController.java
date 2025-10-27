package com.example.backendlaptop.controller;

import com.example.backendlaptop.dto.diaChi.DiaChiRequest;
import com.example.backendlaptop.service.DiaChiService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/dia-chi")
@CrossOrigin("*")
public class DiaChiController {
    @Autowired
    public DiaChiService diaChiService;

    @GetMapping("/hien-thi")
    public ResponseEntity<Object> hienThi() {
        return ResponseEntity.ok(diaChiService.getAllDiaChi());
    }

    @GetMapping("/find-by-ma-khach-hang/{maKhachHang}")
    public ResponseEntity<Object> findByMaKhachHang(@PathVariable("maKhachHang") String maKhachHang) {
        return ResponseEntity.ok(diaChiService.findByMaKhachHang(maKhachHang));
    }

    @GetMapping("/getOne/{id}")
    public ResponseEntity<Object> getOne(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(diaChiService.getOne(id));
    }

    @PostMapping("/add")
    public ResponseEntity<Object> add(@Valid @RequestBody DiaChiRequest diaChiRequest) {
        diaChiService.addDiaChi(diaChiRequest);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/sua/{id}")
    public ResponseEntity<Object> update(@PathVariable("id") UUID id, @Valid @RequestBody DiaChiRequest diaChiRequest) {
        diaChiService.updateDiaChi(id, diaChiRequest);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/xoa/{id}")
    public ResponseEntity<Object> delete(@PathVariable("id") UUID id) {
        diaChiService.deleteDiaChi(id);
        return ResponseEntity.ok().build();
    }
}
