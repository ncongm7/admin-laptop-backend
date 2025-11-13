package com.example.backendlaptop.dto.sanpham;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
public class ChiTietSanPhamResponse {
    
    private UUID id;
    private UUID idSanPham;
    private String maCtsp;
    private BigDecimal giaBan;
    private String ghiChu;
    private Integer soLuongTon;
    private Integer soLuongTamGiu;
    private Integer trangThai;
    
    // Thông tin sản phẩm
    private String tenSanPham;
    private String maSanPham;
    
    // Các thuộc tính đặc trưng
    private UUID idCpu;
    private String tenCpu;
    private String maCpu;
    
    private UUID idGpu;
    private String tenGpu;
    private String maGpu;
    
    private UUID idRam;
    private String tenRam;
    private String maRam;
    
    private UUID idOCung;
    private String dungLuongOCung;
    private String maOCung;
    
    private UUID idMauSac;
    private String tenMauSac;
    private String maMauSac;
    private String hexCode;
    
    private UUID idLoaiManHinh;
    private String kichThuocManHinh;
    private String maLoaiManHinh;
    
    private UUID idPin;
    private String dungLuongPin;
    private String maPin;
    
    // Date fields
    private Instant createdAt;
    private Instant updatedAt;
    
    // Thông tin giảm giá
    private BigDecimal giaGoc;           // Giá gốc (trước giảm giá)
    private BigDecimal giaGiam;          // Giá sau giảm giá  
    private Integer phanTramGiam;        // % giảm giá
    private String tenDotGiamGia;        // Tên đợt giảm giá
    private Boolean coGiamGia;           // Có đang giảm giá không
    private Instant ngayBatDauGiam;      // Ngày bắt đầu giảm giá
    private Instant ngayKetThucGiam;     // Ngày kết thúc giảm giá
}
