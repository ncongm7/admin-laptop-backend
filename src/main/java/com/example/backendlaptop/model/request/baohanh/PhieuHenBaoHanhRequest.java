package com.example.backendlaptop.model.request.baohanh;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
public class PhieuHenBaoHanhRequest {
    @NotNull(message = "Ngày hẹn không được để trống")
    private Instant ngayHen;

    @NotNull(message = "Giờ hẹn không được để trống")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime gioHen;

    @NotBlank(message = "Địa điểm không được để trống")
    private String diaDiem;
    
    private String ghiChu;
    private UUID idNhanVienTiepNhan;
}

