package com.example.backendlaptop.model.request.dotgiamgia;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;

@Getter
@Setter
public class DotGiamGiaRequest {

    @NotBlank(message = "Tên khuyến mãi không được để trống")
    private String tenKm;

    @NotNull(message = "Giá trị không được để trống")
    @Min(value = 0, message = "Giá trị phải >= 0")
    private Integer giaTri;

    private String moTa;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private Instant ngayBatDau;

    @NotNull(message = "Ngày kết thúc không được để trống")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private Instant ngayKetThuc;
}
