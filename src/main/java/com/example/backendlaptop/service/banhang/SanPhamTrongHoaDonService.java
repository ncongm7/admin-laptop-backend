package com.example.backendlaptop.service.banhang;

import com.example.backendlaptop.dto.banhang.HoaDonResponse;
import com.example.backendlaptop.dto.banhang.ThemSanPhamRequest;
import com.example.backendlaptop.entity.ChiTietSanPham;
import com.example.backendlaptop.entity.DotGiamGiaChiTiet;
import com.example.backendlaptop.entity.HoaDon;
import com.example.backendlaptop.entity.HoaDonChiTiet;
import com.example.backendlaptop.expection.ApiException;
import com.example.backendlaptop.model.TrangThaiHoaDon;
import com.example.backendlaptop.repository.ChiTietSanPhamRepository;
import com.example.backendlaptop.repository.DotGiamGiaChiTietRepository;
import com.example.backendlaptop.repository.banhang.HoaDonChiTietRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service quản lý sản phẩm trong hóa đơn
 * Nhiệm vụ: Thêm, xóa, cập nhật sản phẩm trong hóa đơn
 * Bao gồm logic tạm giữ và hoàn trả tồn kho
 */
@Service
public class SanPhamTrongHoaDonService {

    @Autowired
    private HoaDonChiTietRepository hoaDonChiTietRepository;

    @Autowired
    private ChiTietSanPhamRepository chiTietSanPhamRepository;

    @Autowired
    private DotGiamGiaChiTietRepository dotGiamGiaChiTietRepository;

    @Autowired
    private BanHangHoaDonService hoaDonService;

    /**
     * Thêm sản phẩm vào hóa đơn chờ
     * Bao gồm:
     * - Kiểm tra tồn kho
     * - Tạm giữ tồn kho
     * - Thêm/cập nhật chi tiết hóa đơn
     * - Tính lại tổng tiền
     */
    @Transactional
    public HoaDonResponse themSanPhamVaoHoaDon(UUID idHoaDon, ThemSanPhamRequest request) {
        System.out.println("🔍 [SanPhamTrongHoaDonService] Thêm sản phẩm vào hóa đơn:");
        System.out.println("  - ID Hóa đơn: " + idHoaDon);
        System.out.println("  - ID Chi tiết sản phẩm: " + request.getChiTietSanPhamId());
        System.out.println("  - Số lượng: " + request.getSoLuong());
        
        // 1. Tìm hóa đơn
        HoaDon hoaDon = hoaDonService.findById(idHoaDon);

        // 2. Kiểm tra trạng thái hóa đơn
        if (hoaDon.getTrangThai() != TrangThaiHoaDon.CHO_THANH_TOAN) {
            System.err.println("❌ [SanPhamTrongHoaDonService] Hóa đơn không ở trạng thái chờ thanh toán");
            throw new ApiException("Chỉ có thể thêm sản phẩm vào hóa đơn đang chờ thanh toán", "BAD_REQUEST");
        }

        // 3. Tìm chi tiết sản phẩm
        UUID chiTietSanPhamId = request.getChiTietSanPhamId();
        System.out.println("🔍 [SanPhamTrongHoaDonService] Tìm kiếm chi tiết sản phẩm với ID: " + chiTietSanPhamId);
        
        ChiTietSanPham ctsp = chiTietSanPhamRepository.findById(chiTietSanPhamId)
                .orElseThrow(() -> {
                    System.err.println("❌ [SanPhamTrongHoaDonService] Không tìm thấy chi tiết sản phẩm với ID: " + chiTietSanPhamId);
                    return new ApiException("Không tìm thấy chi tiết sản phẩm với ID: " + chiTietSanPhamId, "NOT_FOUND");
                });
        
        System.out.println("✅ [SanPhamTrongHoaDonService] Tìm thấy chi tiết sản phẩm: " + ctsp.getMaCtsp());
        System.out.println("  - Giá bán: " + ctsp.getGiaBan());
        System.out.println("  - Số lượng tồn: " + ctsp.getSoLuongTon());
        System.out.println("  - Số lượng tạm giữ: " + ctsp.getSoLuongTamGiu());

        // 4. Kiểm tra giá bán
        if (ctsp.getGiaBan() == null) {
            System.err.println("❌ [SanPhamTrongHoaDonService] Sản phẩm không có giá bán!");
            throw new ApiException("Sản phẩm " + ctsp.getMaCtsp() + " chưa có giá bán. Vui lòng cập nhật giá bán trước khi thêm vào hóa đơn.", "MISSING_PRICE");
        }

        // 5. Kiểm tra tồn kho
        int soLuongTon = ctsp.getSoLuongTon() != null ? ctsp.getSoLuongTon() : 0;
        int soLuongTamGiu = ctsp.getSoLuongTamGiu() != null ? ctsp.getSoLuongTamGiu() : 0;
        int soLuongKhaDung = soLuongTon - soLuongTamGiu;

        System.out.println("  - Số lượng khả dụng: " + soLuongKhaDung);
        System.out.println("  - Số lượng yêu cầu: " + request.getSoLuong());

        if (request.getSoLuong() > soLuongKhaDung) {
            System.err.println("❌ [SanPhamTrongHoaDonService] Không đủ hàng!");
            throw new ApiException("Không đủ hàng. Số lượng khả dụng: " + soLuongKhaDung, "INSUFFICIENT_STOCK");
        }

        // 6. Tạm giữ tồn kho
        ctsp.setSoLuongTamGiu(soLuongTamGiu + request.getSoLuong());
        ensureVersionNotNull(ctsp);
        chiTietSanPhamRepository.save(ctsp);
        System.out.println("✅ [SanPhamTrongHoaDonService] Đã tạm giữ " + request.getSoLuong() + " sản phẩm");

        // 7. Lấy giá từ dot_giam_gia_chi_tiet (nếu có) hoặc giá gốc
        BigDecimal donGia = getGiaBanHienTai(ctsp);
        System.out.println("  - Giá bán hiện tại (đã tính giảm giá): " + donGia);

        // 8. Kiểm tra sản phẩm đã có trong hóa đơn chưa (và cập nhật giá)
        Optional<HoaDonChiTiet> existingHdct = hoaDon.getHoaDonChiTiets().stream()
                .filter(hdct -> hdct.getChiTietSanPham().getId().equals(ctsp.getId()))
                .findFirst();

        if (existingHdct.isPresent()) {
            // Cập nhật số lượng
            System.out.println("✅ [SanPhamTrongHoaDonService] Sản phẩm đã có trong hóa đơn, cập nhật số lượng");
            HoaDonChiTiet hdct = existingHdct.get();
            hdct.setSoLuong(hdct.getSoLuong() + request.getSoLuong());
            // Cập nhật lại giá (phòng trường hợp giá giảm thay đổi)
            hdct.setDonGia(donGia);
            hoaDonChiTietRepository.save(hdct);
        } else {
            // Tạo mới
            System.out.println("✅ [SanPhamTrongHoaDonService] Tạo mới chi tiết hóa đơn");
            HoaDonChiTiet hdct = new HoaDonChiTiet();
            hdct.setHoaDon(hoaDon);
            hdct.setChiTietSanPham(ctsp);
            hdct.setSoLuong(request.getSoLuong());
            hdct.setDonGia(donGia); // Sử dụng giá từ dot_giam_gia_chi_tiet (nếu có)
            hoaDon.getHoaDonChiTiets().add(hdct);
            hoaDonChiTietRepository.save(hdct);
        }

        // 9. Tính lại tổng tiền
        hoaDonService.capNhatTongTien(hoaDon);
        System.out.println("✅ [SanPhamTrongHoaDonService] Hoàn tất thêm sản phẩm vào hóa đơn!");
        
        return new HoaDonResponse(hoaDonService.findById(idHoaDon));
    }
    
    /**
     * Lấy giá bán hiện tại của sản phẩm
     * Ưu tiên lấy giá từ dot_giam_gia_chi_tiet (nếu có và đang hiệu lực)
     * Nếu không có, lấy giá gốc từ chi_tiet_san_pham
     */
    private BigDecimal getGiaBanHienTai(ChiTietSanPham ctsp) {
        // Tìm thông tin giảm giá cho chi tiết sản phẩm này
        List<DotGiamGiaChiTiet> discountList = dotGiamGiaChiTietRepository.findAll();
        Optional<DotGiamGiaChiTiet> dotGiamGiaChiTiet = discountList.stream()
                .filter(d -> d.getIdCtsp() != null && d.getIdCtsp().getId().equals(ctsp.getId()))
                .filter(d -> d.getDotGiamGia() != null && d.getDotGiamGia().getTrangThai() == 1)
                .filter(d -> {
                    Instant now = Instant.now();
                    return d.getDotGiamGia().getNgayBatDau() != null 
                        && d.getDotGiamGia().getNgayKetThuc() != null
                        && !now.isBefore(d.getDotGiamGia().getNgayBatDau())
                        && !now.isAfter(d.getDotGiamGia().getNgayKetThuc());
                })
                .findFirst();
        
        if (dotGiamGiaChiTiet.isPresent()) {
            DotGiamGiaChiTiet discount = dotGiamGiaChiTiet.get();
            BigDecimal giaSauKhiGiam = discount.getGiaSauKhiGiam();
            if (giaSauKhiGiam != null && giaSauKhiGiam.compareTo(BigDecimal.ZERO) > 0) {
                System.out.println("  ✅ [SanPhamTrongHoaDonService] Tìm thấy giá giảm từ dot_giam_gia_chi_tiet: " + giaSauKhiGiam);
                return giaSauKhiGiam;
            }
        }
        
        // Nếu không có giảm giá hoặc giá giảm không hợp lệ, trả về giá gốc
        System.out.println("  ℹ️ [SanPhamTrongHoaDonService] Sử dụng giá gốc: " + ctsp.getGiaBan());
        return ctsp.getGiaBan();
    }

    /**
     * Xóa sản phẩm khỏi hóa đơn chờ
     * Bao gồm:
     * - Hoàn trả tồn kho tạm giữ
     * - Xóa chi tiết hóa đơn
     * - Tính lại tổng tiền
     */
    @Transactional
    public HoaDonResponse xoaSanPhamKhoiHoaDon(UUID idHoaDonChiTiet) {
        // 1. Tìm hóa đơn chi tiết
        HoaDonChiTiet hdct = hoaDonChiTietRepository.findById(idHoaDonChiTiet)
                .orElseThrow(() -> new ApiException("Không tìm thấy chi tiết hóa đơn với ID: " + idHoaDonChiTiet, "NOT_FOUND"));

        // 2. Lấy thông tin
        HoaDon hoaDon = hdct.getHoaDon();
        ChiTietSanPham ctsp = hdct.getChiTietSanPham();
        int soLuong = hdct.getSoLuong();

        // 3. Kiểm tra trạng thái hóa đơn
        if (hoaDon.getTrangThai() != TrangThaiHoaDon.CHO_THANH_TOAN) {
            throw new ApiException("Chỉ có thể xóa sản phẩm khỏi hóa đơn đang chờ thanh toán", "BAD_REQUEST");
        }

        // 4. Hoàn trả tồn kho tạm giữ
        int soLuongTamGiu = ctsp.getSoLuongTamGiu() != null ? ctsp.getSoLuongTamGiu() : 0;
        ctsp.setSoLuongTamGiu(Math.max(0, soLuongTamGiu - soLuong));
        ensureVersionNotNull(ctsp);
        chiTietSanPhamRepository.save(ctsp);

        // 5. Xóa chi tiết hóa đơn
        hoaDon.getHoaDonChiTiets().remove(hdct);
        hoaDonChiTietRepository.delete(hdct);

        // 6. Tính lại tổng tiền
        hoaDonService.capNhatTongTien(hoaDon);

        return new HoaDonResponse(hoaDonService.findById(hoaDon.getId()));
    }

    /**
     * Giải phóng tồn kho tạm giữ cho tất cả sản phẩm trong hóa đơn
     * Được gọi khi xóa hóa đơn chờ
     */
    @Transactional
    public void giaiPhongTonKhoTamGiu(HoaDon hoaDon) {
        if (hoaDon.getHoaDonChiTiets() != null && !hoaDon.getHoaDonChiTiets().isEmpty()) {
            for (HoaDonChiTiet hdct : hoaDon.getHoaDonChiTiets()) {
                ChiTietSanPham ctsp = hdct.getChiTietSanPham();
                int soLuongTamGiu = ctsp.getSoLuongTamGiu() != null ? ctsp.getSoLuongTamGiu() : 0;
                int soLuongGiaiPhong = hdct.getSoLuong();

                // Giải phóng số lượng tạm giữ
                ctsp.setSoLuongTamGiu(Math.max(0, soLuongTamGiu - soLuongGiaiPhong));
                ensureVersionNotNull(ctsp);
                chiTietSanPhamRepository.save(ctsp);
            }
        }
    }

    /**
     * Helper method: Đảm bảo version field của ChiTietSanPham không null
     * Fix lỗi NullPointerException khi Hibernate cố gắng increment @Version field
     */
    private void ensureVersionNotNull(ChiTietSanPham ctsp) {
        if (ctsp.getVersion() == null) {
            ctsp.setVersion(0L);
            System.out.println("⚠️ [SanPhamTrongHoaDonService] Warning: ChiTietSanPham version was null, initialized to 0 for: " + ctsp.getMaCtsp());
        }
    }
}

