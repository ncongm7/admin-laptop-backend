package com.example.backendlaptop.model;

/**
 * Enum phương thức thanh toán
 * Dùng để phân biệt các loại thanh toán và áp dụng logic doanh thu đúng
 */
public enum PaymentMethod {
    /**
     * Tiền mặt - Thanh toán ngay tại quầy
     * Doanh thu được xác nhận NGAY KHI tạo đơn
     */
    CASH,
    
    /**
     * Chuyển khoản QR/Banking
     * Doanh thu CHỈ được tính KHI payment_confirmed_at != NULL
     */
    QR,
    
    /**
     * Cash on Delivery - Thu tiền khi giao hàng
     * Doanh thu CHỈ được tính KHI trạng thái = HOAN_THANH (đã giao)
     */
    COD
}
