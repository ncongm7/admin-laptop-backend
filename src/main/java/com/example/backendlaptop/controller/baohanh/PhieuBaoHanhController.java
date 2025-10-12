package com.example.backendlaptop.controller.baohanh;

import com.example.backendlaptop.model.response.ResponseObject;
import com.example.backendlaptop.service.baohanh.PhieuBaoHanhService;
import com.example.backendlaptop.service.phieugiamgia.PhieuGiamGiaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/phieu-bao-hanh-quan-ly")
@CrossOrigin(origins = "*")
public class PhieuBaoHanhController {
    @Autowired
    PhieuBaoHanhService service;

    @GetMapping("/danh-sach")
    public ResponseObject<?> danhSach(){
        return new ResponseObject<>(service.getAll());
    }

    @DeleteMapping("/delete/{id1}")
    public ResponseObject<?> delete(@PathVariable("id1") UUID id1){
        service.delete(id1);
        return new ResponseObject<>(null, "Xoa thanh cong");
    }
}



