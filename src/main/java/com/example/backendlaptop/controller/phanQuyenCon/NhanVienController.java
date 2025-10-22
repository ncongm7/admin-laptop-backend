package com.example.backendlaptop.controller.phanQuyenCon;
import com.example.backendlaptop.dto.phanQuyenDto.nhanVien.NhanVienRequest;

import com.example.backendlaptop.service.PhanQuyenSer.NhanVienService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        try {
            nhanVienService.suaNV(id, nhanVienRequest);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }

    }

    @PostMapping("/add-nhan-vien")
    public ResponseEntity<Object> add(@RequestBody NhanVienRequest nhanVienRequest) {
        try {
            nhanVienService.addNV(nhanVienRequest);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }

    @GetMapping("/phan-trang-nv/{pageOne}/{pageSize}")
    public ResponseEntity<Object> phanTrangNhanVien(@PathVariable("pageOne") Integer pageOne, @PathVariable("pageSize") Integer pageSize) {
        return ResponseEntity.ok(nhanVienService.phanTrangNV(pageOne, pageSize).getContent());


    }

    @GetMapping("/getOne/{id}")
    public ResponseEntity<Object> getOne(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(nhanVienService.getOne(id));
    }

    @PostMapping("/upload-avatar")
    public ResponseEntity<Object> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("File không được để trống");
            }

            // Tạo thư mục uploads nếu chưa có
            String uploadDir = "uploads/avatars/";
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Tạo tên file unique
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID().toString() + extension;
            
            // Lưu file
            Path filePath = Paths.get(uploadDir + filename);
            Files.copy(file.getInputStream(), filePath);

            // Trả về URL ảnh
            String imageUrl = "/uploads/avatars/" + filename;
            return ResponseEntity.ok().body(imageUrl);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi upload file: " + e.getMessage());
        }
    }


}

