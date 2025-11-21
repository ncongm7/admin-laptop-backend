package com.example.backendlaptop.dto.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ReviewRequest {
    
    @NotNull(message = "Số sao không được để trống")
    @Min(value = 1, message = "Số sao phải từ 1 đến 5")
    @Max(value = 5, message = "Số sao phải từ 1 đến 5")
    private Integer soSao;
    
    @NotBlank(message = "Nội dung đánh giá không được để trống")
    private String noiDung;
    
    private List<String> imageUrls; // URLs của hình ảnh đánh giá
    
    @NotNull(message = "ID chi tiết sản phẩm không được để trống")
    private UUID chiTietSanPhamId;
}

