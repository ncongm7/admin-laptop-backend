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
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
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

            // Validate và tạo tên file unique
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                return ResponseEntity.badRequest().body("Tên file không hợp lệ");
            }
            
            // Validate file extension
            String extension = "";
            int dotIndex = originalFilename.lastIndexOf(".");
            if (dotIndex > 0 && dotIndex < originalFilename.length() - 1) {
                extension = originalFilename.substring(dotIndex).toLowerCase();
            }
            
            // Whitelist allowed extensions
            List<String> allowedExtensions = Arrays.asList(".jpg", ".jpeg", ".png", ".gif");
            if (!allowedExtensions.contains(extension)) {
                return ResponseEntity.badRequest().body("Chỉ chấp nhận file ảnh: jpg, jpeg, png, gif");
            }
            
            // Generate safe filename using UUID
            String filename = UUID.randomUUID().toString() + extension;
            
            // Lưu file - use resolve to prevent path traversal
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path filePath = uploadPath.resolve(filename).normalize();
            
            // Security check: ensure the resolved path is still within uploadDir
            if (!filePath.startsWith(uploadPath)) {
                return ResponseEntity.badRequest().body("Đường dẫn file không hợp lệ");
            }
            
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Trả về URL ảnh
            String imageUrl = "/uploads/avatars/" + filename;
            return ResponseEntity.ok().body(imageUrl);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi upload file: " + e.getMessage());
        }
    }


}

