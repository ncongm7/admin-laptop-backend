package com.example.backendlaptop.model;

public enum TrangThaiDiem {
    CHUA_SU_DUNG(0, "Chưa sử dụng"),
    DA_SU_DUNG(1, "Đã sử dụng"),
    HET_HAN(2, "Hết hạn");
    
    private final Integer value;
    private final String description;
    
    TrangThaiDiem(Integer value, String description) {
        this.value = value;
        this.description = description;
    }
    
    public Integer getValue() {
        return value;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static TrangThaiDiem fromValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (TrangThaiDiem trangThai : values()) {
            if (trangThai.value.equals(value)) {
                return trangThai;
            }
        }
        return null;
    }
}

