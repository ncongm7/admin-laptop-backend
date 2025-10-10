package com.example.backendlaptop.controller.phanQuyenCon;
import com.example.backendlaptop.dto.phanQuyenDto.nhanVien.NhanVienRequest;

import com.example.backendlaptop.service.PhanQuyenSer.NhanVienService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/nhan-vien")
@CrossOrigin("*")
public class NhanVienController {
    @Autowired
    public NhanVienService nhanVienService;

    @GetMapping("/hien-thi-nv")
    public ResponseEntity<Object> hienThi() {
        return ResponseEntity.ok(nhanVienService.findAllNV());

    }

    @DeleteMapping("/xoa-nv/{id}")
    public ResponseEntity<Object> delete(@PathVariable("id") UUID id) {
        nhanVienService.deleteNV(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/sua-nhan-vien/{id}")
    public ResponseEntity<Object> update(@PathVariable("id") UUID id, @RequestBody NhanVienRequest nhanVienRequest) {
        nhanVienService.suaNV(id, nhanVienRequest );
        return ResponseEntity.ok().build();

    }

    @PostMapping("/add-nhan-vien")
    public ResponseEntity<Object> add(@RequestBody NhanVienRequest nhanVienRequest) {
        nhanVienService.addNV(nhanVienRequest);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/phan-trang-nv/{pageOne}/{pageSize}")
    public ResponseEntity<Object> phanTrangNhanVien(@PathVariable("pageOne") Integer pageOne, @PathVariable("pageSize") Integer pageSize) {
        return ResponseEntity.ok(nhanVienService.phanTrangNV(pageOne, pageSize).getContent());


    }

    @GetMapping("/getOne/{id}")
    public ResponseEntity<Object> getOne(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(nhanVienService.getOne(id));
    }

}
