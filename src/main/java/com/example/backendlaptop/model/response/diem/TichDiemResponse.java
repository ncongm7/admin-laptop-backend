package com.example.backendlaptop.model.response.diem;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TichDiemResponse {
    private UUID id;
    private UUID userId;
    private String tenKhachHang;
    private String soDienThoai;
    private String email;
    private Integer diemDaDung;
    private Integer diemDaCong;
    private Integer tongDiem;
    private Integer trangThai;
}

