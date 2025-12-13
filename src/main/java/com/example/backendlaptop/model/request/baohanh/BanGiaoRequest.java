package com.example.backendlaptop.model.request.baohanh;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class BanGiaoRequest {
    @NotNull(message = "ID nhân viên bàn giao không được để trống")
    private UUID idNhanVienBanGiao;
    private String ghiChu;
    private List<MultipartFile> hinhAnhSauSua;
    private Boolean xacNhanKhachHang;
}
