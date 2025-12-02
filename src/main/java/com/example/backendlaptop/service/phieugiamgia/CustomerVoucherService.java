package com.example.backendlaptop.service.phieugiamgia;

import com.example.backendlaptop.dto.banhang.VoucherSuggestionResponse;
import com.example.backendlaptop.dto.giohang.customer.ApplyVoucherRequest;
import com.example.backendlaptop.dto.giohang.customer.VoucherApplyResponse;
import com.example.backendlaptop.entity.PhieuGiamGia;
import com.example.backendlaptop.expection.ApiException;
import com.example.backendlaptop.model.response.phieugiamgia.PhieuGiamGiaResponse;
import com.example.backendlaptop.repository.PhieuGiamGiaRepository;
import com.example.backendlaptop.repository.PhieuGiamGiaKhachHangRepository;
import com.example.backendlaptop.service.banhang.CustomerGioHangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CustomerVoucherService {

    @Autowired
    private PhieuGiamGiaRepository phieuGiamGiaRepository;

    @Autowired
    private CustomerGioHangService customerGioHangService;

    @Autowired
    private PhieuGiamGiaKhachHangRepository phieuGiamGiaKhachHangRepository;

    /**
     * Lấy danh sách voucher khả dụng cho khách hàng
     */
    public List<PhieuGiamGiaResponse> getAvailableVouchers() {
        Instant now = Instant.now();
        
        // Lấy tất cả voucher còn hiệu lực
        List<PhieuGiamGia> allVouchers = phieuGiamGiaRepository.findAll();
        
        return allVouchers.stream()
                .filter(v -> {
                    // Kiểm tra trạng thái active (1)
                    if (v.getTrangThai() == null || v.getTrangThai() != 1) {
                        return false;
                    }
                    
                    // Kiểm tra thời gian
                    if (v.getNgayBatDau() != null && now.isBefore(v.getNgayBatDau())) {
                        return false;
                    }
                    if (v.getNgayKetThuc() != null && now.isAfter(v.getNgayKetThuc())) {
                        return false;
                    }
                    
                    // Kiểm tra số lượng còn lại
                    if (v.getSoLuongDung() != null && v.getSoLuongDung() <= 0) {
                        return false;
                    }
                    
                    // Không lấy voucher riêng tư (riengTu = true)
                    if (v.getRiengTu() != null && v.getRiengTu()) {
                        return false;
                    }
                    
                    return true;
                })
                .map(PhieuGiamGiaResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * Áp dụng voucher vào giỏ hàng
     */
    public VoucherApplyResponse applyVoucher(UUID khachHangId, ApplyVoucherRequest request) {
        // Tìm voucher theo mã
        PhieuGiamGia voucher = phieuGiamGiaRepository.findByMaIgnoreCase(request.getVoucherCode())
                .orElseThrow(() -> new ApiException("Mã giảm giá không tồn tại", "VOUCHER_NOT_FOUND"));

        // Validate voucher
        String validationError = validateVoucher(voucher);
        if (validationError != null) {
            VoucherApplyResponse response = new VoucherApplyResponse();
            response.setSuccess(false);
            response.setMessage(validationError);
            response.setDiscountAmount(BigDecimal.ZERO);
            response.setUpdatedCart(null);
            return response;
        }

        // Lấy giỏ hàng hiện tại
        var cart = customerGioHangService.getCart(khachHangId);
        
        // Validate giá trị đơn hàng tối thiểu
        if (voucher.getHoaDonToiThieu() != null && 
            cart.getSubtotal().compareTo(voucher.getHoaDonToiThieu()) < 0) {
            VoucherApplyResponse response = new VoucherApplyResponse();
            response.setSuccess(false);
            response.setMessage("Đơn hàng tối thiểu " + formatCurrency(voucher.getHoaDonToiThieu()) 
                    + " để áp dụng mã giảm giá này");
            response.setDiscountAmount(BigDecimal.ZERO);
            response.setUpdatedCart(null);
            return response;
        }

        // Tính toán số tiền giảm
        BigDecimal discountAmount = calculateDiscount(voucher, cart.getSubtotal());

        // Cập nhật cart
        cart.setAppliedVoucher(new PhieuGiamGiaResponse(voucher));
        cart.setDiscount(discountAmount);
        cart.setTotal(cart.getSubtotal().subtract(discountAmount).add(cart.getShippingFee()));

        // Trả về response
        VoucherApplyResponse response = new VoucherApplyResponse();
        response.setSuccess(true);
        response.setMessage("Áp dụng mã giảm giá thành công");
        response.setDiscountAmount(discountAmount);
        response.setUpdatedCart(cart);

        return response;
    }

    /**
     * Validate voucher có thể sử dụng hay không
     */
    private String validateVoucher(PhieuGiamGia voucher) {
        Instant now = Instant.now();

        // Kiểm tra trạng thái
        if (voucher.getTrangThai() == null || voucher.getTrangThai() != 1) {
            return "Mã giảm giá đã bị vô hiệu hóa";
        }

        // Kiểm tra thời gian
        if (voucher.getNgayBatDau() != null && now.isBefore(voucher.getNgayBatDau())) {
            return "Mã giảm giá chưa bắt đầu";
        }
        if (voucher.getNgayKetThuc() != null && now.isAfter(voucher.getNgayKetThuc())) {
            return "Mã giảm giá đã hết hạn";
        }

        // Kiểm tra số lượng
        if (voucher.getSoLuongDung() != null && voucher.getSoLuongDung() <= 0) {
            return "Mã giảm giá đã hết lượt sử dụng";
        }

        return null; // Valid
    }

    /**
     * Tính toán số tiền giảm
     */
    private BigDecimal calculateDiscount(PhieuGiamGia voucher, BigDecimal orderTotal) {
        BigDecimal discount = BigDecimal.ZERO;

        if (voucher.getLoaiPhieuGiamGia() == 0) {
            // Giảm theo phần trăm
            discount = orderTotal.multiply(voucher.getGiaTriGiamGia())
                    .divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
            
            // Áp dụng giới hạn giảm tối đa
            if (voucher.getSoTienGiamToiDa() != null && 
                discount.compareTo(voucher.getSoTienGiamToiDa()) > 0) {
                discount = voucher.getSoTienGiamToiDa();
            }
        } else if (voucher.getLoaiPhieuGiamGia() == 1) {
            // Giảm theo số tiền cố định
            discount = voucher.getGiaTriGiamGia();
        }

        // Đảm bảo discount không vượt quá tổng đơn hàng
        if (discount.compareTo(orderTotal) > 0) {
            discount = orderTotal;
        }

        return discount;
    }

    /**
     * Lấy danh sách voucher suggestions cho giỏ hàng
     * Tái sử dụng logic từ KhuyenMaiService nhưng dựa trên giỏ hàng thay vì hóa đơn
     * Bao gồm cả phiếu giảm giá cá nhân (riêng tư)
     * 
     * @param khachHangId - ID khách hàng (null nếu chưa đăng nhập)
     * @param tongTienGioHang - Tổng tiền giỏ hàng
     * @return Danh sách voucher suggestions
     */
    public List<VoucherSuggestionResponse> getVoucherSuggestions(UUID khachHangId, BigDecimal tongTienGioHang) {
        System.out.println("🔍 [CustomerVoucherService] Lấy gợi ý voucher cho giỏ hàng");
        System.out.println("  - ID khách hàng: " + khachHangId);
        System.out.println("  - Tổng tiền giỏ hàng: " + tongTienGioHang);
        
        Instant now = Instant.now();
        
        // Lấy tất cả voucher
        List<PhieuGiamGia> phieuGiamGias = phieuGiamGiaRepository.findAll();
        System.out.println("  - Tổng số voucher trong DB: " + phieuGiamGias.size());
        
        List<VoucherSuggestionResponse> suggestions = phieuGiamGias.stream()
                .filter(pgg -> {
                    System.out.println("  🔍 Kiểm tra voucher: " + pgg.getMa() + " - " + pgg.getTenPhieuGiamGia());
                    
                    // Kiểm tra trạng thái (1 = Hoạt động)
                    if (pgg.getTrangThai() == null || pgg.getTrangThai() != 1) {
                        System.out.println("    ❌ Bị loại: Trạng thái không hoạt động");
                        return false;
                    }
                    
                    // Kiểm tra ngày hiệu lực
                    if (pgg.getNgayBatDau() != null && pgg.getNgayBatDau().isAfter(now)) {
                        System.out.println("    ❌ Bị loại: Chưa đến ngày bắt đầu");
                        return false;
                    }
                    if (pgg.getNgayKetThuc() != null && pgg.getNgayKetThuc().isBefore(now)) {
                        System.out.println("    ❌ Bị loại: Đã hết hạn");
                        return false;
                    }
                    
                    // Kiểm tra số lượng còn lại
                    if (pgg.getSoLuongDung() != null && pgg.getSoLuongDung() <= 0) {
                        System.out.println("    ❌ Bị loại: Hết lượt sử dụng");
                        return false;
                    }
                    
                    // Kiểm tra điều kiện hóa đơn tối thiểu
                    if (pgg.getHoaDonToiThieu() != null && tongTienGioHang.compareTo(pgg.getHoaDonToiThieu()) < 0) {
                        System.out.println("    ❌ Bị loại: Tổng tiền chưa đủ");
                        return false;
                    }
                    
                    // Kiểm tra voucher riêng tư
                    if (Boolean.TRUE.equals(pgg.getRiengTu())) {
                        // Voucher riêng tư - chỉ áp dụng cho khách hàng cụ thể
                        if (khachHangId == null) {
                            System.out.println("    ❌ Bị loại: Voucher riêng tư nhưng không có khách hàng");
                            return false;
                        }
                        // Kiểm tra khách hàng có quyền sử dụng voucher này không
                        boolean coQuyen = phieuGiamGiaKhachHangRepository.existsByPhieuGiamGia_IdAndKhachHang_Id(
                            pgg.getId(), khachHangId);
                        if (!coQuyen) {
                            System.out.println("    ❌ Bị loại: Khách hàng không có quyền sử dụng voucher riêng tư này");
                            return false;
                        }
                        System.out.println("    ✅ Khách hàng có quyền sử dụng voucher riêng tư");
                    }
                    
                    System.out.println("    ✅ Voucher hợp lệ!");
                    return true;
                })
                .map(pgg -> VoucherSuggestionResponse.fromPhieuGiamGia(pgg, tongTienGioHang))
                .collect(Collectors.toList());
        
        // Sắp xếp theo số tiền giảm dự kiến (giảm dần)
        suggestions.sort((a, b) -> {
            BigDecimal tienGiamA = a.getTienGiamDuKien() != null ? a.getTienGiamDuKien() : BigDecimal.ZERO;
            BigDecimal tienGiamB = b.getTienGiamDuKien() != null ? b.getTienGiamDuKien() : BigDecimal.ZERO;
            return tienGiamB.compareTo(tienGiamA); // Giảm dần
        });
        
        System.out.println("✅ [CustomerVoucherService] Tìm thấy " + suggestions.size() + " voucher hợp lệ");
        
        return suggestions;
    }

    /**
     * Format currency for display
     */
    private String formatCurrency(BigDecimal amount) {
        return String.format("%,dđ", amount.longValue());
    }
}

