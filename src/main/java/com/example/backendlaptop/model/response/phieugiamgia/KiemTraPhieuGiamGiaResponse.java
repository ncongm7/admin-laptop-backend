// FILE: src/main/java/com/example/backendlaptop/model/response/phieugiamgia/KiemTraPhieuGiamGiaResponse.java
package com.example.backendlaptop.model.response.phieugiamgia;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KiemTraPhieuGiamGiaResponse {
    private boolean hopLe;
    private String thongBao;
    private UUID idPhieuGiamGia;
    private String tenPhieuGiamGia;
    private BigDecimal tienDuocGiam;
    private BigDecimal tongTienSauGiam;
}

