package com.example.backendlaptop.model;

public enum LoaiDiem {
    TICH_DIEM(1, "Tích điểm"),
    TIEU_DIEM(2, "Tiêu điểm");
    
    private final Integer value;
    private final String description;
    
    LoaiDiem(Integer value, String description) {
        this.value = value;
        this.description = description;
    }
    
    public Integer getValue() {
        return value;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static LoaiDiem fromValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (LoaiDiem loai : values()) {
            if (loai.value.equals(value)) {
                return loai;
            }
        }
        return null;
    }
}

