package com.example.backendlaptop.controller.chat;

import com.example.backendlaptop.model.response.ResponseObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatFileController {

    @Value("${chat.upload.dir:uploads/chat}")
    private String uploadDir;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final String[] ALLOWED_IMAGE_TYPES = {"image/jpeg", "image/png", "image/gif", "image/webp"};
    private static final String[] ALLOWED_FILE_TYPES = {
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    };

    @PostMapping("/upload")
    public ResponseEntity<ResponseObject<FileUploadResponse>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "conversationId", required = false) String conversationId) {
        
        try {
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ResponseObject<FileUploadResponse>(false, null, "File không được để trống"));
            }

            if (file.getSize() > MAX_FILE_SIZE) {
                return ResponseEntity.badRequest()
                    .body(new ResponseObject<FileUploadResponse>(false, null, 
                        String.format("File quá lớn. Kích thước tối đa: %dMB", MAX_FILE_SIZE / 1024 / 1024)));
            }

            String contentType = file.getContentType();
            boolean isImage = contentType != null && isAllowedImageType(contentType);
            boolean isAllowedFile = contentType != null && (isImage || isAllowedFileType(contentType));

            if (!isAllowedFile) {
                return ResponseEntity.badRequest()
                    .body(new ResponseObject<FileUploadResponse>(false, null, 
                        "Loại file không được hỗ trợ. Chỉ chấp nhận: ảnh (JPG, PNG, GIF, WEBP) hoặc tài liệu (PDF, DOC, DOCX)"));
            }

            // Create upload directory if not exists
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                : "";
            String filename = UUID.randomUUID().toString() + extension;

            // Save file
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Generate URL
            String fileUrl = "/uploads/chat/" + filename;

            FileUploadResponse response = FileUploadResponse.builder()
                .fileUrl(fileUrl)
                .fileName(originalFilename)
                .fileSize(file.getSize())
                .contentType(contentType)
                .messageType(isImage ? "image" : "file")
                .build();

            log.info("File uploaded successfully: {} ({} bytes)", filename, file.getSize());

            return ResponseEntity.ok(new ResponseObject<FileUploadResponse>(response, "Upload file thành công"));

        } catch (IOException e) {
            log.error("Error uploading file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseObject<FileUploadResponse>(false, null, "Lỗi khi upload file: " + e.getMessage()));
        }
    }

    private boolean isAllowedImageType(String contentType) {
        for (String type : ALLOWED_IMAGE_TYPES) {
            if (type.equals(contentType)) {
                return true;
            }
        }
        return false;
    }

    private boolean isAllowedFileType(String contentType) {
        for (String type : ALLOWED_FILE_TYPES) {
            if (type.equals(contentType)) {
                return true;
            }
        }
        return false;
    }

    @lombok.Data
    @lombok.Builder
    public static class FileUploadResponse {
        private String fileUrl;
        private String fileName;
        private Long fileSize;
        private String contentType;
        private String messageType;
    }
}

