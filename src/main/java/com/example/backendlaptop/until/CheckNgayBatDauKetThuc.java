package com.example.backendlaptop.until;

public final class CheckNgayBatDauKetThuc {
    private CheckNgayBatDauKetThuc() {}

    /** 0 = SẮP_DIỄN_RA, 1 = ĐANG_HIỆU_LỰC, 2 = HẾT_HẠN. Ép end > start. */
    public static int status(java.time.Instant start, java.time.Instant end) {
        if (start == null || end == null) throw new IllegalArgumentException("start/end null");
        if (!end.isAfter(start)) throw new IllegalArgumentException("Ngày bắt đầu phải nhỏ hơn ngày kết thúc");
        var now = java.time.Instant.now();
        return now.isBefore(start) ? 0 : (now.isAfter(end) ? 2 : 1);
    }
}
