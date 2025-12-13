package com.example.backendlaptop.dto.warranty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class WarrantyCreateRequest {
    @NotNull(message = "Serial ID không được để trống")
    private UUID serialId;

    @NotBlank(message = "Mô tả không được để trống")
    private String moTa;

    private String hinhAnh; // JSON array or comma separated
}
