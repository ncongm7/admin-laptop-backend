package com.example.backendlaptop.controller.phanQuyenCon;

import com.example.backendlaptop.service.PhanQuyenSer.XaPhuongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dia-chi-tinh-xa")
@CrossOrigin("*")
public class XaPhuongController {
    @Autowired
    public XaPhuongService xaPhuongService;

    @GetMapping("/hien-thi-tinh-all")
    public ResponseEntity<Object> findAllTinh() {
        return ResponseEntity.ok(xaPhuongService.getAllTinhTP());

    }

    @GetMapping("/xa-phuong/{code}")
    public ResponseEntity<Object> getXaPhuong(@PathVariable("code") Integer code) {
        return ResponseEntity.ok(xaPhuongService.findByTinh(code));
    }

    @GetMapping("/ten-tinh/{ten}")
    public ResponseEntity<Object> getTenTinh(@PathVariable("ten") String ten) {
        return ResponseEntity.ok(xaPhuongService.getTinhTPByTenTinh(ten));
    }
}
