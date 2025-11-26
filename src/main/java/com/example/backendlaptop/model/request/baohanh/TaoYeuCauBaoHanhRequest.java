package com.example.backendlaptop.model.request.baohanh;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class TaoYeuCauBaoHanhRequest {

    @NotNull(message = "Thiếu ID hóa đơn")
    private UUID idHoaDon;

    @NotNull(message = "Thiếu ID khách hàng")
    private UUID idKhachHang;

    @NotNull(message = "Thiếu ID hóa đơn chi tiết")
    private UUID idHoaDonChiTiet;

    private UUID idSerialDaBan;

    @NotBlank(message = "Thiếu lý do bảo hành")
    private String lyDoTraHang;

    @NotBlank(message = "Thiếu tình trạng sản phẩm")
    private String tinhTrangLucTra;

    private String moTaTinhTrang;

    @NotNull(message = "Thiếu số lượng")
    private Integer soLuong;

    private List<String> hinhAnhUrls;
}
