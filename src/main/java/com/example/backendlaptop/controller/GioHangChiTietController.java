package com.example.backendlaptop.controller;

import com.example.backendlaptop.dto.giohang.GioHangChiTietRequest;
import com.example.backendlaptop.model.response.ResponseObject;
import com.example.backendlaptop.service.GioHangChiTietService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/gio-hang-chi-tiet")
@CrossOrigin(origins = "*")
public class GioHangChiTietController {

    @Autowired
    private GioHangChiTietService gioHangChiTietService;

    @GetMapping("")
    public ResponseObject<?> getAll() {
        return new ResponseObject<>(gioHangChiTietService.getAll());
    }

    @GetMapping("/page")
    public ResponseObject<?> getAll(@RequestParam(value = "pageNo", defaultValue = "0") Integer pageNo) {
        return new ResponseObject<>(gioHangChiTietService.getAll(pageNo));
    }

    @GetMapping("/{id}")
    public ResponseObject<?> findById(@PathVariable UUID id) {
        return new ResponseObject<>(gioHangChiTietService.findById(id));
    }

    @PostMapping("")
    public ResponseObject<?> create(@RequestBody @Valid GioHangChiTietRequest gioHangChiTietRequest) {
        return new ResponseObject<>(gioHangChiTietService.create(gioHangChiTietRequest));
    }

    @PutMapping("/{id}")
    public ResponseObject<?> update(@PathVariable UUID id, @RequestBody @Valid GioHangChiTietRequest gioHangChiTietRequest) {
        return new ResponseObject<>(gioHangChiTietService.update(id, gioHangChiTietRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseObject<?> delete(@PathVariable UUID id) {
        gioHangChiTietService.delete(id);
        return new ResponseObject<>("Success");
    }
}
