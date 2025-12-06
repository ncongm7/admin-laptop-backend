package com.example.backendlaptop.model.request.diem;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TichDiemRequest {
    private UUID userId;
    private Integer soDiemCong; // Số điểm thêm (dương) hoặc bớt (âm)
    private String lyDo;
    private String ghiChu;
}

