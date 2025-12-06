package com.example.backendlaptop.dto.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    @Size(min = 10, message = "Nội dung đánh giá phải có ít nhất 10 ký tự")
    private String noiDung;
    
    @Size(max = 255, message = "Tiêu đề không được quá 255 ký tự")
    private String reviewTitle; // Tiêu đề đánh giá
    
    private List<String> pros; // Điểm mạnh ["Hiệu năng tốt", "Pin trâu"]
    
    private List<String> cons; // Điểm yếu ["Hơi nặng", "Giá cao"]
    
    private List<String> imageUrls; // URLs của hình ảnh đánh giá (deprecated - dùng mediaFiles)
    
    @NotNull(message = "ID chi tiết sản phẩm không được để trống")
    private UUID chiTietSanPhamId;
}


