package com.example.backendlaptop.service.customer;

import com.example.backendlaptop.dto.customer.TaoDonHangCustomerRequest;
import com.example.backendlaptop.dto.hoadon.HoaDonDetailResponse;
import com.example.backendlaptop.dto.hoadon.HoaDonListResponse;
import com.example.backendlaptop.entity.*;
import com.example.backendlaptop.expection.ApiException;
import com.example.backendlaptop.model.TrangThaiHoaDon;
import com.example.backendlaptop.repository.KhachHangRepository;
import com.example.backendlaptop.repository.banhang.HoaDonChiTietRepository;
import com.example.backendlaptop.repository.banhang.HoaDonRepository;
import com.example.backendlaptop.repository.ChiTietSanPhamRepository;
import com.example.backendlaptop.repository.PhieuGiamGiaRepository;
import com.example.backendlaptop.repository.SerialRepository;
import com.example.backendlaptop.service.WebSocketNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerOrderService {

    private final HoaDonRepository hoaDonRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final KhachHangRepository khachHangRepository;
    private final ChiTietSanPhamRepository chiTietSanPhamRepository;
    private final PhieuGiamGiaRepository phieuGiamGiaRepository;
    private final SerialRepository serialRepository;
    private final WebSocketNotificationService webSocketNotificationService;

    /**
     * Tạo đơn hàng từ customer
     */
    @Transactional
    public HoaDonDetailResponse taoDonHang(TaoDonHangCustomerRequest request) {
        try {
            // 1. Kiểm tra khách hàng tồn tại
            KhachHang khachHang = khachHangRepository.findById(request.getKhachHangId())
                    .orElseThrow(() -> new ApiException("Không tìm thấy khách hàng", "CUSTOMER_NOT_FOUND"));

            // 2. Tạo hóa đơn mới
            HoaDon hoaDon = new HoaDon();
            hoaDon.setIdKhachHang(khachHang);
            hoaDon.setTenKhachHang(request.getTenKhachHang());
            hoaDon.setSdt(request.getSoDienThoai());
            hoaDon.setDiaChi(request.getDiaChi());
            hoaDon.setLoaiHoaDon(1); // 1 = Online
            hoaDon.setGhiChu(request.getGhiChu());
            hoaDon.setNgayTao(Instant.now());
            hoaDon.setTrangThai(TrangThaiHoaDon.CHO_THANH_TOAN); // Chờ thanh toán
            hoaDon.setTrangThaiThanhToan(0); // Chưa thanh toán

            // 3. Tạo mã hóa đơn
            String maHoaDon = "HD" + System.currentTimeMillis();
            hoaDon.setMa(maHoaDon);

            // 4. Xử lý chi tiết sản phẩm
            BigDecimal tongTien = BigDecimal.ZERO;
            List<HoaDonChiTiet> chiTietList = new ArrayList<>();

            for (TaoDonHangCustomerRequest.SanPhamDonHang sp : request.getSanPhams()) {
                ChiTietSanPham ctsp = chiTietSanPhamRepository.findById(sp.getIdCtsp())
                        .orElseThrow(() -> new ApiException("Không tìm thấy sản phẩm: " + sp.getIdCtsp(), "PRODUCT_NOT_FOUND"));

                // Kiểm tra số lượng tồn kho dựa trên Serial (trangThai = 1 = có sẵn)
                // QUAN TRỌNG: Chỉ kiểm tra, KHÔNG trừ kho. Kho sẽ được trừ khi admin xác nhận đơn hàng.
                String tenSanPham = ctsp.getSanPham() != null ? ctsp.getSanPham().getTenSanPham() : "Sản phẩm";
                int soLuongKhaDung = serialRepository.countByCtspIdAndTrangThai(sp.getIdCtsp(), 1);
                if (soLuongKhaDung < sp.getSoLuong()) {
                    throw new ApiException("Sản phẩm " + tenSanPham + " không đủ số lượng. Còn lại: " + soLuongKhaDung, "INSUFFICIENT_STOCK");
                }

                HoaDonChiTiet chiTiet = new HoaDonChiTiet();
                chiTiet.setHoaDon(hoaDon);
                chiTiet.setChiTietSanPham(ctsp);
                chiTiet.setSoLuong(sp.getSoLuong());

                // Tính giá: sử dụng giá bán
                BigDecimal donGia = ctsp.getGiaBan() != null ? ctsp.getGiaBan() : BigDecimal.ZERO;
                chiTiet.setDonGia(donGia);

                BigDecimal thanhTien = donGia.multiply(BigDecimal.valueOf(sp.getSoLuong()));
                tongTien = tongTien.add(thanhTien);
                chiTietList.add(chiTiet);
            }

            hoaDon.setTongTien(tongTien);

            // 5. Xử lý phiếu giảm giá (nếu có)
            BigDecimal tienDuocGiam = BigDecimal.ZERO;
            if (request.getMaPhieuGiamGia() != null && !request.getMaPhieuGiamGia().trim().isEmpty()) {
                PhieuGiamGia phieuGiamGia = phieuGiamGiaRepository.findByMaIgnoreCase(request.getMaPhieuGiamGia())
                        .orElse(null);

                if (phieuGiamGia != null && phieuGiamGia.getTrangThai() == 1) {
                    hoaDon.setIdPhieuGiamGia(phieuGiamGia);

                    if (phieuGiamGia.getLoaiPhieuGiamGia() == 0) { // Phần trăm
                        tienDuocGiam = tongTien.multiply(phieuGiamGia.getGiaTriGiamGia())
                                .divide(BigDecimal.valueOf(100));
                    } else { // Số tiền cố định
                        tienDuocGiam = phieuGiamGia.getGiaTriGiamGia();
                    }

                    if (tienDuocGiam.compareTo(tongTien) > 0) {
                        tienDuocGiam = tongTien;
                    }
                }
            }

            hoaDon.setTienDuocGiam(tienDuocGiam);
            hoaDon.setTongTienSauGiam(tongTien.subtract(tienDuocGiam));

            // 6. Xử lý điểm tích lũy (nếu có)
            if (request.getSoDiemSuDung() != null && request.getSoDiemSuDung() > 0) {
                hoaDon.setSoDiemSuDung(request.getSoDiemSuDung());
                BigDecimal soTienQuyDoi = BigDecimal.valueOf(request.getSoDiemSuDung()).multiply(BigDecimal.valueOf(1000));
                hoaDon.setSoTienQuyDoi(soTienQuyDoi);
                hoaDon.setTongTienSauGiam(hoaDon.getTongTienSauGiam().subtract(soTienQuyDoi));
            }

            // 7. Lưu hóa đơn và chi tiết
            hoaDon = hoaDonRepository.save(hoaDon);
            for (HoaDonChiTiet chiTiet : chiTietList) {
                hoaDonChiTietRepository.save(chiTiet);
            }

            // 8. Gửi WebSocket notification cho đơn hàng mới
            try {
                webSocketNotificationService.notifyNewOnlineOrder(
                    hoaDon.getId(),
                    hoaDon.getMa(),
                    hoaDon.getTenKhachHang()
                );
            } catch (Exception e) {
                System.err.println("⚠️ [CustomerOrderService] Lỗi khi gửi WebSocket notification (không ảnh hưởng đến tạo đơn): " + e.getMessage());
            }

            return new HoaDonDetailResponse(hoaDon);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("Lỗi khi tạo đơn hàng: " + e.getMessage());
            e.printStackTrace();
            throw new ApiException("Lỗi khi tạo đơn hàng: " + e.getMessage(), "CREATE_ORDER_ERROR");
        }
    }

    /**
     * Lấy danh sách đơn hàng của customer
     */
    public Page<HoaDonListResponse> getDanhSachDonHang(UUID khachHangId, Integer page, Integer size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "ngayTao"));
            // Sử dụng Specification để tìm theo khách hàng
            Specification<HoaDon> spec = (root, query, cb) -> {
                Predicate predicate = cb.equal(root.get("idKhachHang").get("id"), khachHangId);
                return predicate;
            };
            Page<HoaDon> hoaDonPage = hoaDonRepository.findAll(spec, pageable);
            return hoaDonPage.map(HoaDonListResponse::new);
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy danh sách đơn hàng: " + e.getMessage());
            e.printStackTrace();
            throw new ApiException("Lỗi khi lấy danh sách đơn hàng: " + e.getMessage(), "GET_ORDERS_ERROR");
        }
    }

    /**
     * Lấy chi tiết đơn hàng của customer
     */
    public HoaDonDetailResponse getChiTietDonHang(UUID idHoaDon) {
        try {
            HoaDon hoaDon = hoaDonRepository.findById(idHoaDon)
                    .orElseThrow(() -> new ApiException("Không tìm thấy đơn hàng", "ORDER_NOT_FOUND"));
            return new HoaDonDetailResponse(hoaDon);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy chi tiết đơn hàng: " + e.getMessage());
            e.printStackTrace();
            throw new ApiException("Lỗi khi lấy chi tiết đơn hàng: " + e.getMessage(), "GET_ORDER_DETAIL_ERROR");
        }
    }

    /**
     * Hủy đơn hàng của customer
     * Chỉ cho phép hủy khi trạng thái = CHO_THANH_TOAN (chưa trừ kho)
     */
    @Transactional
    public HoaDonDetailResponse huyDonHang(UUID idHoaDon, UUID khachHangId) {
        try {
            // 1. Tìm hóa đơn
            HoaDon hoaDon = hoaDonRepository.findById(idHoaDon)
                    .orElseThrow(() -> new ApiException("Không tìm thấy đơn hàng", "ORDER_NOT_FOUND"));

            // 2. Kiểm tra quyền: đơn hàng phải thuộc về khách hàng này
            System.out.println("[CustomerOrderService] huyDonHang - khachHangId từ request: " + khachHangId);
            System.out.println("[CustomerOrderService] huyDonHang - hoaDon.getIdKhachHang(): " + hoaDon.getIdKhachHang());
            
            if (hoaDon.getIdKhachHang() == null) {
                System.out.println("⚠️ [CustomerOrderService] huyDonHang - Đơn hàng không có khách hàng (khách lẻ)");
                throw new ApiException("Bạn không có quyền hủy đơn hàng này", "UNAUTHORIZED");
            }
            
            UUID orderKhachHangId = hoaDon.getIdKhachHang().getId();
            System.out.println("🔍 [CustomerOrderService] huyDonHang - orderKhachHangId: " + orderKhachHangId);
            System.out.println("🔍 [CustomerOrderService] huyDonHang - IDs match: " + orderKhachHangId.equals(khachHangId));
            
            if (!orderKhachHangId.equals(khachHangId)) {
                System.out.println("❌ [CustomerOrderService] huyDonHang - ID không khớp!");
                throw new ApiException("Bạn không có quyền hủy đơn hàng này", "UNAUTHORIZED");
            }
            
            System.out.println("✅ [CustomerOrderService] huyDonHang - Quyền hợp lệ, tiếp tục hủy đơn hàng");

            // 3. Kiểm tra trạng thái: chỉ hủy được khi CHO_THANH_TOAN (chưa trừ kho)
            if (hoaDon.getTrangThai() != TrangThaiHoaDon.CHO_THANH_TOAN) {
                throw new ApiException("Chỉ có thể hủy đơn hàng ở trạng thái 'Chờ thanh toán'. Trạng thái hiện tại: " + hoaDon.getTrangThai(), "INVALID_STATUS");
            }

            // 4. Cập nhật trạng thái thành DA_HUY
            hoaDon.setTrangThai(TrangThaiHoaDon.DA_HUY);
            hoaDon = hoaDonRepository.save(hoaDon);

            System.out.println("✅ [CustomerOrderService] Đã hủy đơn hàng: " + idHoaDon);

            return new HoaDonDetailResponse(hoaDon);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("Lỗi khi hủy đơn hàng: " + e.getMessage());
            e.printStackTrace();
            throw new ApiException("Lỗi khi hủy đơn hàng: " + e.getMessage(), "CANCEL_ORDER_ERROR");
        }
    }
}

