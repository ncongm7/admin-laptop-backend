package com.example.backendlaptop.service.customer;

import com.example.backendlaptop.dto.customer.TaoDonHangCustomerRequest;
import com.example.backendlaptop.dto.hoadon.HoaDonDetailResponse;
import com.example.backendlaptop.dto.hoadon.HoaDonListResponse;
import com.example.backendlaptop.entity.*;
import com.example.backendlaptop.expection.ApiException;
import com.example.backendlaptop.model.PaymentMethod;
import com.example.backendlaptop.model.SalesChannel;
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
    private final com.example.backendlaptop.service.SerialService serialService;

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
            
            // Map payment method từ request (0=COD, 1=Online)
            if (request.getPhuongThucThanhToan() != null) {
                if (request.getPhuongThucThanhToan() == 0) {
                    hoaDon.setPaymentMethod(PaymentMethod.COD);
                } else if (request.getPhuongThucThanhToan() == 1) {
                    hoaDon.setPaymentMethod(PaymentMethod.QR);
                }
            }
            
            // Set sales channel
            hoaDon.setSalesChannel(SalesChannel.ONLINE);

            // 3. Tạo mã hóa đơn
            String maHoaDon = "HD" + System.currentTimeMillis();
            hoaDon.setMa(maHoaDon);

            // 3.5. Lưu hóa đơn SỚM (để có ID cho serial reservation)
            // Tổng tiền sẽ được cập nhật sau
            hoaDon.setTongTien(BigDecimal.ZERO);
            hoaDon.setTienDuocGiam(BigDecimal.ZERO);
            hoaDon.setTongTienSauGiam(BigDecimal.ZERO);
            hoaDon = hoaDonRepository.save(hoaDon);

            // 4. Xử lý chi tiết sản phẩm
            BigDecimal tongTien = BigDecimal.ZERO;
            List<HoaDonChiTiet> chiTietList = new ArrayList<>();

            for (TaoDonHangCustomerRequest.SanPhamDonHang sp : request.getSanPhams()) {
                ChiTietSanPham ctsp = chiTietSanPhamRepository.findById(sp.getIdCtsp())
                        .orElseThrow(() -> new ApiException("Không tìm thấy sản phẩm: " + sp.getIdCtsp(), "PRODUCT_NOT_FOUND"));

                String tenSanPham = ctsp.getSanPham() != null ? ctsp.getSanPham().getTenSanPham() : "Sản phẩm";
                
                // Kiểm tra số lượng khả dụng (tính cả tạm giữ của đơn offline)
                // Số lượng khả dụng = Tồn kho - Tạm giữ (của đơn offline)
                int soLuongTon = ctsp.getSoLuongTon() != null ? ctsp.getSoLuongTon() : 0;
                int soLuongTamGiu = ctsp.getSoLuongTamGiu() != null ? ctsp.getSoLuongTamGiu() : 0;
                int soLuongKhaDung = soLuongTon - soLuongTamGiu;
                
                // Kiểm tra số lượng Serial có sẵn (trangThai = 1)
                int soLuongSerialKhaDung = serialRepository.countByCtspIdAndTrangThai(sp.getIdCtsp(), 1);
                
                // Số lượng khả dụng thực tế = min(soLuongKhaDung, soLuongSerialKhaDung)
                int soLuongKhaDungThucTe = Math.min(soLuongKhaDung, soLuongSerialKhaDung);
                
                if (soLuongKhaDungThucTe < sp.getSoLuong()) {
                    throw new ApiException(
                        "Sản phẩm " + tenSanPham + " không đủ số lượng. " +
                        "Cần: " + sp.getSoLuong() + ", " +
                        "Có sẵn: " + soLuongKhaDungThucTe,
                        "INSUFFICIENT_STOCK"
                    );
                }

                HoaDonChiTiet chiTiet = new HoaDonChiTiet();
                chiTiet.setHoaDon(hoaDon); // hoaDon đã có ID rồi
                chiTiet.setChiTietSanPham(ctsp);
                chiTiet.setSoLuong(sp.getSoLuong());

                // Tính giá: sử dụng giá bán
                BigDecimal donGia = ctsp.getGiaBan() != null ? ctsp.getGiaBan() : BigDecimal.ZERO;
                chiTiet.setDonGia(donGia);

                BigDecimal thanhTien = donGia.multiply(BigDecimal.valueOf(sp.getSoLuong()));
                tongTien = tongTien.add(thanhTien);
                chiTietList.add(chiTiet);

                // === RESERVATION LOGIC ===
                // Reserve serials for this item (hoaDon đã có ID)
                Instant expiry = Instant.now().plusSeconds(1800); // Default 30 mins for COD
                
                for (int i = 0; i < sp.getSoLuong(); i++) {
                    Serial reservedSerial = serialService.findAndReserveSerial(ctsp.getId(), hoaDon, expiry);
                    if (reservedSerial == null) {
                        // Double check failure (should be caught by count check above, but racing could cause this)
                        throw new ApiException("Sản phẩm " + tenSanPham + " vừa hết hàng trong khi bạn đang thao tác.", "OUT_OF_STOCK_RACE");
                    }
                }
                
                // === DEDUCT INVENTORY IMMEDIATELY ===
                // Trừ tồn kho ngay khi đặt hàng online (không đợi admin xác nhận)
                // Lý do: Đảm bảo khách online được ưu tiên khi đã đặt trước, tránh bị khách tại quầy mua mất
                int soLuongTonHienTai = ctsp.getSoLuongTon();
                ctsp.setSoLuongTon(soLuongTonHienTai - sp.getSoLuong());
                chiTietSanPhamRepository.save(ctsp);
                
                System.out.println("📦 [CustomerOrder] Đã trừ tồn kho ngay: " + 
                    tenSanPham + " (" + sp.getSoLuong() + " máy). " +
                    "Tồn kho cũ: " + soLuongTonHienTai + " → Tồn kho mới: " + ctsp.getSoLuongTon());
            }

            // 4.5. Cập nhật tổng tiền
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

            // 7. Cập nhật lại hóa đơn với totals cuối cùng
            hoaDon = hoaDonRepository.save(hoaDon);
            
            // 8. Lưu chi tiết (hoaDon đã có ID từ trước)
            for (HoaDonChiTiet chiTiet : chiTietList) {
                hoaDonChiTietRepository.save(chiTiet);
            }

            // 9. Gửi WebSocket notification cho đơn hàng mới
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

            // 4. Hoàn lại tồn kho (vì đã trừ khi đặt hàng)
            List<HoaDonChiTiet> chiTietList = new ArrayList<>(hoaDon.getHoaDonChiTiets());
            for (HoaDonChiTiet hdct : chiTietList) {
                ChiTietSanPham ctsp = hdct.getChiTietSanPham();
                int soLuongHoan = hdct.getSoLuong();
                String tenSanPham = ctsp.getSanPham() != null ? ctsp.getSanPham().getTenSanPham() : "Sản phẩm";
                
                // Hoàn lại tồn kho
                int soLuongTonHienTai = ctsp.getSoLuongTon();
                ctsp.setSoLuongTon(soLuongTonHienTai + soLuongHoan);
                chiTietSanPhamRepository.save(ctsp);
                
                System.out.println("📦 [CustomerOrder] Hoàn lại tồn kho: " + 
                    tenSanPham + " (+" + soLuongHoan + " máy). " +
                    "Tồn kho cũ: " + soLuongTonHienTai + " → Tồn kho mới: " + ctsp.getSoLuongTon());
            }

            // 4.5. Giải phóng serials đã giữ
            serialService.cancelReservation(hoaDon);

            // 5. Cập nhật trạng thái thành DA_HUY
            hoaDon.setTrangThai(TrangThaiHoaDon.DA_HUY);
            hoaDon.setGhiChu(hoaDon.getGhiChu() + " [Đã hủy bởi Khách hàng]");
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

    /**
     * Hủy đơn hàng bởi Hệ thống (Scheduler) khi hết hạn giữ hàng
     */
    @Transactional
    public void cancelOrderSystem(UUID idHoaDon, String reason) {
        try {
            HoaDon hoaDon = hoaDonRepository.findById(idHoaDon)
                    .orElse(null);
            
            if (hoaDon == null) return;
            
            // Chỉ hủy nếu chưa thanh toán
            if (hoaDon.getTrangThaiThanhToan() == 1) return; // Đã thanh toán thì không hủy
            
            // Hoàn lại tồn kho (vì đã trừ khi đặt hàng)
            List<HoaDonChiTiet> chiTietList = new ArrayList<>(hoaDon.getHoaDonChiTiets());
            for (HoaDonChiTiet hdct : chiTietList) {
                ChiTietSanPham ctsp = hdct.getChiTietSanPham();
                int soLuongHoan = hdct.getSoLuong();
                
                int soLuongTonHienTai = ctsp.getSoLuongTon();
                ctsp.setSoLuongTon(soLuongTonHienTai + soLuongHoan);
                chiTietSanPhamRepository.save(ctsp);
                
                System.out.println("📦 [SYSTEM] Hoàn lại tồn kho: " + soLuongHoan + " máy. Tồn kho mới: " + ctsp.getSoLuongTon());
            }
            
            // Giải phóng serials
            serialService.cancelReservation(hoaDon);
            
            // Cập nhật trạng thái
            hoaDon.setTrangThai(TrangThaiHoaDon.DA_HUY);
            hoaDon.setGhiChu((hoaDon.getGhiChu() != null ? hoaDon.getGhiChu() : "") + " [" + reason + "]");
            hoaDonRepository.save(hoaDon);
            
            System.out.println("SYSTEM CANCELLED ORDER: " + idHoaDon + " Reason: " + reason);
            
            // Notify via WebSocket
            try {
                webSocketNotificationService.notifyOrderCancelled(hoaDon.getId(), "Hệ thống hủy đơn hàng do hết hạn giữ hàng.");
            } catch (Exception e) {
                // Ignore socket error
            }
            
        } catch (Exception e) {
            System.err.println("Lỗi khi hủy đơn hệ thống: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

