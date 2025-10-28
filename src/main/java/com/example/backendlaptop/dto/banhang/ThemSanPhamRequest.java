package com.example.backendlaptop.dto.banhang;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class ThemSanPhamRequest {
    @NotNull(message = "ID chi tiết sản phẩm không được để trống")
    private UUID chiTietSanPhamId;
    
    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    private Integer soLuong;
}
