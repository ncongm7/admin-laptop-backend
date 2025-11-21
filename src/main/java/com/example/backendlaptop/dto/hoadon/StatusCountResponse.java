package com.example.backendlaptop.dto.hoadon;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO Response cho số lượng hóa đơn theo trạng thái
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatusCountResponse {
    private Long total; // Tổng số hóa đơn
    private Long CHO_THANH_TOAN; // Trạng thái 0
    private Long DA_THANH_TOAN; // Trạng thái 1
    private Long DANG_GIAO; // Trạng thái 2
    private Long HOAN_THANH; // Trạng thái 3
    private Long DA_HUY; // Trạng thái 4
}

