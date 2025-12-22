package com.example.backendlaptop.service.banhang;

import com.example.backendlaptop.dto.banhang.CapNhatGiaResponse;
import com.example.backendlaptop.dto.banhang.HoaDonResponse;
import com.example.backendlaptop.dto.banhang.KiemTraTruocThanhToanResponse;
import com.example.backendlaptop.dto.banhang.SerialThanhToanItem;
import com.example.backendlaptop.dto.banhang.ThanhToanRequest;
import com.example.backendlaptop.dto.banhang.XacThucSerialRequest;
import com.example.backendlaptop.dto.banhang.XacThucSerialResponse;
import com.example.backendlaptop.entity.ChiTietSanPham;
import com.example.backendlaptop.entity.ChiTietThanhToan;
import com.example.backendlaptop.entity.DotGiamGiaChiTiet;
import com.example.backendlaptop.entity.HoaDon;
import com.example.backendlaptop.entity.HoaDonChiTiet;
import com.example.backendlaptop.entity.PhieuGiamGia;
import com.example.backendlaptop.entity.PhuongThucThanhToan;
import com.example.backendlaptop.entity.QuyDoiDiem;
import com.example.backendlaptop.entity.Serial;
import com.example.backendlaptop.entity.SerialDaBan;
import com.example.backendlaptop.entity.TichDiem;
import com.example.backendlaptop.expection.ApiException;
import com.example.backendlaptop.model.TrangThaiHoaDon;
import com.example.backendlaptop.repository.ChiTietSanPhamRepository;
import com.example.backendlaptop.repository.ChiTietThanhToanRepository;
import com.example.backendlaptop.repository.DotGiamGiaChiTietRepository;
import com.example.backendlaptop.repository.PhieuGiamGiaKhachHangRepository;
import com.example.backendlaptop.repository.PhieuGiamGiaRepository;
import com.example.backendlaptop.repository.PhuongThucThanhToanRepository;
import com.example.backendlaptop.repository.QuyDoiDiemRepository;
import com.example.backendlaptop.repository.SerialDaBanRepository;
import com.example.backendlaptop.repository.SerialRepository;
import com.example.backendlaptop.repository.TichDiemRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Service xử lý thanh toán hóa đơn
 * Nhiệm vụ:
 * - Xác thực Serial Numbers
 * - Xử lý thanh toán cuối cùng (cập nhật tồn kho, Serial, trạng thái hóa đơn)
 * Đây là nghiệp vụ quan trọng và phức tạp nhất, phải được bọc
 * trong @Transactional
 */
@Service
public class ThanhToanService {

    @Autowired
    private BanHangHoaDonService hoaDonService;

    @Autowired
    private SerialRepository serialRepository;

    @Autowired
    private SerialDaBanRepository serialDaBanRepository;

    @Autowired
    private ChiTietSanPhamRepository chiTietSanPhamRepository;

    @Autowired
    private PhuongThucThanhToanRepository phuongThucThanhToanRepository;

    @Autowired
    private ChiTietThanhToanRepository chiTietThanhToanRepository;

    @Autowired
    private PhieuGiamGiaRepository phieuGiamGiaRepository;

    @Autowired
    private PhieuGiamGiaKhachHangRepository phieuGiamGiaKhachHangRepository;

    @Autowired
    private TichDiemRepository tichDiemRepository;

    @Autowired
    private QuyDoiDiemRepository quyDoiDiemRepository;

    @Autowired
    private DotGiamGiaChiTietRepository dotGiamGiaChiTietRepository;

    @Autowired
    private com.example.backendlaptop.service.diem.TichDiemService tichDiemService;

    @Autowired
    private com.example.backendlaptop.service.SerialService serialService;

    /**
     * Xác Thực Serial Number
     * Endpoint: POST /api/v1/ban-hang/hoa-don/xac-thuc-serial
     */
    public XacThucSerialResponse xacThucSerial(XacThucSerialRequest request) {
        // 1. Kiểm tra hóa đơn tồn tại
        HoaDon hoaDon = hoaDonService.findById(request.getIdHoaDon());

        // 2. Kiểm tra trạng thái hóa đơn (phải là CHO_THANH_TOAN)
        if (hoaDon.getTrangThai() != TrangThaiHoaDon.CHO_THANH_TOAN) {
            return new XacThucSerialResponse(
                    false,
                    "Hóa đơn không ở trạng thái chờ thanh toán",
                    null,
                    request.getSerialNumber(),
                    null,
                    null);
        }

        // 3. Tìm serial theo serialNumber và ctspId
        Optional<Serial> serialOpt = serialRepository.findBySerialNoAndCtspId(
                request.getSerialNumber(),
                request.getIdChiTietSanPham());

        if (serialOpt.isEmpty()) {
            return new XacThucSerialResponse(
                    false,
                    "Serial không tồn tại hoặc không thuộc sản phẩm này",
                    null,
                    request.getSerialNumber(),
                    null,
                    null);
        }

        Serial serial = serialOpt.get();

        // 4. Kiểm tra trạng thái serial (phải là 1 = Chưa bán / Trong kho)
        if (serial.getTrangThai() == null || serial.getTrangThai() != 1) {
            String statusMessage = switch (serial.getTrangThai() != null ? serial.getTrangThai() : -1) {
                case 2 -> "Serial đã được bán";
                case 0 -> "Serial bị hỏng/không khả dụng";
                default -> "Serial có trạng thái không hợp lệ";
            };

            return new XacThucSerialResponse(
                    false,
                    statusMessage,
                    serial.getId(),
                    serial.getSerialNo(),
                    serial.getCtsp() != null && serial.getCtsp().getSanPham() != null
                            ? serial.getCtsp().getSanPham().getTenSanPham()
                            : null,
                    serial.getCtsp() != null ? serial.getCtsp().getMaCtsp() : null);
        }

        // 5. Kiểm tra serial đã được bán chưa (trong bảng serial_da_ban)
        boolean daBan = serialDaBanRepository.existsBySerialId(serial.getId());
        if (daBan) {
            return new XacThucSerialResponse(
                    false,
                    "Serial đã được sử dụng trong đơn hàng khác",
                    serial.getId(),
                    serial.getSerialNo(),
                    serial.getCtsp() != null && serial.getCtsp().getSanPham() != null
                            ? serial.getCtsp().getSanPham().getTenSanPham()
                            : null,
                    serial.getCtsp() != null ? serial.getCtsp().getMaCtsp() : null);
        }

        // 6. Serial hợp lệ
        return new XacThucSerialResponse(
                true,
                "Serial hợp lệ",
                serial.getId(),
                serial.getSerialNo(),
                serial.getCtsp() != null && serial.getCtsp().getSanPham() != null
                        ? serial.getCtsp().getSanPham().getTenSanPham()
                        : null,
                serial.getCtsp() != null ? serial.getCtsp().getMaCtsp() : null);
    }

    /**
     * Kiểm tra và cập nhật giá sản phẩm trước khi thanh toán
     * Endpoint: POST /api/v1/ban-hang/hoa-don/{idHoaDon}/kiem-tra-cap-nhat-gia
     * 
     * Kiểm tra giá sản phẩm có thay đổi không, nếu có thì tự động cập nhật
     * Trả về thông tin về sự thay đổi để frontend hiển thị cho người dùng
     */
    @Transactional
    public CapNhatGiaResponse kiemTraVaCapNhatGia(UUID idHoaDon) {
        System.out.println("🔍 [ThanhToanService] Kiểm tra và cập nhật giá sản phẩm trước khi thanh toán...");

        // 1. Tìm hóa đơn
        HoaDon hoaDon = hoaDonService.findById(idHoaDon);

        // 2. Kiểm tra trạng thái
        if (hoaDon.getTrangThai() != TrangThaiHoaDon.CHO_THANH_TOAN) {
            throw new ApiException("Chỉ có thể kiểm tra giá cho hóa đơn đang chờ thanh toán", "BAD_REQUEST");
        }

        if (hoaDon.getHoaDonChiTiets() == null || hoaDon.getHoaDonChiTiets().isEmpty()) {
            return new CapNhatGiaResponse(false, 0, new ArrayList<>(), new HoaDonResponse(hoaDon));
        }

        Instant now = Instant.now();
        boolean coThayDoi = false;
        List<CapNhatGiaResponse.ThongTinThayDoiGia> danhSachThayDoi = new ArrayList<>();

        // 3. Kiểm tra và cập nhật giá từng sản phẩm trong hóa đơn
        for (HoaDonChiTiet hdct : hoaDon.getHoaDonChiTiets()) {
            ChiTietSanPham ctsp = hdct.getChiTietSanPham();
            if (ctsp == null) {
                continue;
            }

            BigDecimal giaHienTaiTrongHoaDon = hdct.getDonGia();
            if (giaHienTaiTrongHoaDon == null) {
                continue;
            }

            // Lấy giá đúng hiện tại (từ dot_giam_gia_chi_tiet nếu có, hoặc giá gốc)
            BigDecimal giaDungHienTai = getGiaBanHienTai(ctsp, now);

            // So sánh giá và cập nhật nếu khác
            if (giaDungHienTai.compareTo(giaHienTaiTrongHoaDon) != 0) {
                String tenSanPham = ctsp.getSanPham() != null ? ctsp.getSanPham().getTenSanPham() : ctsp.getMaCtsp();
                String maCtsp = ctsp.getMaCtsp();

                System.out.println("⚠️ [ThanhToanService] Giá sản phẩm không khớp, tự động cập nhật:");
                System.out.println("  - Sản phẩm: " + tenSanPham);
                System.out.println("  - Giá cũ: " + formatCurrency(giaHienTaiTrongHoaDon));
                System.out.println("  - Giá mới: " + formatCurrency(giaDungHienTai));

                // Tự động cập nhật giá
                hdct.setDonGia(giaDungHienTai);
                coThayDoi = true;

                // Thêm vào danh sách thay đổi
                danhSachThayDoi.add(new CapNhatGiaResponse.ThongTinThayDoiGia(
                        tenSanPham,
                        maCtsp,
                        giaHienTaiTrongHoaDon,
                        giaDungHienTai));
            }
        }

        // 4. Nếu có thay đổi giá, tính lại tổng tiền
        if (coThayDoi) {
            System.out.println("🔄 [ThanhToanService] Đã cập nhật giá, tính lại tổng tiền...");

            // Tính lại tổng tiền
            hoaDonService.capNhatTongTien(hoaDon);

            // Tính lại tổng tiền sau giảm (có xem xét voucher và điểm)
            BigDecimal tongTien = hoaDon.getTongTien() != null ? hoaDon.getTongTien() : BigDecimal.ZERO;
            BigDecimal tienDuocGiam = hoaDon.getTienDuocGiam() != null ? hoaDon.getTienDuocGiam() : BigDecimal.ZERO;
            BigDecimal soTienQuyDoi = hoaDon.getSoTienQuyDoi() != null ? hoaDon.getSoTienQuyDoi() : BigDecimal.ZERO;
            BigDecimal tongTienSauGiam = tongTien.subtract(tienDuocGiam).subtract(soTienQuyDoi);
            if (tongTienSauGiam.compareTo(BigDecimal.ZERO) < 0) {
                tongTienSauGiam = BigDecimal.ZERO;
            }
            hoaDon.setTongTienSauGiam(tongTienSauGiam);

            // Lưu lại hóa đơn với giá đã cập nhật
            hoaDonService.save(hoaDon);

            System.out.println("✅ [ThanhToanService] Đã cập nhật giá và tính lại tổng tiền");
        } else {
            System.out.println("✅ [ThanhToanService] Tất cả giá sản phẩm đều khớp");
        }

        // 5. Trả về response
        HoaDonResponse hoaDonResponse = new HoaDonResponse(hoaDonService.findById(idHoaDon));
        return new CapNhatGiaResponse(
                coThayDoi,
                danhSachThayDoi.size(),
                danhSachThayDoi,
                hoaDonResponse);
    }

    /**
     * Hoàn Tất Thanh Toán Hóa Đơn
     * Endpoint: POST /api/v1/ban-hang/hoa-don/{idHoaDon}/thanh-toan
     * 
     * YÊU CẦU QUAN TRỌNG: Phải có danh sách Serial Numbers đã được xác thực
     * 
     * Logic:
     * 1. Validate hóa đơn và Serial Numbers
     * 2. Xử lý từng Serial: cập nhật trạng thái, tạo SerialDaBan
     * 3. Cập nhật tồn kho chính thức (trừ soLuongTon, giải phóng soLuongTamGiu)
     * 4. Cập nhật trạng thái hóa đơn
     * 5. Ghi nhận chi tiết thanh toán
     */
    @Transactional
    public HoaDonResponse thanhToanHoaDon(UUID idHoaDon, ThanhToanRequest request) {
        // 1. Tìm hóa đơn
        HoaDon hoaDon = hoaDonService.findById(idHoaDon);

        // 2. Kiểm tra trạng thái
        if (hoaDon.getTrangThai() != TrangThaiHoaDon.CHO_THANH_TOAN) {
            throw new ApiException("Hóa đơn này không ở trạng thái chờ thanh toán", "BAD_REQUEST");
        }

        if (hoaDon.getHoaDonChiTiets() == null || hoaDon.getHoaDonChiTiets().isEmpty()) {
            throw new ApiException("Không thể thanh toán hóa đơn trống", "BAD_REQUEST");
        }

        // 2.1. Kiểm tra và validate lại voucher (nếu có) - QUAN TRỌNG: Tránh voucher bị
        // hết hạn/tắt/sửa
        if (hoaDon.getIdPhieuGiamGia() != null) {
            validateAndRecalculateVoucher(hoaDon);
        }

        // 2.2. Kiểm tra và validate lại điểm tích lũy (nếu có) - QUAN TRỌNG: Tránh điểm
        // bị thay đổi/hết
        if (hoaDon.getSoDiemSuDung() != null && hoaDon.getSoDiemSuDung() > 0) {
            validateAndRecalculatePoints(hoaDon);
        }

        // 2.3. Kiểm tra và validate lại giá sản phẩm - QUAN TRỌNG: Đảm bảo giá khớp với
        // đợt giảm giá hiện tại
        validateAndRecalculateProductPrices(hoaDon);

        // 3. Kiểm tra Serial Numbers (YÊU CẦU QUAN TRỌNG)
        if (request.getSerialNumbers() == null || request.getSerialNumbers().isEmpty()) {
            throw new ApiException("Phải quét/nhập Serial Number cho tất cả sản phẩm trước khi thanh toán",
                    "SERIAL_REQUIRED");
        }

        // 3.1. Tính tổng số lượng sản phẩm trong hóa đơn
        int tongSoLuongSanPham = hoaDon.getHoaDonChiTiets().stream()
                .mapToInt(HoaDonChiTiet::getSoLuong)
                .sum();

        // 3.2. Kiểm tra số lượng serial phải bằng tổng số lượng sản phẩm
        if (request.getSerialNumbers().size() != tongSoLuongSanPham) {
            throw new ApiException(
                    String.format("Số lượng serial (%d) không khớp với tổng số lượng sản phẩm (%d)",
                            request.getSerialNumbers().size(), tongSoLuongSanPham),
                    "SERIAL_COUNT_MISMATCH");
        }

        // 4. Xử lý Serial Numbers và cập nhật tồn kho
        // Map để theo dõi số lượng serial đã xử lý cho mỗi sản phẩm
        Map<UUID, Integer> serialCountByProduct = new HashMap<>();

        for (SerialThanhToanItem serialItem : request.getSerialNumbers()) {
            // 4.1. Tìm hóa đơn chi tiết tương ứng
            HoaDonChiTiet hdct = hoaDon.getHoaDonChiTiets().stream()
                    .filter(h -> h.getId().equals(serialItem.getIdHoaDonChiTiet()))
                    .findFirst()
                    .orElseThrow(() -> new ApiException(
                            "Không tìm thấy sản phẩm trong hóa đơn với ID: " + serialItem.getIdHoaDonChiTiet(),
                            "PRODUCT_NOT_FOUND_IN_INVOICE"));

            // 4.2. Tìm Serial
            Serial serial = serialRepository.findBySerialNoAndCtspId(
                    serialItem.getSerialNumber(),
                    serialItem.getIdChiTietSanPham())
                    .orElseThrow(() -> new ApiException(
                            "Serial không hợp lệ: " + serialItem.getSerialNumber(),
                            "INVALID_SERIAL"));

            // 4.3. Kiểm tra trạng thái Serial (phải là 1 = Chưa bán)
            if (serial.getTrangThai() != 1) {
                throw new ApiException(
                        "Serial " + serial.getSerialNo() + " không ở trạng thái có thể bán",
                        "SERIAL_UNAVAILABLE");
            }

            // 4.4. Kiểm tra Serial chưa được bán
            if (serialDaBanRepository.existsBySerialId(serial.getId())) {
                throw new ApiException(
                        "Serial " + serial.getSerialNo() + " đã được sử dụng trong đơn hàng khác",
                        "SERIAL_ALREADY_SOLD");
            }

            // 4.5. Cập nhật trạng thái Serial thành "Đã bán" (2)
            serial.setTrangThai(2);
            serialRepository.save(serial);

            // 4.6. Tạo bản ghi SerialDaBan
            SerialDaBan serialDaBan = new SerialDaBan();
            serialDaBan.setId(UUID.randomUUID());
            serialDaBan.setIdHoaDonChiTiet(hdct);
            serialDaBan.setIdSerial(serial);
            serialDaBan.setNgayTao(Instant.now());
            serialDaBanRepository.save(serialDaBan);

            // 4.7. Đếm số lượng serial đã xử lý cho sản phẩm này
            UUID ctspId = hdct.getChiTietSanPham().getId();
            serialCountByProduct.put(ctspId, serialCountByProduct.getOrDefault(ctspId, 0) + 1);
        }

        // 4.8. Đồng bộ tồn kho (Auto-Sync)
        // Sau khi đã bán (Serial 1 -> 2), tồn kho không đổi về mặt con số (vì Reserved
        // cũng không tính là Available)
        // Nhưng ta vẫn gọ sync để đảm bảo nhất quán dữ liệu.
        for (HoaDonChiTiet hdct : hoaDon.getHoaDonChiTiets()) {
            try {
                serialService.updateStockCount(hdct.getChiTietSanPham().getId());

                // Reset tạm giữ về 0 (để sạch data, dù logic auto-sync không dùng đến nữa)
                ChiTietSanPham ctsp = hdct.getChiTietSanPham();
                ctsp.setSoLuongTamGiu(0);
                chiTietSanPhamRepository.save(ctsp);

            } catch (Exception e) {
                System.err.println("⚠️ [ThanhToanService] Lỗi sync stock: " + e.getMessage());
            }

            System.out.println("✅ [ThanhToanService] Đã thanh toán và đồng bộ kho cho SP: "
                    + hdct.getChiTietSanPham().getMaCtsp());
        }

        // 5. Cập nhật trạng thái hóa đơn dựa trên canGiaoHang và isCOD
        boolean isCOD = Boolean.TRUE.equals(request.getCanGiaoHang()) && Boolean.TRUE.equals(request.getIsCOD());

        if (isCOD) {
            // COD: Thanh toán khi nhận hàng
            hoaDon.setTrangThai(TrangThaiHoaDon.DANG_GIAO);
            hoaDon.setTrangThaiThanhToan(0); // Chưa thanh toán
            hoaDon.setNgayThanhToan(null); // Chưa thanh toán nên không có ngày thanh toán
        } else {
            // Đã thanh toán (tại quầy hoặc không cần giao hàng)
            hoaDon.setTrangThaiThanhToan(1); // Đã thanh toán
            hoaDon.setNgayThanhToan(Instant.now());

            if (Boolean.TRUE.equals(request.getCanGiaoHang())) {
                // Đã thanh toán tại quầy + cần giao hàng → trạng thái: ĐANG_GIAO
                hoaDon.setTrangThai(TrangThaiHoaDon.DANG_GIAO);
            } else {
                // Không cần giao hàng → trạng thái: HOAN_THANH
                hoaDon.setTrangThai(TrangThaiHoaDon.HOAN_THANH);
            }
        }

        // 5.1. Cập nhật thông tin giao hàng (nếu có)
        if (Boolean.TRUE.equals(request.getCanGiaoHang())) {
            // Nếu có thông tin người nhận riêng, cập nhật vào hóa đơn
            if (request.getTenNguoiNhan() != null && !request.getTenNguoiNhan().trim().isEmpty()) {
                hoaDon.setTenKhachHang(request.getTenNguoiNhan());
            }
            if (request.getSdtNguoiNhan() != null && !request.getSdtNguoiNhan().trim().isEmpty()) {
                hoaDon.setSdt(request.getSdtNguoiNhan());
            }
            if (request.getDiaChiGiaoHang() != null && !request.getDiaChiGiaoHang().trim().isEmpty()) {
                hoaDon.setDiaChi(request.getDiaChiGiaoHang());
            }
            // Thêm ghi chú giao hàng vào ghi chú hóa đơn (nếu có)
            if (request.getGhiChuGiaoHang() != null && !request.getGhiChuGiaoHang().trim().isEmpty()) {
                String ghiChuHienTai = hoaDon.getGhiChu() != null ? hoaDon.getGhiChu() : "";
                String ghiChuMoi = ghiChuHienTai.isEmpty()
                        ? "Giao hàng: " + request.getGhiChuGiaoHang()
                        : ghiChuHienTai + "\nGiao hàng: " + request.getGhiChuGiaoHang();
                hoaDon.setGhiChu(ghiChuMoi);
            }

            // Thêm ghi chú COD nếu có
            if (isCOD) {
                String ghiChuHienTai = hoaDon.getGhiChu() != null ? hoaDon.getGhiChu() : "";
                String ghiChuCOD = ghiChuHienTai.isEmpty()
                        ? "COD: Thanh toán khi nhận hàng"
                        : ghiChuHienTai + "\nCOD: Thanh toán khi nhận hàng";
                hoaDon.setGhiChu(ghiChuCOD);
            }
        }

        // 6. Ghi nhận chi tiết thanh toán (chỉ khi đã thanh toán, không phải COD)
        if (!isCOD) {
            if (request.getIdPhuongThucThanhToan() == null) {
                throw new ApiException("Vui lòng chọn phương thức thanh toán", "MISSING_PAYMENT_METHOD");
            }
            PhuongThucThanhToan pttt = phuongThucThanhToanRepository.findById(request.getIdPhuongThucThanhToan())
                    .orElseThrow(() -> new ApiException(
                            "Không tìm thấy phương thức thanh toán với ID: " + request.getIdPhuongThucThanhToan(),
                            "NOT_FOUND"));

            ChiTietThanhToan cttt = new ChiTietThanhToan();
            cttt.setId(UUID.randomUUID());
            cttt.setIdHoaDon(hoaDon);
            cttt.setPhuongThucThanhToan(pttt);
            cttt.setSoTienThanhToan(request.getSoTienThanhToan());
            cttt.setTienKhachDua(request.getTienKhachDua()); // Số tiền khách đưa (cho thanh toán tiền mặt)
            cttt.setTienTraLai(request.getTienTraLai()); // Số tiền trả lại khách (cho thanh toán tiền mặt)
            cttt.setMaGiaoDich(request.getMaGiaoDich());
            cttt.setGhiChu(request.getGhiChu());
            chiTietThanhToanRepository.save(cttt);
        }

        // 7. Lưu hóa đơn
        hoaDonService.save(hoaDon);

        // 8. Trừ điểm nếu có sử dụng điểm (chỉ khi đã thanh toán, không phải COD)
        if (!isCOD && hoaDon.getSoDiemSuDung() != null && hoaDon.getSoDiemSuDung() > 0) {
            try {
                tichDiemService.truDiemKhiThanhToan(idHoaDon, hoaDon.getSoDiemSuDung());
            } catch (Exception e) {
                // Log error nhưng không rollback transaction vì đã thanh toán thành công
                System.err.println("⚠️ [ThanhToanService] Lỗi khi trừ điểm: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // 9. Tích điểm sau thanh toán thành công (chỉ khi đã thanh toán, không phải
        // COD)
        if (!isCOD && hoaDon.getIdKhachHang() != null) {
            try {
                tichDiemService.tichDiemSauThanhToan(idHoaDon);
            } catch (Exception e) {
                // Log error nhưng không rollback transaction vì đã thanh toán thành công
                System.err.println("⚠️ [ThanhToanService] Lỗi khi tích điểm: " + e.getMessage());
                e.printStackTrace();
            }
        }

        return new HoaDonResponse(hoaDonService.findById(idHoaDon));
    }

    /**
     * Xử lý thanh toán COD (Cash on Delivery) khi giao hàng thành công
     * 
     * @param idHoaDon     ID hóa đơn
     * @param tienKhachDua Số tiền khách đưa khi nhận hàng
     * @return HoaDonResponse
     */
    @Transactional
    public HoaDonResponse thanhToanCOD(UUID idHoaDon, BigDecimal tienKhachDua) {
        // 1. Tìm hóa đơn
        HoaDon hoaDon = hoaDonService.findById(idHoaDon);

        // 2. Kiểm tra trạng thái hóa đơn (phải là DANG_GIAO và chưa thanh toán)
        if (hoaDon.getTrangThai() != TrangThaiHoaDon.DANG_GIAO) {
            throw new ApiException("Hóa đơn không ở trạng thái đang giao hàng!", "INVALID_STATUS");
        }

        if (hoaDon.getTrangThaiThanhToan() != null && hoaDon.getTrangThaiThanhToan() == 1) {
            throw new ApiException("Hóa đơn đã được thanh toán rồi!", "ALREADY_PAID");
        }

        // 3. Validate số tiền khách đưa
        BigDecimal tongTien = hoaDon.getTongTienSauGiam() != null ? hoaDon.getTongTienSauGiam() : hoaDon.getTongTien();
        if (tienKhachDua == null || tienKhachDua.compareTo(tongTien) < 0) {
            throw new ApiException("Số tiền khách đưa không đủ! Cần: " + tongTien, "INSUFFICIENT_AMOUNT");
        }

        // 4. Tính tiền trả lại
        BigDecimal tienTraLai = tienKhachDua.subtract(tongTien);

        // 5. Tìm phương thức thanh toán "Tiền mặt"
        PhuongThucThanhToan pttt = phuongThucThanhToanRepository.findAll().stream()
                .filter(pt -> "Tien mat".equalsIgnoreCase(pt.getTenPhuongThuc())
                        || "Cash".equalsIgnoreCase(pt.getLoaiPhuongThuc()))
                .findFirst()
                .orElseThrow(() -> new ApiException("Không tìm thấy phương thức thanh toán Tiền mặt!", "NOT_FOUND"));

        // 6. Tạo chi tiết thanh toán
        ChiTietThanhToan cttt = new ChiTietThanhToan();
        cttt.setId(UUID.randomUUID());
        cttt.setIdHoaDon(hoaDon);
        cttt.setPhuongThucThanhToan(pttt);
        cttt.setSoTienThanhToan(tongTien);
        cttt.setTienKhachDua(tienKhachDua);
        cttt.setTienTraLai(tienTraLai);
        cttt.setMaGiaoDich(null); // COD không có mã giao dịch
        cttt.setGhiChu("Thanh toán COD khi giao hàng");
        chiTietThanhToanRepository.save(cttt);

        // 7. Cập nhật trạng thái hóa đơn
        hoaDon.setTrangThaiThanhToan(1); // Đã thanh toán
        hoaDon.setTrangThai(TrangThaiHoaDon.HOAN_THANH); // Hoàn thành
        hoaDon.setNgayThanhToan(Instant.now());

        // 8. Lưu hóa đơn
        hoaDonService.save(hoaDon);

        // 9. Trừ điểm nếu có sử dụng điểm
        if (hoaDon.getSoDiemSuDung() != null && hoaDon.getSoDiemSuDung() > 0) {
            try {
                tichDiemService.truDiemKhiThanhToan(idHoaDon, hoaDon.getSoDiemSuDung());
            } catch (Exception e) {
                System.err.println("⚠️ [ThanhToanService] Lỗi khi trừ điểm (COD): " + e.getMessage());
                e.printStackTrace();
            }
        }

        // 10. Tích điểm sau thanh toán COD thành công
        if (hoaDon.getIdKhachHang() != null) {
            try {
                tichDiemService.tichDiemSauThanhToan(idHoaDon);
            } catch (Exception e) {
                System.err.println("⚠️ [ThanhToanService] Lỗi khi tích điểm (COD): " + e.getMessage());
                e.printStackTrace();
            }
        }

        return new HoaDonResponse(hoaDonService.findById(idHoaDon));
    }

    /**
     * Helper method: Đảm bảo version field của ChiTietSanPham không null
     */
    // Removed ensureVersionNotNull as it's no longer used

    /**
     * Validate và tính lại voucher trước khi thanh toán
     * Đảm bảo voucher vẫn còn hợp lệ (chưa hết hạn, chưa bị tắt, chưa bị sửa)
     * 
     * Nếu voucher không hợp lệ, sẽ throw ApiException
     * Nếu voucher hợp lệ, sẽ tính lại số tiền giảm và cập nhật vào hóa đơn
     */
    private void validateAndRecalculateVoucher(HoaDon hoaDon) {
        System.out.println("🔍 [ThanhToanService] Kiểm tra lại voucher trước khi thanh toán...");

        PhieuGiamGia voucher = hoaDon.getIdPhieuGiamGia();
        if (voucher == null) {
            return;
        }

        // 1. Load lại voucher từ DB để đảm bảo có dữ liệu mới nhất
        if (voucher.getId() == null) {
            return;
        }
        PhieuGiamGia voucherFromDb = phieuGiamGiaRepository.findById(voucher.getId())
                .orElseThrow(() -> new ApiException(
                        "Voucher không còn tồn tại trong hệ thống. Vui lòng xóa voucher và thử lại.",
                        "VOUCHER_NOT_FOUND"));

        Instant now = Instant.now();
        BigDecimal tongTien = hoaDon.getTongTien() != null ? hoaDon.getTongTien() : BigDecimal.ZERO;
        UUID idKhachHang = hoaDon.getIdKhachHang() != null ? hoaDon.getIdKhachHang().getId() : null;

        System.out.println("  - Voucher: " + voucherFromDb.getMa() + " - " + voucherFromDb.getTenPhieuGiamGia());
        System.out.println("  - Tổng tiền hóa đơn: " + tongTien);
        System.out.println("  - ID khách hàng: " + idKhachHang);

        // 2. Kiểm tra trạng thái (phải = 1 = Hoạt động)
        if (voucherFromDb.getTrangThai() == null || voucherFromDb.getTrangThai() != 1) {
            throw new ApiException(
                    "Voucher đã bị tắt. Vui lòng xóa voucher khỏi hóa đơn và thử lại.",
                    "VOUCHER_INACTIVE");
        }

        // 3. Kiểm tra ngày hiệu lực
        if (voucherFromDb.getNgayBatDau() != null && voucherFromDb.getNgayBatDau().isAfter(now)) {
            throw new ApiException(
                    "Voucher chưa đến thời gian hiệu lực. Vui lòng xóa voucher và thử lại.",
                    "VOUCHER_NOT_STARTED");
        }
        if (voucherFromDb.getNgayKetThuc() != null && voucherFromDb.getNgayKetThuc().isBefore(now)) {
            throw new ApiException(
                    "Voucher đã hết hạn. Vui lòng xóa voucher khỏi hóa đơn và thử lại.",
                    "VOUCHER_EXPIRED");
        }

        // 4. Kiểm tra số lượng còn lại (chỉ với voucher công khai)
        if (!Boolean.TRUE.equals(voucherFromDb.getRiengTu())) {
            if (voucherFromDb.getSoLuongDung() != null && voucherFromDb.getSoLuongDung() < 0) {
                throw new ApiException(
                        "Voucher đã hết lượt sử dụng. Vui lòng xóa voucher và thử lại.",
                        "VOUCHER_OUT_OF_STOCK");
            }
        }

        // 5. Kiểm tra điều kiện hóa đơn tối thiểu
        if (voucherFromDb.getHoaDonToiThieu() != null && tongTien.compareTo(voucherFromDb.getHoaDonToiThieu()) < 0) {
            throw new ApiException(
                    String.format(
                            "Hóa đơn không đủ điều kiện để sử dụng voucher. Tối thiểu: %s. Vui lòng xóa voucher và thử lại.",
                            formatCurrency(voucherFromDb.getHoaDonToiThieu())),
                    "INSUFFICIENT_ORDER_VALUE");
        }

        // 6. Kiểm tra voucher riêng tư
        if (Boolean.TRUE.equals(voucherFromDb.getRiengTu())) {
            if (idKhachHang == null) {
                throw new ApiException(
                        "Voucher này chỉ dành cho khách hàng thành viên. Vui lòng xóa voucher và thử lại.",
                        "VOUCHER_PRIVATE");
            }
            // Kiểm tra khách hàng có quyền sử dụng voucher này không
            boolean coQuyen = phieuGiamGiaKhachHangRepository.existsByPhieuGiamGia_IdAndKhachHang_Id(
                    voucherFromDb.getId(), idKhachHang);
            if (!coQuyen) {
                throw new ApiException(
                        "Bạn không còn quyền sử dụng voucher này. Vui lòng xóa voucher và thử lại.",
                        "VOUCHER_NO_PERMISSION");
            }
        }

        // 7. Tính lại số tiền giảm (đảm bảo chính xác với giá trị voucher hiện tại)
        BigDecimal tienDuocGiam = calculateTienGiam(voucherFromDb, tongTien);

        System.out.println("  - Số tiền giảm (tính lại): " + tienDuocGiam);

        // 8. Cập nhật lại hóa đơn với số tiền giảm mới
        hoaDon.setTienDuocGiam(tienDuocGiam);

        // Tính lại tổng tiền sau giảm
        BigDecimal tongTienSauGiam = tongTien.subtract(tienDuocGiam);
        if (tongTienSauGiam.compareTo(BigDecimal.ZERO) < 0) {
            tongTienSauGiam = BigDecimal.ZERO; // Không được âm
        }
        hoaDon.setTongTienSauGiam(tongTienSauGiam);

        // 9. Cập nhật lại voucher trong hóa đơn (đảm bảo reference đúng)
        hoaDon.setIdPhieuGiamGia(voucherFromDb);

        System.out.println("✅ [ThanhToanService] Voucher hợp lệ, đã tính lại số tiền giảm");
    }

    /**
     * Tính toán số tiền giảm dựa trên voucher và tổng tiền hóa đơn
     * (Logic giống với KhuyenMaiService)
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
     * Validate và tính lại điểm tích lũy trước khi thanh toán
     * Đảm bảo điểm vẫn còn hợp lệ (khách hàng có đủ điểm, tỷ lệ quy đổi còn hợp lệ)
     * 
     * Nếu điểm không hợp lệ, sẽ throw ApiException
     * Nếu điểm hợp lệ, sẽ tính lại số tiền quy đổi và cập nhật vào hóa đơn
     */
    private void validateAndRecalculatePoints(HoaDon hoaDon) {
        System.out.println("🔍 [ThanhToanService] Kiểm tra lại điểm tích lũy trước khi thanh toán...");

        Integer soDiemSuDung = hoaDon.getSoDiemSuDung();
        if (soDiemSuDung == null || soDiemSuDung <= 0) {
            return;
        }

        // 1. Kiểm tra có khách hàng không (điểm chỉ dành cho khách hàng thành viên)
        if (hoaDon.getIdKhachHang() == null) {
            throw new ApiException(
                    "Không thể sử dụng điểm tích lũy cho khách lẻ. Vui lòng xóa điểm và thử lại.",
                    "POINTS_REQUIRE_CUSTOMER");
        }

        UUID khachHangId = hoaDon.getIdKhachHang().getId();

        // 2. Lấy thông tin điểm tích lũy của khách hàng từ DB
        TichDiem tichDiem = tichDiemRepository.findByUser_Id(khachHangId)
                .orElseThrow(() -> new ApiException(
                        "Khách hàng chưa có tài khoản điểm tích lũy. Vui lòng xóa điểm và thử lại.",
                        "POINTS_ACCOUNT_NOT_FOUND"));

        System.out.println("  - Khách hàng: " + khachHangId);
        System.out.println("  - Điểm muốn sử dụng: " + soDiemSuDung);
        System.out.println("  - Tổng điểm hiện có: " + (tichDiem.getTongDiem() != null ? tichDiem.getTongDiem() : 0));

        // 3. Kiểm tra khách hàng có đủ điểm không
        Integer tongDiem = tichDiem.getTongDiem() != null ? tichDiem.getTongDiem() : 0;
        if (tongDiem < soDiemSuDung) {
            throw new ApiException(
                    String.format(
                            "Khách hàng không đủ điểm tích lũy. Hiện có: %d điểm, cần: %d điểm. Vui lòng xóa điểm và thử lại.",
                            tongDiem, soDiemSuDung),
                    "INSUFFICIENT_POINTS");
        }

        // 4. Lấy tỷ lệ quy đổi điểm hiện tại (từ bảng quy_doi_diem với trangThai = 1)
        QuyDoiDiem quyDoiDiem = quyDoiDiemRepository.findFirstByTrangThaiOrderByIdAsc(1);
        if (quyDoiDiem == null) {
            throw new ApiException(
                    "Hệ thống quy đổi điểm đang tạm dừng. Vui lòng xóa điểm và thử lại.",
                    "POINTS_CONVERSION_UNAVAILABLE");
        }

        BigDecimal tienTieuDiem = quyDoiDiem.getTienTieuDiem();
        if (tienTieuDiem == null || tienTieuDiem.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiException(
                    "Tỷ lệ quy đổi điểm không hợp lệ. Vui lòng xóa điểm và thử lại.",
                    "INVALID_POINTS_CONVERSION_RATE");
        }

        System.out.println("  - Tỷ lệ quy đổi: " + tienTieuDiem + " VND/điểm");

        // 5. Tính lại số tiền quy đổi (đảm bảo chính xác với tỷ lệ hiện tại)
        BigDecimal soTienQuyDoi = BigDecimal.valueOf(soDiemSuDung).multiply(tienTieuDiem);

        System.out.println("  - Số tiền quy đổi (tính lại): " + soTienQuyDoi);

        // 6. Kiểm tra số tiền quy đổi không được vượt quá tổng tiền sau giảm (nếu có
        // voucher)
        BigDecimal tongTienSauGiam = hoaDon.getTongTienSauGiam() != null ? hoaDon.getTongTienSauGiam()
                : hoaDon.getTongTien();
        if (tongTienSauGiam == null) {
            tongTienSauGiam = BigDecimal.ZERO;
        }

        if (soTienQuyDoi.compareTo(tongTienSauGiam) > 0) {
            // Nếu số tiền quy đổi lớn hơn tổng tiền, chỉ được quy đổi bằng tổng tiền
            soTienQuyDoi = tongTienSauGiam;
            // Tính lại số điểm tương ứng
            int diemToiDa = soTienQuyDoi.divide(tienTieuDiem, 0, java.math.RoundingMode.DOWN).intValue();
            if (diemToiDa < soDiemSuDung) {
                throw new ApiException(
                        String.format(
                                "Số điểm sử dụng (%d điểm) vượt quá tổng tiền hóa đơn. Tối đa có thể dùng: %d điểm. Vui lòng điều chỉnh và thử lại.",
                                soDiemSuDung, diemToiDa),
                        "POINTS_EXCEED_ORDER_VALUE");
            }
        }

        // 7. Cập nhật lại hóa đơn với số tiền quy đổi mới
        hoaDon.setSoTienQuyDoi(soTienQuyDoi);

        // Tính lại tổng tiền sau giảm (trừ cả voucher và điểm)
        BigDecimal tongTien = hoaDon.getTongTien() != null ? hoaDon.getTongTien() : BigDecimal.ZERO;
        BigDecimal tienDuocGiam = hoaDon.getTienDuocGiam() != null ? hoaDon.getTienDuocGiam() : BigDecimal.ZERO;
        BigDecimal tongTienSauGiamMoi = tongTien.subtract(tienDuocGiam).subtract(soTienQuyDoi);
        if (tongTienSauGiamMoi.compareTo(BigDecimal.ZERO) < 0) {
            tongTienSauGiamMoi = BigDecimal.ZERO; // Không được âm
        }
        hoaDon.setTongTienSauGiam(tongTienSauGiamMoi);

        System.out.println("✅ [ThanhToanService] Điểm tích lũy hợp lệ, đã tính lại số tiền quy đổi");
    }

    /**
     * Validate và tự động cập nhật giá sản phẩm realtime trước khi thanh toán
     * Đảm bảo giá trong hóa đơn luôn khớp với giá trong đợt giảm giá hiện tại (nếu
     * có)
     * 
     * Nếu giá không khớp, sẽ tự động cập nhật giá và tính lại tổng tiền
     * Không throw exception, tiếp tục thanh toán bình thường với giá đã cập nhật
     */
    private void validateAndRecalculateProductPrices(HoaDon hoaDon) {
        System.out.println("🔍 [ThanhToanService] Kiểm tra và cập nhật giá sản phẩm realtime trước khi thanh toán...");

        if (hoaDon.getHoaDonChiTiets() == null || hoaDon.getHoaDonChiTiets().isEmpty()) {
            return;
        }

        Instant now = Instant.now();
        boolean coThayDoi = false;

        // Kiểm tra và cập nhật giá từng sản phẩm trong hóa đơn
        for (HoaDonChiTiet hdct : hoaDon.getHoaDonChiTiets()) {
            ChiTietSanPham ctsp = hdct.getChiTietSanPham();
            if (ctsp == null) {
                continue;
            }

            BigDecimal giaHienTaiTrongHoaDon = hdct.getDonGia();
            if (giaHienTaiTrongHoaDon == null) {
                continue;
            }

            // Lấy giá đúng hiện tại (từ dot_giam_gia_chi_tiet nếu có, hoặc giá gốc)
            BigDecimal giaDungHienTai = getGiaBanHienTai(ctsp, now);

            // So sánh giá và cập nhật nếu khác
            if (giaDungHienTai.compareTo(giaHienTaiTrongHoaDon) != 0) {
                String tenSanPham = ctsp.getSanPham() != null ? ctsp.getSanPham().getTenSanPham() : ctsp.getMaCtsp();

                System.out.println("⚠️ [ThanhToanService] Giá sản phẩm không khớp, tự động cập nhật realtime:");
                System.out.println("  - Sản phẩm: " + tenSanPham);
                System.out.println("  - Giá cũ: " + formatCurrency(giaHienTaiTrongHoaDon));
                System.out.println("  - Giá mới: " + formatCurrency(giaDungHienTai));

                // Tự động cập nhật giá realtime
                hdct.setDonGia(giaDungHienTai);
                coThayDoi = true;
            }
        }

        // Nếu có thay đổi giá, tính lại tổng tiền
        if (coThayDoi) {
            System.out.println("🔄 [ThanhToanService] Đã cập nhật giá realtime, tính lại tổng tiền...");

            // Tính lại tổng tiền
            hoaDonService.capNhatTongTien(hoaDon);

            // Tính lại tổng tiền sau giảm (có xem xét voucher và điểm)
            BigDecimal tongTien = hoaDon.getTongTien() != null ? hoaDon.getTongTien() : BigDecimal.ZERO;
            BigDecimal tienDuocGiam = hoaDon.getTienDuocGiam() != null ? hoaDon.getTienDuocGiam() : BigDecimal.ZERO;
            BigDecimal soTienQuyDoi = hoaDon.getSoTienQuyDoi() != null ? hoaDon.getSoTienQuyDoi() : BigDecimal.ZERO;
            BigDecimal tongTienSauGiam = tongTien.subtract(tienDuocGiam).subtract(soTienQuyDoi);
            if (tongTienSauGiam.compareTo(BigDecimal.ZERO) < 0) {
                tongTienSauGiam = BigDecimal.ZERO;
            }
            hoaDon.setTongTienSauGiam(tongTienSauGiam);

            // Lưu lại hóa đơn với giá đã cập nhật
            hoaDonService.save(hoaDon);

            System.out.println(
                    "✅ [ThanhToanService] Đã cập nhật giá realtime và tính lại tổng tiền, tiếp tục thanh toán");
        } else {
            System.out.println("✅ [ThanhToanService] Tất cả giá sản phẩm đều khớp");
        }
    }

    /**
     * Lấy giá bán hiện tại của sản phẩm
     * Ưu tiên lấy giá từ dot_giam_gia_chi_tiet (nếu có và đang hiệu lực)
     * Nếu không có, lấy giá gốc từ chi_tiet_san_pham
     * 
     * Logic giống với SanPhamTrongHoaDonService.getGiaBanHienTai()
     */
    private BigDecimal getGiaBanHienTai(ChiTietSanPham ctsp, Instant now) {
        // Tìm thông tin giảm giá cho chi tiết sản phẩm này
        List<DotGiamGiaChiTiet> discountList = dotGiamGiaChiTietRepository.findAll();
        Optional<DotGiamGiaChiTiet> dotGiamGiaChiTiet = discountList.stream()
                .filter(d -> d.getIdCtsp() != null && d.getIdCtsp().getId().equals(ctsp.getId()))
                .filter(d -> d.getDotGiamGia() != null && d.getDotGiamGia().getTrangThai() == 1)
                .filter(d -> {
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
                return giaSauKhiGiam;
            }
        }

        // Nếu không có giảm giá hoặc giá giảm không hợp lệ, trả về giá gốc
        return ctsp.getGiaBan() != null ? ctsp.getGiaBan() : BigDecimal.ZERO;
    }

    /**
     * Helper: Format currency (để hiển thị trong error message)
     */
    private String formatCurrency(BigDecimal amount) {
        if (amount == null)
            return "0";
        return amount.toString();
    }

    /**
     * Kiểm tra toàn bộ (giá, voucher, điểm) trước khi xác nhận thanh toán
     * Endpoint: POST /api/v1/ban-hang/hoa-don/{idHoaDon}/kiem-tra-truoc-thanh-toan
     * 
     * Nếu có thay đổi, tự động cập nhật hóa đơn và trả về thông tin thay đổi
     * Frontend sẽ hiển thị thông báo và yêu cầu người dùng xác nhận lại
     */
    @Transactional
    public KiemTraTruocThanhToanResponse kiemTraTruocThanhToan(UUID idHoaDon) {
        System.out.println("🔍 [ThanhToanService] Kiểm tra toàn bộ trước khi xác nhận thanh toán...");

        // 1. Tìm hóa đơn
        HoaDon hoaDon = hoaDonService.findById(idHoaDon);

        // 2. Kiểm tra trạng thái
        if (hoaDon.getTrangThai() != TrangThaiHoaDon.CHO_THANH_TOAN) {
            throw new ApiException("Chỉ có thể kiểm tra cho hóa đơn đang chờ thanh toán", "BAD_REQUEST");
        }

        boolean coThayDoi = false;
        List<String> thongBaoList = new ArrayList<>();

        // 3. Kiểm tra và cập nhật giá sản phẩm
        KiemTraTruocThanhToanResponse.ThayDoiGia thayDoiGia = kiemTraVaCapNhatGiaInternal(hoaDon);
        if (thayDoiGia.isCoThayDoi()) {
            coThayDoi = true;
            thongBaoList.add(String.format("Giá của %d sản phẩm đã thay đổi", thayDoiGia.getSoSanPhamThayDoi()));
        }

        // 4. Kiểm tra và cập nhật voucher
        KiemTraTruocThanhToanResponse.ThayDoiVoucher thayDoiVoucher = kiemTraVaCapNhatVoucherInternal(hoaDon);
        if (thayDoiVoucher.isCoThayDoi()) {
            coThayDoi = true;
            if (thayDoiVoucher.isBiXoa()) {
                thongBaoList.add("Voucher đã bị xóa: " + thayDoiVoucher.getLyDo());
            } else {
                thongBaoList.add("Voucher đã được cập nhật: " + thayDoiVoucher.getLyDo());
            }
        }

        // 5. Kiểm tra và cập nhật điểm tích lũy
        KiemTraTruocThanhToanResponse.ThayDoiDiem thayDoiDiem = kiemTraVaCapNhatDiemInternal(hoaDon);
        if (thayDoiDiem.isCoThayDoi()) {
            coThayDoi = true;
            if (thayDoiDiem.isBiXoa()) {
                thongBaoList.add("Điểm tích lũy đã bị xóa: " + thayDoiDiem.getLyDo());
            } else {
                thongBaoList.add("Điểm tích lũy đã được cập nhật: " + thayDoiDiem.getLyDo());
            }
        }

        // 6. Nếu có thay đổi, tính lại tổng tiền và lưu hóa đơn
        if (coThayDoi) {
            hoaDonService.capNhatTongTien(hoaDon);
            BigDecimal tongTien = hoaDon.getTongTien() != null ? hoaDon.getTongTien() : BigDecimal.ZERO;
            BigDecimal tienDuocGiam = hoaDon.getTienDuocGiam() != null ? hoaDon.getTienDuocGiam() : BigDecimal.ZERO;
            BigDecimal soTienQuyDoi = hoaDon.getSoTienQuyDoi() != null ? hoaDon.getSoTienQuyDoi() : BigDecimal.ZERO;
            BigDecimal tongTienSauGiam = tongTien.subtract(tienDuocGiam).subtract(soTienQuyDoi);
            if (tongTienSauGiam.compareTo(BigDecimal.ZERO) < 0) {
                tongTienSauGiam = BigDecimal.ZERO;
            }
            hoaDon.setTongTienSauGiam(tongTienSauGiam);
            hoaDonService.save(hoaDon);
        }

        // 7. Tạo message tổng hợp
        String message = coThayDoi
                ? String.join(". ", thongBaoList) + ". Vui lòng kiểm tra lại và xác nhận thanh toán."
                : "Không có thay đổi. Có thể tiếp tục thanh toán.";

        // 8. Trả về response
        HoaDonResponse hoaDonResponse = new HoaDonResponse(hoaDonService.findById(idHoaDon));
        return new KiemTraTruocThanhToanResponse(
                coThayDoi,
                message,
                thayDoiGia,
                thayDoiVoucher,
                thayDoiDiem,
                hoaDonResponse);
    }

    /**
     * Helper: Kiểm tra và cập nhật giá (internal, không throw exception)
     */
    private KiemTraTruocThanhToanResponse.ThayDoiGia kiemTraVaCapNhatGiaInternal(HoaDon hoaDon) {
        if (hoaDon.getHoaDonChiTiets() == null || hoaDon.getHoaDonChiTiets().isEmpty()) {
            return new KiemTraTruocThanhToanResponse.ThayDoiGia(false, 0, new ArrayList<>());
        }

        Instant now = Instant.now();
        boolean coThayDoi = false;
        List<CapNhatGiaResponse.ThongTinThayDoiGia> danhSachThayDoi = new ArrayList<>();

        for (HoaDonChiTiet hdct : hoaDon.getHoaDonChiTiets()) {
            ChiTietSanPham ctsp = hdct.getChiTietSanPham();
            if (ctsp == null)
                continue;

            BigDecimal giaHienTaiTrongHoaDon = hdct.getDonGia();
            if (giaHienTaiTrongHoaDon == null)
                continue;

            BigDecimal giaDungHienTai = getGiaBanHienTai(ctsp, now);

            if (giaDungHienTai.compareTo(giaHienTaiTrongHoaDon) != 0) {
                String tenSanPham = ctsp.getSanPham() != null ? ctsp.getSanPham().getTenSanPham() : ctsp.getMaCtsp();
                String maCtsp = ctsp.getMaCtsp();
                hdct.setDonGia(giaDungHienTai);
                coThayDoi = true;
                danhSachThayDoi.add(new CapNhatGiaResponse.ThongTinThayDoiGia(
                        tenSanPham, maCtsp, giaHienTaiTrongHoaDon, giaDungHienTai));
            }
        }

        return new KiemTraTruocThanhToanResponse.ThayDoiGia(
                coThayDoi,
                danhSachThayDoi.size(),
                danhSachThayDoi);
    }

    /**
     * Helper: Kiểm tra và cập nhật voucher (internal, không throw exception, tự
     * động xóa nếu không hợp lệ)
     */
    private KiemTraTruocThanhToanResponse.ThayDoiVoucher kiemTraVaCapNhatVoucherInternal(HoaDon hoaDon) {
        PhieuGiamGia voucher = hoaDon.getIdPhieuGiamGia();
        if (voucher == null) {
            return new KiemTraTruocThanhToanResponse.ThayDoiVoucher(false, false, null, null, null);
        }

        BigDecimal tienGiamCu = hoaDon.getTienDuocGiam() != null ? hoaDon.getTienDuocGiam() : BigDecimal.ZERO;

        try {
            // Load lại voucher từ DB
            if (voucher.getId() == null) {
                return new KiemTraTruocThanhToanResponse.ThayDoiVoucher(false, false, null, null, null);
            }
            PhieuGiamGia voucherFromDb = phieuGiamGiaRepository.findById(voucher.getId())
                    .orElse(null);

            if (voucherFromDb == null) {
                // Voucher không còn tồn tại, xóa khỏi hóa đơn
                hoaDon.setIdPhieuGiamGia(null);
                hoaDon.setTienDuocGiam(BigDecimal.ZERO);
                return new KiemTraTruocThanhToanResponse.ThayDoiVoucher(
                        true, true, "Voucher không còn tồn tại trong hệ thống", tienGiamCu, BigDecimal.ZERO);
            }

            Instant now = Instant.now();
            BigDecimal tongTien = hoaDon.getTongTien() != null ? hoaDon.getTongTien() : BigDecimal.ZERO;
            UUID idKhachHang = hoaDon.getIdKhachHang() != null ? hoaDon.getIdKhachHang().getId() : null;

            // Kiểm tra các điều kiện
            if (voucherFromDb.getTrangThai() == null || voucherFromDb.getTrangThai() != 1) {
                restoreVoucherQuantity(voucherFromDb);
                hoaDon.setIdPhieuGiamGia(null);
                hoaDon.setTienDuocGiam(BigDecimal.ZERO);
                return new KiemTraTruocThanhToanResponse.ThayDoiVoucher(
                        true, true, "Voucher đã bị tắt", tienGiamCu, BigDecimal.ZERO);
            }

            if (voucherFromDb.getNgayBatDau() != null && voucherFromDb.getNgayBatDau().isAfter(now)) {
                restoreVoucherQuantity(voucherFromDb);
                hoaDon.setIdPhieuGiamGia(null);
                hoaDon.setTienDuocGiam(BigDecimal.ZERO);
                return new KiemTraTruocThanhToanResponse.ThayDoiVoucher(
                        true, true, "Voucher chưa đến thời gian hiệu lực", tienGiamCu, BigDecimal.ZERO);
            }

            if (voucherFromDb.getNgayKetThuc() != null && voucherFromDb.getNgayKetThuc().isBefore(now)) {
                restoreVoucherQuantity(voucherFromDb);
                hoaDon.setIdPhieuGiamGia(null);
                hoaDon.setTienDuocGiam(BigDecimal.ZERO);
                return new KiemTraTruocThanhToanResponse.ThayDoiVoucher(
                        true, true, "Voucher đã hết hạn", tienGiamCu, BigDecimal.ZERO);
            }

            // Kiểm tra số lượng còn lại (chỉ với voucher công khai)
            if (!Boolean.TRUE.equals(voucherFromDb.getRiengTu())) {
                if (voucherFromDb.getSoLuongDung() != null && voucherFromDb.getSoLuongDung() < 0) {
                    restoreVoucherQuantity(voucherFromDb);
                    hoaDon.setIdPhieuGiamGia(null);
                    hoaDon.setTienDuocGiam(BigDecimal.ZERO);
                    return new KiemTraTruocThanhToanResponse.ThayDoiVoucher(
                            true, true, "Voucher đã hết lượt sử dụng", tienGiamCu, BigDecimal.ZERO);
                }
            }

            if (voucherFromDb.getHoaDonToiThieu() != null
                    && tongTien.compareTo(voucherFromDb.getHoaDonToiThieu()) < 0) {
                restoreVoucherQuantity(voucherFromDb);
                hoaDon.setIdPhieuGiamGia(null);
                hoaDon.setTienDuocGiam(BigDecimal.ZERO);
                return new KiemTraTruocThanhToanResponse.ThayDoiVoucher(
                        true, true,
                        String.format("Hóa đơn không đủ điều kiện (tối thiểu: %s)",
                                formatCurrency(voucherFromDb.getHoaDonToiThieu())),
                        tienGiamCu, BigDecimal.ZERO);
            }

            if (Boolean.TRUE.equals(voucherFromDb.getRiengTu())) {
                if (idKhachHang == null) {
                    restoreVoucherQuantity(voucherFromDb);
                    hoaDon.setIdPhieuGiamGia(null);
                    hoaDon.setTienDuocGiam(BigDecimal.ZERO);
                    return new KiemTraTruocThanhToanResponse.ThayDoiVoucher(
                            true, true, "Voucher chỉ dành cho khách hàng thành viên", tienGiamCu, BigDecimal.ZERO);
                }
                boolean coQuyen = phieuGiamGiaKhachHangRepository.existsByPhieuGiamGia_IdAndKhachHang_Id(
                        voucherFromDb.getId(), idKhachHang);
                if (!coQuyen) {
                    restoreVoucherQuantity(voucherFromDb);
                    hoaDon.setIdPhieuGiamGia(null);
                    hoaDon.setTienDuocGiam(BigDecimal.ZERO);
                    return new KiemTraTruocThanhToanResponse.ThayDoiVoucher(
                            true, true, "Bạn không còn quyền sử dụng voucher này", tienGiamCu, BigDecimal.ZERO);
                }
            }

            // Voucher hợp lệ, tính lại số tiền giảm
            BigDecimal tienGiamMoi = calculateTienGiam(voucherFromDb, tongTien);
            if (tienGiamCu.compareTo(tienGiamMoi) != 0) {
                hoaDon.setIdPhieuGiamGia(voucherFromDb);
                hoaDon.setTienDuocGiam(tienGiamMoi);
                return new KiemTraTruocThanhToanResponse.ThayDoiVoucher(
                        true, false, "Số tiền giảm đã được cập nhật", tienGiamCu, tienGiamMoi);
            }

            // Không có thay đổi
            return new KiemTraTruocThanhToanResponse.ThayDoiVoucher(false, false, null, tienGiamCu, tienGiamCu);

        } catch (Exception e) {
            // Nếu có lỗi, xóa voucher
            hoaDon.setIdPhieuGiamGia(null);
            hoaDon.setTienDuocGiam(BigDecimal.ZERO);
            return new KiemTraTruocThanhToanResponse.ThayDoiVoucher(
                    true, true, "Lỗi khi kiểm tra voucher: " + e.getMessage(), tienGiamCu, BigDecimal.ZERO);
        }
    }

    /**
     * Helper: Kiểm tra và cập nhật điểm tích lũy (internal, không throw exception,
     * tự động xóa nếu không hợp lệ)
     */
    private KiemTraTruocThanhToanResponse.ThayDoiDiem kiemTraVaCapNhatDiemInternal(HoaDon hoaDon) {
        Integer soDiemSuDung = hoaDon.getSoDiemSuDung();
        if (soDiemSuDung == null || soDiemSuDung <= 0) {
            return new KiemTraTruocThanhToanResponse.ThayDoiDiem(false, false, null, null, null, null, null);
        }

        Integer soDiemCu = soDiemSuDung;
        BigDecimal soTienQuyDoiCu = hoaDon.getSoTienQuyDoi() != null ? hoaDon.getSoTienQuyDoi() : BigDecimal.ZERO;

        try {
            // Kiểm tra có khách hàng không
            if (hoaDon.getIdKhachHang() == null) {
                hoaDon.setSoDiemSuDung(null);
                hoaDon.setSoTienQuyDoi(BigDecimal.ZERO);
                return new KiemTraTruocThanhToanResponse.ThayDoiDiem(
                        true, true, "Không thể sử dụng điểm tích lũy cho khách lẻ",
                        soDiemCu, null, soTienQuyDoiCu, BigDecimal.ZERO);
            }

            UUID khachHangId = hoaDon.getIdKhachHang().getId();

            // Lấy thông tin điểm tích lũy
            TichDiem tichDiem = tichDiemRepository.findByUser_Id(khachHangId).orElse(null);
            if (tichDiem == null) {
                hoaDon.setSoDiemSuDung(null);
                hoaDon.setSoTienQuyDoi(BigDecimal.ZERO);
                return new KiemTraTruocThanhToanResponse.ThayDoiDiem(
                        true, true, "Khách hàng chưa có tài khoản điểm tích lũy",
                        soDiemCu, null, soTienQuyDoiCu, BigDecimal.ZERO);
            }

            Integer tongDiem = tichDiem.getTongDiem() != null ? tichDiem.getTongDiem() : 0;
            if (tongDiem < soDiemSuDung) {
                hoaDon.setSoDiemSuDung(null);
                hoaDon.setSoTienQuyDoi(BigDecimal.ZERO);
                return new KiemTraTruocThanhToanResponse.ThayDoiDiem(
                        true, true,
                        String.format("Không đủ điểm tích lũy (hiện có: %d điểm, cần: %d điểm)", tongDiem,
                                soDiemSuDung),
                        soDiemCu, null, soTienQuyDoiCu, BigDecimal.ZERO);
            }

            // Lấy tỷ lệ quy đổi
            QuyDoiDiem quyDoiDiem = quyDoiDiemRepository.findFirstByTrangThaiOrderByIdAsc(1);
            if (quyDoiDiem == null || quyDoiDiem.getTienTieuDiem() == null ||
                    quyDoiDiem.getTienTieuDiem().compareTo(BigDecimal.ZERO) <= 0) {
                hoaDon.setSoDiemSuDung(null);
                hoaDon.setSoTienQuyDoi(BigDecimal.ZERO);
                return new KiemTraTruocThanhToanResponse.ThayDoiDiem(
                        true, true, "Hệ thống quy đổi điểm đang tạm dừng hoặc không hợp lệ",
                        soDiemCu, null, soTienQuyDoiCu, BigDecimal.ZERO);
            }

            BigDecimal tienTieuDiem = quyDoiDiem.getTienTieuDiem();
            BigDecimal soTienQuyDoiMoi = BigDecimal.valueOf(soDiemSuDung).multiply(tienTieuDiem);

            // Kiểm tra số tiền quy đổi không vượt quá tổng tiền
            BigDecimal tongTienSauGiam = hoaDon.getTongTienSauGiam() != null ? hoaDon.getTongTienSauGiam()
                    : hoaDon.getTongTien();
            if (tongTienSauGiam == null)
                tongTienSauGiam = BigDecimal.ZERO;

            if (soTienQuyDoiMoi.compareTo(tongTienSauGiam) > 0) {
                // Tính lại số điểm tối đa có thể dùng
                int diemToiDa = tongTienSauGiam.divide(tienTieuDiem, 0, java.math.RoundingMode.DOWN).intValue();
                if (diemToiDa < soDiemSuDung) {
                    hoaDon.setSoDiemSuDung(null);
                    hoaDon.setSoTienQuyDoi(BigDecimal.ZERO);
                    return new KiemTraTruocThanhToanResponse.ThayDoiDiem(
                            true, true,
                            String.format("Số điểm sử dụng (%d điểm) vượt quá tổng tiền hóa đơn. Tối đa: %d điểm",
                                    soDiemSuDung, diemToiDa),
                            soDiemCu, null, soTienQuyDoiCu, BigDecimal.ZERO);
                }
                soTienQuyDoiMoi = tongTienSauGiam;
            }

            // So sánh với giá trị cũ
            if (soTienQuyDoiCu.compareTo(soTienQuyDoiMoi) != 0) {
                hoaDon.setSoTienQuyDoi(soTienQuyDoiMoi);
                return new KiemTraTruocThanhToanResponse.ThayDoiDiem(
                        true, false, "Số tiền quy đổi đã được cập nhật",
                        soDiemCu, soDiemCu, soTienQuyDoiCu, soTienQuyDoiMoi);
            }

            // Không có thay đổi
            return new KiemTraTruocThanhToanResponse.ThayDoiDiem(
                    false, false, null, soDiemCu, soDiemCu, soTienQuyDoiCu, soTienQuyDoiCu);

        } catch (Exception e) {
            // Nếu có lỗi, xóa điểm
            hoaDon.setSoDiemSuDung(null);
            hoaDon.setSoTienQuyDoi(BigDecimal.ZERO);
            return new KiemTraTruocThanhToanResponse.ThayDoiDiem(
                    true, true, "Lỗi khi kiểm tra điểm: " + e.getMessage(),
                    soDiemCu, null, soTienQuyDoiCu, BigDecimal.ZERO);
        }
    }

    /**
     * Restore voucher quantity (increment soLuongDung)
     * Skips private vouchers (riengTu = true)
     */
    private void restoreVoucherQuantity(PhieuGiamGia voucher) {
        if (voucher != null && voucher.getSoLuongDung() != null) {
            // Check if private voucher
            if (Boolean.TRUE.equals(voucher.getRiengTu())) {
                System.out.println(
                        "ℹ️ [ThanhToanService] Private voucher, skipping quantity restore: " + voucher.getMa());
                return;
            }

            voucher.setSoLuongDung(voucher.getSoLuongDung() + 1);
            phieuGiamGiaRepository.save(voucher);
            System.out.println("✅ [ThanhToanService] Restored voucher quantity: " + voucher.getMa());
        }
    }
}
