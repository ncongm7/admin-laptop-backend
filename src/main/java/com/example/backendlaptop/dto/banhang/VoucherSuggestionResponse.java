package com.example.backendlaptop.dto.banhang;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * DTO cho voucher suggestions
 * Hiển thị thông tin voucher hợp lệ cho hóa đơn hiện tại
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoucherSuggestionResponse {
    
    private UUID id;
    private String ma;
    private String tenPhieuGiamGia;
    private Integer loaiPhieuGiamGia; // 0: Phần trăm, 1: Tiền mặt
    private BigDecimal giaTriGiamGia; // Giá trị giảm (phần trăm hoặc số tiền)
    private BigDecimal soTienGiamToiDa; // Số tiền giảm tối đa (nếu là phần trăm)
    private BigDecimal hoaDonToiThieu; // Điều kiện hóa đơn tối thiểu
    private Integer soLuongDung; // Số lượng còn lại
    private Instant ngayKetThuc; // Ngày hết hạn
    private String moTa; // Mô tả
    
    // Tính toán số tiền giảm dự kiến (để hiển thị cho nhân viên)
    private BigDecimal tienGiamDuKien; // Số tiền sẽ được giảm nếu áp dụng voucher này
    
    // Loại voucher
    private String loaiVoucher; // "PHIEU_GIAM_GIA" hoặc "DOT_GIAM_GIA"
    
    // Constructor cho PhieuGiamGia
    public static VoucherSuggestionResponse fromPhieuGiamGia(
            com.example.backendlaptop.entity.PhieuGiamGia pgg,
            BigDecimal tongTienHoaDon) {
        
        VoucherSuggestionResponse response = new VoucherSuggestionResponse();
        response.setId(pgg.getId());
        response.setMa(pgg.getMa());
        response.setTenPhieuGiamGia(pgg.getTenPhieuGiamGia());
        response.setLoaiPhieuGiamGia(pgg.getLoaiPhieuGiamGia());
        response.setGiaTriGiamGia(pgg.getGiaTriGiamGia());
        response.setSoTienGiamToiDa(pgg.getSoTienGiamToiDa());
        response.setHoaDonToiThieu(pgg.getHoaDonToiThieu());
        response.setSoLuongDung(pgg.getSoLuongDung());
        response.setNgayKetThuc(pgg.getNgayKetThuc());
        response.setMoTa(pgg.getMoTa());
        response.setLoaiVoucher("PHIEU_GIAM_GIA");
        
        // Tính toán số tiền giảm dự kiến
        response.setTienGiamDuKien(calculateTienGiam(pgg, tongTienHoaDon));
        
        return response;
    }
    
    // Constructor cho DotGiamGia (nếu cần)
    public static VoucherSuggestionResponse fromDotGiamGia(
            com.example.backendlaptop.entity.DotGiamGia dgg) {
        
        VoucherSuggestionResponse response = new VoucherSuggestionResponse();
        response.setId(dgg.getId());
        response.setMa("DGG-" + dgg.getId().toString().substring(0, 8));
        response.setTenPhieuGiamGia(dgg.getTenKm());
        response.setGiaTriGiamGia(new BigDecimal(dgg.getGiaTri()));
        response.setNgayKetThuc(dgg.getNgayKetThuc());
        response.setMoTa(dgg.getMoTa());
        response.setLoaiVoucher("DOT_GIAM_GIA");
        // DotGiamGia thường áp dụng trên sản phẩm, không có điều kiện hóa đơn tối thiểu
        response.setHoaDonToiThieu(BigDecimal.ZERO);
        
        return response;
    }
    
    /**
     * Tính toán số tiền giảm dự kiến dựa trên loại voucher và tổng tiền hóa đơn
     */
    private static BigDecimal calculateTienGiam(
            com.example.backendlaptop.entity.PhieuGiamGia pgg,
            BigDecimal tongTienHoaDon) {
        
        if (tongTienHoaDon == null || tongTienHoaDon.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal tienGiam = BigDecimal.ZERO;
        
        // LoaiPhieuGiamGia: 0 = Phần trăm, 1 = Tiền mặt (hoặc giá trị cố định)
        if (pgg.getLoaiPhieuGiamGia() != null && pgg.getLoaiPhieuGiamGia() == 0) {
            // Giảm theo phần trăm
            if (pgg.getGiaTriGiamGia() != null && pgg.getGiaTriGiamGia().compareTo(BigDecimal.ZERO) > 0) {
                tienGiam = tongTienHoaDon
                        .multiply(pgg.getGiaTriGiamGia())
                        .divide(new BigDecimal(100), 2, java.math.RoundingMode.HALF_UP);
                
                // Áp dụng giới hạn tối đa (nếu có)
                if (pgg.getSoTienGiamToiDa() != null && tienGiam.compareTo(pgg.getSoTienGiamToiDa()) > 0) {
                    tienGiam = pgg.getSoTienGiamToiDa();
                }
            }
        } else {
            // Giảm theo số tiền cố định
            if (pgg.getGiaTriGiamGia() != null) {
                tienGiam = pgg.getGiaTriGiamGia();
            }
        }
        
        // Không được giảm nhiều hơn tổng tiền hóa đơn
        if (tienGiam.compareTo(tongTienHoaDon) > 0) {
            tienGiam = tongTienHoaDon;
        }
        
        return tienGiam;
    }
}

