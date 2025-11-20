package com.example.backendlaptop.model.request.dotgiamgia;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
public class DotGiamGiaRequest {

    @NotBlank(message = "Tên khuyến mãi không được để trống")
    private String tenKm;

    @NotNull(message = "Loại đợt giảm giá không được để trống")
    private Integer loaiDotGiamGia; // 1: Giảm theo %, 2: Giảm theo số tiền (VND)

    @NotNull(message = "Giá trị không được để trống")
    @DecimalMin(value = "0.0", message = "Giá trị phải >= 0")
    private BigDecimal giaTri; // Giá trị giảm: % (0-100) hoặc số tiền VND

    @DecimalMin(value = "0.0", message = "Số tiền giảm tối đa phải >= 0")
    private BigDecimal soTienGiamToiDa; // Giới hạn số tiền giảm tối đa (chỉ dùng khi loai = 1 - %)

    private String moTa;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private Instant ngayBatDau;

    @NotNull(message = "Ngày kết thúc không được để trống")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private Instant ngayKetThuc;

    @NotNull(message = "Trạng thái (bật/tắt) không được để trống")
    private Integer trangThai; // 0 = Tắt, 1 = Bật
}
