package com.example.backendlaptop.model.response.diem;

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
public class QuyDoiDiemResponse {
    private UUID id;
    private BigDecimal tienTichDiem;
    private BigDecimal tienTieuDiem;
    private Integer trangThai;
}

