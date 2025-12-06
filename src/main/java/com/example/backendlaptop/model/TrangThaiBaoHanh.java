package com.example.backendlaptop.model;

/**
 * Enum trạng thái bảo hành
 * 0: CHO_XU_LY - Chờ xử lý
 * 1: DA_TIEP_NHAN - Đã tiếp nhận
 * 2: DANG_SUA_CHUA - Đang sửa chữa
 * 3: CHO_BAN_GIAO - Chờ bàn giao
 * 4: HOAN_THANH - Hoàn thành
 * 5: DA_HUY - Đã hủy
 */
public enum TrangThaiBaoHanh {
    CHO_XU_LY(0, "Chờ xử lý"),
    DA_TIEP_NHAN(1, "Đã tiếp nhận"),
    DANG_SUA_CHUA(2, "Đang sửa chữa"),
    CHO_BAN_GIAO(3, "Chờ bàn giao"),
    HOAN_THANH(4, "Hoàn thành"),
    DA_HUY(5, "Đã hủy");

    private final Integer value;
    private final String description;

    TrangThaiBaoHanh(Integer value, String description) {
        this.value = value;
        this.description = description;
    }

    public Integer getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public static TrangThaiBaoHanh fromValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (TrangThaiBaoHanh status : TrangThaiBaoHanh.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Không tìm thấy TrangThaiBaoHanh với giá trị: " + value);
    }
}

