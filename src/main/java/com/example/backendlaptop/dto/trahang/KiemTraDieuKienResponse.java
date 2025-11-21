package com.example.backendlaptop.dto.trahang;

import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
public class KiemTraDieuKienResponse {
    private UUID idHoaDon;
    private String maHoaDon;
    private Instant ngayMua;
    private Integer soNgaySauMua;
    private Boolean coTheTraHang; // ≤ 7 ngày và hỏng
    private Boolean coTheBaoHanh; // > 7 ngày hoặc hỏng
    private String goiY; // "Trả hàng" hoặc "Bảo hành"
    private List<SanPhamInfo> danhSachSanPham;
    private List<SerialInfo> danhSachSerial;

    @Data
    public static class SanPhamInfo {
        private UUID idHoaDonChiTiet;
        private String tenSanPham;
        private String maCtsp;
        private Integer soLuong;
        private Boolean coSerial; // Có serial/IMEI không
    }

    @Data
    public static class SerialInfo {
        private UUID idSerialDaBan;
        private UUID idHoaDonChiTiet;
        private String serialNo;
        private String imei;
    }
}

