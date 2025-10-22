package com.example.backendlaptop.dto.giohang;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class GioHangRequest {
    private UUID khachHangId;
    private Instant ngayTao;
    private Instant ngayCapNhat;
    private Integer trangThaiGioHang;
}
