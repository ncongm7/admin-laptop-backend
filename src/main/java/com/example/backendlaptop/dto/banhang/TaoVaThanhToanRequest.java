package com.example.backendlaptop.dto.banhang;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * DTO cho API tạo và thanh toán hóa đơn trong 1 transaction
 * Dùng khi hóa đơn chỉ tồn tại local (frontend), chưa insert DB
 */
@Data
public class TaoVaThanhToanRequest {
    
    // Thông tin hóa đơn cơ bản
    @NotNull(message = "ID nhân viên không được để trống")
    private UUID nhanVienId;
    
    private UUID khachHangId; // Null = khách vãng lai
    
    private UUID idPhieuGiamGia; // ID voucher (nếu có)
    
    private Integer diemSuDung = 0; // Số điểm tích lũy sử dụng
    
    // Danh sách chi tiết sản phẩm
    @NotNull(message = "Danh sách sản phẩm không được để trống")
    @Valid
    private List<ChiTietSanPhamItem> chiTietList;
    
    // Thông tin thanh toán
    @NotNull(message = "Phương thức thanh toán không được để trống")
    private String phuongThucThanhToan; // TIEN_MAT, CHUYEN_KHOAN, THE, ...
    
    private BigDecimal tienKhachDua = BigDecimal.ZERO;
    
    private BigDecimal tienThua = BigDecimal.ZERO;
    
    private String ghiChu;
    
    // Thông tin tổng tiền (để backend validate)
    private BigDecimal tongTien;
    
    private BigDecimal tongTienSauGiam;
    
    private BigDecimal tienDuocGiam;
    
    /**
     * Inner class cho từng item trong chi tiết hóa đơn
     */
    @Data
    public static class ChiTietSanPhamItem {
        @NotNull(message = "ID chi tiết sản phẩm không được để trống")
        private UUID chiTietSanPhamId;
        
        @NotNull(message = "Số lượng không được để trống")
        private Integer soLuong;
        
        @NotNull(message = "Đơn giá không được để trống")
        private BigDecimal donGia;
    }
}
