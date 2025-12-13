package com.example.backendlaptop.service.banhang;

import com.example.backendlaptop.dto.banhang.ApDungVoucherRequest;
import com.example.backendlaptop.dto.banhang.HoaDonResponse;
import com.example.backendlaptop.dto.banhang.VoucherSuggestionResponse;
import com.example.backendlaptop.entity.HoaDon;
import com.example.backendlaptop.entity.PhieuGiamGia;
import com.example.backendlaptop.expection.ApiException;
import com.example.backendlaptop.model.TrangThaiHoaDon;
import com.example.backendlaptop.repository.PhieuGiamGiaRepository;
import com.example.backendlaptop.repository.PhieuGiamGiaKhachHangRepository;
import com.example.backendlaptop.repository.banhang.HoaDonRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service quản lý khuyến mãi/voucher cho hóa đơn
 * Nhiệm vụ: Gợi ý voucher, áp dụng voucher, xóa voucher
 */
@Service
public class KhuyenMaiService {

    @Autowired
    private PhieuGiamGiaRepository phieuGiamGiaRepository;
    
    @Autowired
    private PhieuGiamGiaKhachHangRepository phieuGiamGiaKhachHangRepository;

    @Autowired
    private BanHangHoaDonService hoaDonService;

    @Autowired
    private HoaDonRepository hoaDonRepository;

    /**
     * Gợi ý Voucher/Khuyến mãi cho hóa đơn hiện tại
     * Logic: Tìm tất cả voucher/khuyến mãi hợp lệ dựa trên:
     * - Tổng tiền hóa đơn
     * - ID khách hàng (cho voucher riêng tư)
     * - Ngày hiệu lực
     * - Số lượng còn lại
     * - Trạng thái
     */
    public List<VoucherSuggestionResponse> getVoucherSuggestions(UUID idHoaDon) {
        System.out.println("🔍 [KhuyenMaiService] Lấy gợi ý voucher cho hóa đơn: " + idHoaDon);
        
        // 1. Tìm hóa đơn
        HoaDon hoaDon = hoaDonService.findById(idHoaDon);
        
        // 2. Lấy thông tin cần thiết
        BigDecimal tongTien = hoaDon.getTongTien() != null ? hoaDon.getTongTien() : BigDecimal.ZERO;
        UUID idKhachHang = hoaDon.getIdKhachHang() != null ? hoaDon.getIdKhachHang().getId() : null;
        Instant now = Instant.now();
        
        System.out.println("  - Tổng tiền hóa đơn: " + tongTien);
        System.out.println("  - ID khách hàng: " + idKhachHang);
        
        // 3. Lấy danh sách PhieuGiamGia hợp lệ
        List<PhieuGiamGia> phieuGiamGias = phieuGiamGiaRepository.findAll();
        System.out.println("  - Tổng số voucher trong DB: " + phieuGiamGias.size());
        
        List<VoucherSuggestionResponse> suggestions = phieuGiamGias.stream()
                .filter(pgg -> {
                    System.out.println("  🔍 Kiểm tra voucher: " + pgg.getMa() + " - " + pgg.getTenPhieuGiamGia());
                    
                    // Kiểm tra trạng thái (1 = Hoạt động)
                    if (pgg.getTrangThai() == null || pgg.getTrangThai() != 1) {
                        System.out.println("    ❌ Bị loại: Trạng thái không hoạt động (trangThai=" + pgg.getTrangThai() + ")");
                        return false;
                    }
                    
                    // Kiểm tra ngày hiệu lực
                    if (pgg.getNgayBatDau() != null && pgg.getNgayBatDau().isAfter(now)) {
                        System.out.println("    ❌ Bị loại: Chưa đến ngày bắt đầu (ngayBatDau=" + pgg.getNgayBatDau() + ")");
                        return false; // Chưa đến ngày bắt đầu
                    }
                    if (pgg.getNgayKetThuc() != null && pgg.getNgayKetThuc().isBefore(now)) {
                        System.out.println("    ❌ Bị loại: Đã hết hạn (ngayKetThuc=" + pgg.getNgayKetThuc() + ", now=" + now + ")");
                        return false; // Đã hết hạn
                    }
                    
                    // Kiểm tra số lượng còn lại
                    if (pgg.getSoLuongDung() != null && pgg.getSoLuongDung() <= 0) {
                        System.out.println("    ❌ Bị loại: Hết lượt sử dụng (soLuongDung=" + pgg.getSoLuongDung() + ")");
                        return false; // Hết lượt sử dụng
                    }
                    
                    // Kiểm tra điều kiện hóa đơn tối thiểu
                    if (pgg.getHoaDonToiThieu() != null && tongTien.compareTo(pgg.getHoaDonToiThieu()) < 0) {
                        System.out.println("    ❌ Bị loại: Tổng tiền chưa đủ (tongTien=" + tongTien + ", hoaDonToiThieu=" + pgg.getHoaDonToiThieu() + ")");
                        return false; // Tổng tiền chưa đủ điều kiện
                    }
                    
                    // Kiểm tra voucher riêng tư
                    if (Boolean.TRUE.equals(pgg.getRiengTu())) {
                        // Voucher riêng tư - chỉ áp dụng cho khách hàng cụ thể
                        if (idKhachHang == null) {
                            System.out.println("    ❌ Bị loại: Voucher riêng tư nhưng không có khách hàng");
                            return false; // Khách lẻ không dùng được voucher riêng tư
                        }
                        // Kiểm tra khách hàng có quyền sử dụng voucher này không
                        boolean coQuyen = phieuGiamGiaKhachHangRepository.existsByPhieuGiamGia_IdAndKhachHang_Id(
                            pgg.getId(), idKhachHang);
                        if (!coQuyen) {
                            System.out.println("    ❌ Bị loại: Khách hàng không có quyền sử dụng voucher riêng tư này");
                            return false; // Khách hàng không có trong danh sách được gán voucher
                        }

                        // Kiểm tra xem khách hàng đã từng sử dụng voucher này chưa (trừ hóa đơn đã hủy)
                        boolean daSuDung = hoaDonRepository.existsByIdKhachHang_IdAndIdPhieuGiamGia_IdAndTrangThaiNot(
                            idKhachHang, pgg.getId(), TrangThaiHoaDon.DA_HUY);
                        if (daSuDung) {
                            System.out.println("    ❌ Bị loại: Khách hàng đã sử dụng voucher này rồi");
                            return false;
                        }

                        System.out.println("    ✅ Khách hàng có quyền sử dụng voucher riêng tư");
                    }
                    
                    System.out.println("    ✅ Voucher hợp lệ!");
                    return true;
                })
                .map(pgg -> VoucherSuggestionResponse.fromPhieuGiamGia(pgg, tongTien))
                .collect(Collectors.toList());
        
        // Sắp xếp theo số tiền giảm dự kiến (giảm dần)
        suggestions.sort((a, b) -> {
            BigDecimal tienGiamA = a.getTienGiamDuKien() != null ? a.getTienGiamDuKien() : BigDecimal.ZERO;
            BigDecimal tienGiamB = b.getTienGiamDuKien() != null ? b.getTienGiamDuKien() : BigDecimal.ZERO;
            return tienGiamB.compareTo(tienGiamA); // Giảm dần
        });
        
        System.out.println("✅ [KhuyenMaiService] Tìm thấy " + suggestions.size() + " voucher hợp lệ");
        
        return suggestions;
    }
    
    /**
     * Áp dụng Voucher/Phiếu giảm giá vào hóa đơn
     * Logic:
     * 1. Kiểm tra voucher hợp lệ
     * 2. Tính toán số tiền giảm
     * 3. Cập nhật hóa đơn: idPhieuGiamGia, tienDuocGiam, tongTienSauGiam
     * 4. Giảm soLuongDung của voucher
     */
    @Transactional
    public HoaDonResponse apDungVoucher(UUID idHoaDon, ApDungVoucherRequest request) {
        System.out.println("🎟️ [KhuyenMaiService] Áp dụng voucher cho hóa đơn: " + idHoaDon);
        System.out.println("  - ID Voucher: " + request.getIdPhieuGiamGia());
        
        // 1. Tìm hóa đơn
        HoaDon hoaDon = hoaDonService.findById(idHoaDon);
        
        // 2. Kiểm tra trạng thái hóa đơn
        if (hoaDon.getTrangThai() != TrangThaiHoaDon.CHO_THANH_TOAN) {
            throw new ApiException("Chỉ có thể áp dụng voucher cho hóa đơn đang chờ thanh toán", "INVALID_STATUS");
        }
        
        // 3. Tìm voucher
        PhieuGiamGia phieuGiamGia = phieuGiamGiaRepository.findById(request.getIdPhieuGiamGia())
                .orElseThrow(() -> new ApiException("Không tìm thấy phiếu giảm giá với ID: " + request.getIdPhieuGiamGia(), "NOT_FOUND"));
        
        // 4. Kiểm tra voucher hợp lệ
        validateVoucher(phieuGiamGia, hoaDon);
        
        // 5. Tính toán số tiền giảm
        BigDecimal tongTien = hoaDon.getTongTien() != null ? hoaDon.getTongTien() : BigDecimal.ZERO;
        BigDecimal tienDuocGiam = calculateTienGiam(phieuGiamGia, tongTien);
        
        System.out.println("  - Tổng tiền hóa đơn: " + tongTien);
        System.out.println("  - Số tiền giảm: " + tienDuocGiam);
        
        // 6. Cập nhật hóa đơn
        hoaDon.setIdPhieuGiamGia(phieuGiamGia);
        hoaDon.setTienDuocGiam(tienDuocGiam);
        
        // Tính lại tổng tiền sau giảm
        BigDecimal tongTienSauGiam = tongTien.subtract(tienDuocGiam);
        if (tongTienSauGiam.compareTo(BigDecimal.ZERO) < 0) {
            tongTienSauGiam = BigDecimal.ZERO; // Không được âm
        }
        hoaDon.setTongTienSauGiam(tongTienSauGiam);
        
        // 7. Giảm số lượng sử dụng của voucher
        if (phieuGiamGia.getSoLuongDung() != null && phieuGiamGia.getSoLuongDung() > 0) {
            phieuGiamGia.setSoLuongDung(phieuGiamGia.getSoLuongDung() - 1);
            phieuGiamGiaRepository.save(phieuGiamGia);
        }
        
        // 8. Lưu hóa đơn
        hoaDonService.save(hoaDon);
        
        System.out.println("✅ [KhuyenMaiService] Áp dụng voucher thành công!");
        
        return new HoaDonResponse(hoaDonService.findById(idHoaDon));
    }
    
    /**
     * Xóa voucher khỏi hóa đơn
     */
    @Transactional
    public HoaDonResponse xoaVoucher(UUID idHoaDon) {
        System.out.println("🗑️ [KhuyenMaiService] Xóa voucher khỏi hóa đơn: " + idHoaDon);
        // 1. Tìm hóa đơn
        HoaDon hoaDon = hoaDonService.findById(idHoaDon);
        
        // 2. Kiểm tra trạng thái
        if (hoaDon.getTrangThai() != TrangThaiHoaDon.CHO_THANH_TOAN) {
            throw new ApiException("Chỉ có thể xóa voucher khỏi hóa đơn đang chờ thanh toán", "INVALID_STATUS");
        }
        
        // 3. Hoàn trả số lượng voucher (nếu có)
        if (hoaDon.getIdPhieuGiamGia() != null) {
            PhieuGiamGia pgg = hoaDon.getIdPhieuGiamGia();
            if (pgg.getSoLuongDung() != null) {
                pgg.setSoLuongDung(pgg.getSoLuongDung() + 1);
                phieuGiamGiaRepository.save(pgg);
            }
        }
        
        // 4. Xóa voucher khỏi hóa đơn
        hoaDon.setIdPhieuGiamGia(null);
        hoaDon.setTienDuocGiam(BigDecimal.ZERO);
        
        // Tính lại tổng tiền sau giảm
        BigDecimal tongTien = hoaDon.getTongTien() != null ? hoaDon.getTongTien() : BigDecimal.ZERO;
        hoaDon.setTongTienSauGiam(tongTien);
        
        // 5. Lưu hóa đơn
        hoaDonService.save(hoaDon);
        
        System.out.println("✅ [KhuyenMaiService] Xóa voucher thành công!");
        
        return new HoaDonResponse(hoaDonService.findById(idHoaDon));
    }
    
    /**
     * Validate voucher có thể áp dụng cho hóa đơn hay không
     */
    private void validateVoucher(PhieuGiamGia phieuGiamGia, HoaDon hoaDon) {
        Instant now = Instant.now();
        BigDecimal tongTien = hoaDon.getTongTien() != null ? hoaDon.getTongTien() : BigDecimal.ZERO;
        UUID idKhachHang = hoaDon.getIdKhachHang() != null ? hoaDon.getIdKhachHang().getId() : null;
        
        // 4.1. Kiểm tra trạng thái
        if (phieuGiamGia.getTrangThai() == null || phieuGiamGia.getTrangThai() != 1) {
            throw new ApiException("Voucher không hoạt động", "VOUCHER_INACTIVE");
        }
        
        // 4.2. Kiểm tra ngày hiệu lực
        if (phieuGiamGia.getNgayBatDau() != null && phieuGiamGia.getNgayBatDau().isAfter(now)) {
            throw new ApiException("Voucher chưa đến thời gian hiệu lực", "VOUCHER_NOT_STARTED");
        }
        if (phieuGiamGia.getNgayKetThuc() != null && phieuGiamGia.getNgayKetThuc().isBefore(now)) {
            throw new ApiException("Voucher đã hết hạn", "VOUCHER_EXPIRED");
        }
        
        // 4.3. Kiểm tra số lượng còn lại
        if (phieuGiamGia.getSoLuongDung() != null && phieuGiamGia.getSoLuongDung() <= 0) {
            throw new ApiException("Voucher đã hết lượt sử dụng", "VOUCHER_OUT_OF_STOCK");
        }
        
        // 4.4. Kiểm tra điều kiện hóa đơn tối thiểu
        if (phieuGiamGia.getHoaDonToiThieu() != null && tongTien.compareTo(phieuGiamGia.getHoaDonToiThieu()) < 0) {
            throw new ApiException(
                String.format("Hóa đơn chưa đủ điều kiện. Tối thiểu: %s", 
                    formatCurrency(phieuGiamGia.getHoaDonToiThieu())),
                "INSUFFICIENT_ORDER_VALUE"
            );
        }
        
        // 4.5. Kiểm tra voucher riêng tư
        if (Boolean.TRUE.equals(phieuGiamGia.getRiengTu())) {
            if (idKhachHang == null) {
                throw new ApiException("Voucher này chỉ dành cho khách hàng thành viên", "VOUCHER_PRIVATE");
            }
            // Kiểm tra khách hàng có quyền sử dụng voucher này không
            boolean coQuyen = phieuGiamGiaKhachHangRepository.existsByPhieuGiamGia_IdAndKhachHang_Id(
                phieuGiamGia.getId(), idKhachHang);
            if (!coQuyen) {
                throw new ApiException("Bạn không có quyền sử dụng phiếu giảm giá này", "VOUCHER_NO_PERMISSION");
            }
        }
    }
    
    /**
     * Tính toán số tiền giảm dựa trên voucher và tổng tiền hóa đơn
     */
    private BigDecimal calculateTienGiam(PhieuGiamGia pgg, BigDecimal tongTien) {
        if (tongTien == null || tongTien.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal tienGiam = BigDecimal.ZERO;
        
        // LoaiPhieuGiamGia: 0 = Phần trăm, 1 = Tiền mặt
        if (pgg.getLoaiPhieuGiamGia() != null && pgg.getLoaiPhieuGiamGia() == 0) {
            // Giảm theo phần trăm
            if (pgg.getGiaTriGiamGia() != null && pgg.getGiaTriGiamGia().compareTo(BigDecimal.ZERO) > 0) {
                tienGiam = tongTien
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
        if (tienGiam.compareTo(tongTien) > 0) {
            tienGiam = tongTien;
        }
        
        return tienGiam;
    }
    
    /**
     * Helper: Format currency (để hiển thị trong error message)
     */
    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "0";
        return amount.toString();
    }
}
