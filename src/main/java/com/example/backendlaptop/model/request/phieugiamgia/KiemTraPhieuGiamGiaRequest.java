// FILE: src/main/java/com/example/backendlaptop/model/request/phieugiamgia/KiemTraPhieuGiamGiaRequest.java
package com.example.backendlaptop.model.request.phieugiamgia;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class KiemTraPhieuGiamGiaRequest {
    @NotBlank(message = "Mã phiếu giảm giá không được để trống")
    private String ma;
    
    @NotNull(message = "ID khách hàng không được để trống")
    private UUID khachHangId;
    
    @NotNull(message = "Tổng tiền hóa đơn không được để trống")
    private BigDecimal tongTienHoaDon;
}

