package com.example.backendlaptop.model.request.baohanh;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;
@Data
public class LichSuBaoHanhRequest {
    @NotNull(message = "Thiếu id phiếu bảo hành")
    public UUID idPhieuBaoHanh;

    @NotBlank(message = "Thiếu mô tả lỗi")
    public String moTaLoi;
}
