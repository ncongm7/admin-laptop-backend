package com.example.backendlaptop.dto.banhang;

import java.util.UUID;

public class CapNhatKhachHangRequest {
    private UUID khachHangId;

    public CapNhatKhachHangRequest() {}

    public CapNhatKhachHangRequest(UUID khachHangId) {
        this.khachHangId = khachHangId;
    }

    public UUID getKhachHangId() {
        return khachHangId;
    }

    public void setKhachHangId(UUID khachHangId) {
        this.khachHangId = khachHangId;
    }
}
