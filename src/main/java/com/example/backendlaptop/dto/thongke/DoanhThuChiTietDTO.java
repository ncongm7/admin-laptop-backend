package com.example.backendlaptop.dto.thongke;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO chi tiết doanh thu - Phân tích đầy đủ theo nghiệp vụ
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoanhThuChiTietDTO {
    
    // ===== DOANH THU THỰC (ĐÃ XÁC NHẬN) =====
    /**
     * Tổng doanh thu thực tế đã xác nhận
     * = Tiền mặt + QR đã xác nhận + COD đã giao
     */
    private BigDecimal doanhThuThuc;
    
    /**
     * Tiền mặt thu tại quầy (POS)
     * Xác nhận ngay khi tạo đơn
     */
    private BigDecimal tienMatTaiQuay;
    
    /**
     * QR/Chuyển khoản đã xác nhận
     * Chỉ tính khi payment_confirmed_at != NULL
     */
    private BigDecimal qrDaXacNhan;
    
    /**
     * COD đã giao hàng thành công
     * Chỉ tính khi status = HOAN_THANH (4)
     */
    private BigDecimal codDaGiao;
    
    // ===== DOANH THU KỲ VỌNG (ĐANG CHỜ) =====
    /**
     * Tổng tiền đang chờ xác nhận
     * = QR pending + COD đang giao
     */
    private BigDecimal doanhThuKyVong;
    
    /**
     * QR chưa xác nhận (đang pending)
     */
    private BigDecimal qrChuaXacNhan;
    
    /**
     * COD đang giao hàng (status = DANG_GIAO)
     */
    private BigDecimal codDangGiao;
    
    // ===== PHÂN LOẠI THEO KÊNH =====
    /**
     * Doanh thu POS (bán tại quầy)
     */
    private BigDecimal doanhThuPOS;
    
    /**
     * Doanh thu Online
     */
    private BigDecimal doanhThuOnline;
    
    // ===== PHÂN LOẠI THEO PHƯƠNG THỨC =====
    /**
     * Tổng doanh thu qua tiền mặt
     */
    private BigDecimal doanhThuCash;
    
    /**
     * Tổng doanh thu qua QR
     */
    private BigDecimal doanhThuQR;
    
    /**
     * Tổng doanh thu qua COD
     */
    private BigDecimal doanhThuCOD;
    
    // ===== SỐ LƯỢNG ĐƠN =====
    /**
     * Số đơn thành công
     */
    private Long soDonThanhCong;
    
    /**
     * Số đơn đã hủy
     */
    private Long soDonDaHuy;
    
    /**
     * Số đơn đang xử lý
     */
    private Long soDonDangXuLy;

    /**
     * Số đơn QR đang chờ xác nhận
     */
    private Long soDonQRChoXacNhan;

    /**
     * Số đơn COD đang giao
     */
    private Long soDonCODDangGiao;
    
    // ===== TỶ LỆ =====
    /**
     * Tỷ lệ hủy đơn (%)
     */
    private Double tyLeHuyDon;
    
    /**
     * % so với kỳ trước
     */
    private Double soSanhKyTruoc;
}
