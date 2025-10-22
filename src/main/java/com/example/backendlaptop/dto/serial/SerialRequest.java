package com.example.backendlaptop.dto.serial;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class SerialRequest {
    
    @NotNull(message = "ID chi tiết sản phẩm không được để trống")
    private UUID ctspId;
    
    @NotBlank(message = "Serial number không được để trống")
    @Size(max = 100, message = "Serial number không được vượt quá 100 ký tự")
    private String serialNo;
    
    private Integer trangThai = 1; // 1: Chưa bán, 2: Đã bán, 0: Hỏng
}
