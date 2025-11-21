package com.example.backendlaptop.dto.hoadon;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * DTO Response cho đơn hàng online chờ xác nhận
 * Dùng cho Pending Order Ticker
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PendingOrderResponse {
    private UUID id;
    private String ma;
    private String tenKhachHang;
    private Instant ngayTao;
    private BigDecimal tongTienSauGiam;
}

