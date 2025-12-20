package com.example.backendlaptop.model;

/**
 * Enum kênh bán hàng
 * Dùng để phân biệt bán tại quầy vs bán online
 */
public enum SalesChannel {
    /**
     * Point of Sale - Bán tại quầy
     * Khách hàng đến cửa hàng mua trực tiếp
     */
    POS,
    
    /**
     * Bán online qua website/app
     * Khách hàng đặt hàng trực tuyến và nhận ship về
     */
    ONLINE
}
