package com.example.backendlaptop.dto.hoadon;

import lombok.Data;

import java.time.LocalDate;

/**
 * DTO Request để tìm kiếm và lọc hóa đơn
 */
@Data
public class HoaDonSearchRequest {
    // Phân trang
    private Integer page = 0;
    private Integer size = 10;
    
    // Tìm kiếm
    private String keyword;              // Tìm theo mã HĐ, tên KH, SĐT
    
    // Lọc
    private Integer trangThai;           // 0: Chờ XN, 1: Đã XN, 2: Đang giao, 3: Hoàn thành, 4: Đã hủy
    private Integer loaiHoaDon;          // 0: Tại quầy, 1: Online
    private Integer trangThaiThanhToan;  // 0: Chưa thanh toán, 1: Đã thanh toán
    
    // Lọc theo thời gian
    private LocalDate startDate;
    private LocalDate endDate;
}

