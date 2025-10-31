package com.example.backendlaptop.model;

/**
 * Enum trạng thái hóa đơn
 * 0: CHO_THANH_TOAN - Chờ thanh toán
 * 1: DA_THANH_TOAN - Đã thanh toán
 * 2: DA_HUY - Đã hủy
 * 3: DANG_GIAO - Đang giao (nếu có trong DB)
 * 4: HOAN_THANH - Hoàn thành (nếu có trong DB)
 */
public enum TrangThaiHoaDon {
    CHO_THANH_TOAN,    // 0
    DA_THANH_TOAN,     // 1
    DA_HUY,            // 2
    DANG_GIAO,         // 3
    HOAN_THANH         // 4
}
