package com.example.backendlaptop.dto.giohang;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class GioHangChiTietRequest {
    private UUID gioHangId;
    private UUID chiTietSanPhamId;
    private Integer soLuong;
    private BigDecimal donGia;
    private Instant ngayThem;
}
