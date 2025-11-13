package com.example.backendlaptop.dto.thongke;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO response cho API biểu đồ doanh số
 * Trả về danh sách các điểm dữ liệu theo khoảng thời gian group by
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BieuDoDoanhSoResponse {
    private String thoiGian; // Ngày/Tuần/Tháng/Năm - format tùy groupBy
    private BigDecimal doanhThu; // Tổng doanh thu trong khoảng thời gian đó
    private Long soHoaDon; // Số lượng hóa đơn trong khoảng thời gian đó
}

