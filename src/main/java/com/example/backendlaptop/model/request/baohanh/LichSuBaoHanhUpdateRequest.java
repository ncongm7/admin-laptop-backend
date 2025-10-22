package com.example.backendlaptop.model.request.baohanh;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;
@Data
public class LichSuBaoHanhUpdateRequest {
    private Instant ngayTiepNhan;
    private Instant ngayHoanThanh;
    private String moTaLoi;
}
