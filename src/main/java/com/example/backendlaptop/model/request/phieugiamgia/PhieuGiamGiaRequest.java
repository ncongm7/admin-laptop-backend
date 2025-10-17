package com.example.backendlaptop.model.request.phieugiamgia;

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
public class PhieuGiamGiaRequest {

    @NotBlank(message = "Mã phiếu giảm giá không được để trống")
    private String ma;

    @NotBlank(message = "Tên phiếu giảm giá không được để trống")
    private String tenPhieuGiamGia;

    @NotNull(message = "Loại phiếu giảm giá không được để trống")
    private Integer loaiPhieuGiamGia;

    @DecimalMin(value = "0.0", message = "Giá trị giảm phải >= 0")
    private BigDecimal giaTriGiamGia;

    @DecimalMin(value = "0.0", message = "Số tiền giảm tối đa phải >= 0")
    private BigDecimal soTienGiamToiDa;

    @DecimalMin(value = "0.0", message = "Hóa đơn tối thiểu phải >= 0")
    private BigDecimal hoaDonToiThieu;

    @Min(value = 1, message = "Số lượng sử dụng phải >= 1")
    private Integer soLuongDung;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private Instant ngayBatDau;

    @NotNull(message = "Ngày kết thúc không được để trống")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private Instant ngayKetThuc;

    private Boolean riengTu = false;

    private String moTa;
}

