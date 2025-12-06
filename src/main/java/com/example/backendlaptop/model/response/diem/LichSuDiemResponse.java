package com.example.backendlaptop.model.response.diem;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LichSuDiemResponse {
    private UUID id;
    private UUID hoaDonId;
    private String maHoaDon;
    private Integer loaiDiem;
    private String tenLoaiDiem;
    private Integer soDiemDaDung;
    private Integer soDiemCong;
    private Instant thoiGian;
    private LocalDate hanSuDung;
    private Integer trangThai;
    private String tenTrangThai;
    private String ghiChu;
    private List<ChiTietLichSuDiemResponse> chiTietLichSuDiem;
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChiTietLichSuDiemResponse {
        private UUID id;
        private Integer soDiemDaTru;
        private Instant ngayTru;
    }
}

