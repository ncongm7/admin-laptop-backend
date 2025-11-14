package com.example.backendlaptop.dto.customer;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import java.util.List;
import java.util.UUID;

@Data
public class TaoDonHangCustomerRequest {
    @NotNull(message = "Khách hàng ID không được để trống")
    private UUID khachHangId;
    
    @NotBlank(message = "Tên khách hàng không được để trống")
    private String tenKhachHang;
    
    @NotBlank(message = "Số điện thoại không được để trống")
    private String soDienThoai;
    
    @NotBlank(message = "Địa chỉ không được để trống")
    private String diaChi;
    
    private String email;
    
    @NotNull(message = "Phương thức thanh toán không được để trống")
    private Integer phuongThucThanhToan; // 0: COD, 1: Online
    
    private String ghiChu;
    
    private String maPhieuGiamGia;
    
    private Integer soDiemSuDung;
    
    @NotNull(message = "Danh sách sản phẩm không được để trống")
    private List<SanPhamDonHang> sanPhams;
    
    @Data
    public static class SanPhamDonHang {
        @NotNull(message = "ID chi tiết sản phẩm không được để trống")
        private UUID idCtsp;
        
        @NotNull(message = "Số lượng không được để trống")
        @Min(value = 1, message = "Số lượng phải lớn hơn 0")
        private Integer soLuong;
    }
}

