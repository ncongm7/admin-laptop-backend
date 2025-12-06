package com.example.backendlaptop.model;

/**
 * Enum trạng thái phiếu hẹn bảo hành
 * 0: CHO_XAC_NHAN - Chờ xác nhận
 * 1: DA_XAC_NHAN - Đã xác nhận
 * 2: DA_HUY - Đã hủy
 */
public enum TrangThaiPhieuHen {
    CHO_XAC_NHAN(0, "Chờ xác nhận"),
    DA_XAC_NHAN(1, "Đã xác nhận"),
    DA_HUY(2, "Đã hủy");

    private final Integer value;
    private final String description;

    TrangThaiPhieuHen(Integer value, String description) {
        this.value = value;
        this.description = description;
    }

    public Integer getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public static TrangThaiPhieuHen fromValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (TrangThaiPhieuHen status : TrangThaiPhieuHen.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Không tìm thấy TrangThaiPhieuHen với giá trị: " + value);
    }
}

