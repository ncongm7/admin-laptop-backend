package com.example.backendlaptop.dto.sanpham;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class SanPhamRequest {
    
    @NotBlank(message = "Mã sản phẩm không được để trống")
    @Size(max = 50, message = "Mã sản phẩm không được vượt quá 50 ký tự")
    private String maSanPham;
    
    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 255, message = "Tên sản phẩm không được vượt quá 255 ký tự")
    private String tenSanPham;
    
    private String moTa;
    
    @NotNull(message = "Trạng thái không được để trống")
    private Integer trangThai;
    
    @Size(max = 255, message = "Người tạo không được vượt quá 255 ký tự")
    private String nguoiTao;
    
    @Size(max = 255, message = "Người sửa không được vượt quá 255 ký tự")
    private String nguoiSua;
}
