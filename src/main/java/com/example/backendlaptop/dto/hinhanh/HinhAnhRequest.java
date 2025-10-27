package com.example.backendlaptop.dto.hinhanh;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class HinhAnhRequest {
    
    @NotNull(message = "ID chi tiết sản phẩm không được để trống")
    private UUID idSpct;
    
    @NotBlank(message = "URL hình ảnh không được để trống")
    private String url;
    
    private Boolean anhChinhDaiDien = false;
}
