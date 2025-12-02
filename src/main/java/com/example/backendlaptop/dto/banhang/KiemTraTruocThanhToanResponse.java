package com.example.backendlaptop.dto.banhang;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO response cho API kiểm tra trước khi xác nhận thanh toán
 * Kiểm tra giá, voucher, điểm tích lũy
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KiemTraTruocThanhToanResponse {
    /**
     * Có thay đổi gì không (giá, voucher, hoặc điểm)
     */
    private boolean coThayDoi;
    
    /**
     * Thông báo tổng hợp
     */
    private String message;
    
    /**
     * Thông tin thay đổi về giá sản phẩm
     */
    private ThayDoiGia thayDoiGia;
    
    /**
     * Thông tin thay đổi về voucher
     */
    private ThayDoiVoucher thayDoiVoucher;
    
    /**
     * Thông tin thay đổi về điểm tích lũy
     */
    private ThayDoiDiem thayDoiDiem;
    
    /**
     * Hóa đơn đã được cập nhật
     */
    private HoaDonResponse hoaDonMoi;
    
    /**
     * Thông tin thay đổi về giá
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ThayDoiGia {
        private boolean coThayDoi;
        private Integer soSanPhamThayDoi;
        private List<CapNhatGiaResponse.ThongTinThayDoiGia> danhSachThayDoi;
    }
    
    /**
     * Thông tin thay đổi về voucher
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ThayDoiVoucher {
        private boolean coThayDoi;
        private boolean biXoa; // Voucher có bị xóa khỏi hóa đơn không
        private String lyDo; // Lý do thay đổi/xóa
        private BigDecimal tienGiamCu; // Số tiền giảm cũ
        private BigDecimal tienGiamMoi; // Số tiền giảm mới (nếu không bị xóa)
    }
    
    /**
     * Thông tin thay đổi về điểm tích lũy
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ThayDoiDiem {
        private boolean coThayDoi;
        private boolean biXoa; // Điểm có bị xóa khỏi hóa đơn không
        private String lyDo; // Lý do thay đổi/xóa
        private Integer soDiemCu; // Số điểm sử dụng cũ
        private Integer soDiemMoi; // Số điểm sử dụng mới (nếu không bị xóa)
        private BigDecimal soTienQuyDoiCu; // Số tiền quy đổi cũ
        private BigDecimal soTienQuyDoiMoi; // Số tiền quy đổi mới (nếu không bị xóa)
    }
}

