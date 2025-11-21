package com.example.backendlaptop.model.request.trahang;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class TaoYeuCauTraHangRequest {
    @NotNull(message = "Thiếu ID hóa đơn")
    private UUID idHoaDon;

    @NotNull(message = "Thiếu ID khách hàng")
    private UUID idKhachHang;

    @NotNull(message = "Thiếu ID hóa đơn chi tiết")
    private UUID idHoaDonChiTiet;

    private UUID idSerialDaBan; // Optional: chỉ cần khi có serial/IMEI

    @NotBlank(message = "Thiếu lý do trả hàng")
    private String lyDoTraHang;

    @NotNull(message = "Thiếu tình trạng sản phẩm")
    private String tinhTrangLucTra; // Tốt, Hỏng, Trầy xước, Khác

    private String moTaTinhTrang; // Mô tả chi tiết tình trạng

    @NotNull(message = "Thiếu loại yêu cầu")
    private Integer loaiYeuCau; // 0: Đổi trả (hoàn tiền), 1: Bảo hành

    @NotNull(message = "Thiếu số lượng")
    private Integer soLuong; // Số lượng trả

    private List<String> hinhAnhUrls; // Danh sách URL ảnh (sẽ được xử lý từ MultipartFile)
}
