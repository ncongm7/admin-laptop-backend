package com.example.backendlaptop.controller;

import com.example.backendlaptop.model.response.ResponseObject;
import com.example.backendlaptop.service.DotGiamGiaChiTietService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/dot-giam-gia-chi-tiet-quan-ly")
@CrossOrigin(origins = "*")
public class DotGiamGiaChiTietController {
    @Autowired
    DotGiamGiaChiTietService service;

    @GetMapping("/danh-sach/{iddotgiamgia}")
    public ResponseObject<?> danhSach(@PathVariable("iddotgiamgia") UUID iddotgiamgia){
        return new ResponseObject<>(service.findByDotGiamGiaId(iddotgiamgia));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseObject<?> delete(@PathVariable("id") UUID id){
        service.delete(id);
        return new ResponseObject<>(null, "Xoa thanh cong");
    }
}
