package com.example.backendlaptop.model.request.baohanh;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class BanGiaoRequest {
    private UUID idNhanVienBanGiao;
    private String ghiChu;
    private List<MultipartFile> hinhAnhSauSua;
    private Boolean xacNhanKhachHang;
}

