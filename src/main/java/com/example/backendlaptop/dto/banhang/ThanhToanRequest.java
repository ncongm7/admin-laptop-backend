package com.example.backendlaptop.dto.banhang;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
public class ThanhToanRequest {
    @NotNull(message = "ID phương thức thanh toán không được để trống")
    private UUID idPhuongThucThanhToan;
    
    @NotNull(message = "Số tiền thanh toán không được để trống")
    private BigDecimal soTienThanhToan;
    
    private BigDecimal tienKhachDua; // Số tiền khách đưa (cho thanh toán tiền mặt)
    
    private BigDecimal tienTraLai; // Số tiền trả lại khách (cho thanh toán tiền mặt)
    
    private String ghiChu;
    
    private String maGiaoDich;
    
    /**
     * Danh sách các Serial Number đã được quét/xác thực cho từng sản phẩm
     * YÊU CẦU: Mỗi sản phẩm trong hóa đơn phải có đủ serial number
     */
    @Valid
    private List<SerialThanhToanItem> serialNumbers;
}

