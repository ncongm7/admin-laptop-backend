package com.example.backendlaptop.model;

/**
 * Enum loại lý do bảo hành
 */
public enum LoaiLyDoBaoHanh {
    PHAN_CUNG("PHAN_CUNG", "Phần cứng"),
    PHAN_MEM("PHAN_MEM", "Phần mềm"),
    PHU_KIEN("PHU_KIEN", "Phụ kiện"),
    KHAC("KHAC", "Khác");

    private final String code;
    private final String description;

    LoaiLyDoBaoHanh(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static LoaiLyDoBaoHanh fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (LoaiLyDoBaoHanh loai : LoaiLyDoBaoHanh.values()) {
            if (loai.code.equals(code)) {
                return loai;
            }
        }
        throw new IllegalArgumentException("Không tìm thấy LoaiLyDoBaoHanh với code: " + code);
    }
}

