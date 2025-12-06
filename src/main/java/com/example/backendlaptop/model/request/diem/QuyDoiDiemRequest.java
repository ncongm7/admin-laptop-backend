package com.example.backendlaptop.model.request.diem;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuyDoiDiemRequest {
    private BigDecimal tienTichDiem;
    private BigDecimal tienTieuDiem;
    private Integer trangThai;
}

