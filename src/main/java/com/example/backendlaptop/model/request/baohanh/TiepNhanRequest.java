package com.example.backendlaptop.model.request.baohanh;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class TiepNhanRequest {
    @NotNull(message = "ID nhân viên tiếp nhận không được để trống")
    private UUID idNhanVienTiepNhan;

    private String ghiChu;
    private List<MultipartFile> hinhAnhTinhTrang;
    private String xacNhanSerial;
    private String xacNhanTemBaoHanh;
}

