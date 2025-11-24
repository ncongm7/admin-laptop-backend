package com.example.backendlaptop.service.banhang;

import com.example.backendlaptop.dto.banhang.HoaDonResponse;
import com.example.backendlaptop.dto.banhang.SerialThanhToanItem;
import com.example.backendlaptop.dto.banhang.ThanhToanRequest;
import com.example.backendlaptop.dto.banhang.XacThucSerialRequest;
import com.example.backendlaptop.dto.banhang.XacThucSerialResponse;
import com.example.backendlaptop.entity.ChiTietSanPham;
import com.example.backendlaptop.entity.ChiTietThanhToan;
import com.example.backendlaptop.entity.HoaDon;
import com.example.backendlaptop.entity.HoaDonChiTiet;
import com.example.backendlaptop.entity.PhuongThucThanhToan;
import com.example.backendlaptop.entity.Serial;
import com.example.backendlaptop.entity.SerialDaBan;
import com.example.backendlaptop.expection.ApiException;
import com.example.backendlaptop.model.TrangThaiHoaDon;
import com.example.backendlaptop.repository.ChiTietSanPhamRepository;
import com.example.backendlaptop.repository.ChiTietThanhToanRepository;
import com.example.backendlaptop.repository.PhuongThucThanhToanRepository;
import com.example.backendlaptop.repository.SerialDaBanRepository;
import com.example.backendlaptop.repository.SerialRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Service xử lý thanh toán hóa đơn
 * Nhiệm vụ: 
 * - Xác thực Serial Numbers
 * - Xử lý thanh toán cuối cùng (cập nhật tồn kho, Serial, trạng thái hóa đơn)
 * Đây là nghiệp vụ quan trọng và phức tạp nhất, phải được bọc trong @Transactional
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
                null
            );
        }
        
        // 3. Tìm serial theo serialNumber và ctspId
        Optional<Serial> serialOpt = serialRepository.findBySerialNoAndCtspId(
            request.getSerialNumber(),
            request.getIdChiTietSanPham()
        );
        
        if (serialOpt.isEmpty()) {
            return new XacThucSerialResponse(
                false,
                "Serial không tồn tại hoặc không thuộc sản phẩm này",
                null,
                request.getSerialNumber(),
                null,
                null
            );
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
                serial.getCtsp() != null ? serial.getCtsp().getMaCtsp() : null
            );
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
                serial.getCtsp() != null ? serial.getCtsp().getMaCtsp() : null
            );
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
            serial.getCtsp() != null ? serial.getCtsp().getMaCtsp() : null
        );
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

        // 3. Kiểm tra Serial Numbers (YÊU CẦU QUAN TRỌNG)
        if (request.getSerialNumbers() == null || request.getSerialNumbers().isEmpty()) {
            throw new ApiException("Phải quét/nhập Serial Number cho tất cả sản phẩm trước khi thanh toán", "SERIAL_REQUIRED");
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
                "SERIAL_COUNT_MISMATCH"
            );
        }

        // 4. Xử lý Serial Numbers và cập nhật tồn kho
        for (SerialThanhToanItem serialItem : request.getSerialNumbers()) {
            // 4.1. Tìm hóa đơn chi tiết tương ứng
            HoaDonChiTiet hdct = hoaDon.getHoaDonChiTiets().stream()
                    .filter(h -> h.getId().equals(serialItem.getIdHoaDonChiTiet()))
                    .findFirst()
                    .orElseThrow(() -> new ApiException(
                        "Không tìm thấy sản phẩm trong hóa đơn với ID: " + serialItem.getIdHoaDonChiTiet(),
                        "PRODUCT_NOT_FOUND_IN_INVOICE"
                    ));

            // 4.2. Tìm Serial
            Serial serial = serialRepository.findBySerialNoAndCtspId(
                    serialItem.getSerialNumber(),
                    serialItem.getIdChiTietSanPham()
                )
                .orElseThrow(() -> new ApiException(
                    "Serial không hợp lệ: " + serialItem.getSerialNumber(),
                    "INVALID_SERIAL"
                ));

            // 4.3. Kiểm tra trạng thái Serial (phải là 1 = Chưa bán)
            if (serial.getTrangThai() != 1) {
                throw new ApiException(
                    "Serial " + serial.getSerialNo() + " không ở trạng thái có thể bán",
                    "SERIAL_UNAVAILABLE"
                );
            }

            // 4.4. Kiểm tra Serial chưa được bán
            if (serialDaBanRepository.existsBySerialId(serial.getId())) {
                throw new ApiException(
                    "Serial " + serial.getSerialNo() + " đã được sử dụng trong đơn hàng khác",
                    "SERIAL_ALREADY_SOLD"
                );
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

            // 4.7. Cập nhật tồn kho (trừ 1 cho mỗi serial)
            ChiTietSanPham ctsp = hdct.getChiTietSanPham();
            int soLuongTon = ctsp.getSoLuongTon() != null ? ctsp.getSoLuongTon() : 0;
            int soLuongTamGiu = ctsp.getSoLuongTamGiu() != null ? ctsp.getSoLuongTamGiu() : 0;

            ctsp.setSoLuongTon(soLuongTon - 1);
            ctsp.setSoLuongTamGiu(Math.max(0, soLuongTamGiu - 1));
            
            // Fix: Đảm bảo version field không null
            ensureVersionNotNull(ctsp);
            
            chiTietSanPhamRepository.save(ctsp);
        }

        // 5. Cập nhật trạng thái hóa đơn
        hoaDon.setTrangThai(TrangThaiHoaDon.DA_THANH_TOAN);
        hoaDon.setTrangThaiThanhToan(1); // 1: Đã thanh toán
        hoaDon.setNgayThanhToan(Instant.now());
        
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
        }

        // 6. Ghi nhận chi tiết thanh toán
        PhuongThucThanhToan pttt = phuongThucThanhToanRepository.findById(request.getIdPhuongThucThanhToan())
                .orElseThrow(() -> new ApiException("Không tìm thấy phương thức thanh toán với ID: " + request.getIdPhuongThucThanhToan(), "NOT_FOUND"));

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

        // 7. Lưu hóa đơn
        hoaDonService.save(hoaDon);
        
        return new HoaDonResponse(hoaDonService.findById(idHoaDon));
    }

    /**
     * Helper method: Đảm bảo version field của ChiTietSanPham không null
     */
    private void ensureVersionNotNull(ChiTietSanPham ctsp) {
        if (ctsp.getVersion() == null) {
            ctsp.setVersion(0L);
            System.out.println("⚠️ [ThanhToanService] Warning: ChiTietSanPham version was null, initialized to 0 for: " + ctsp.getMaCtsp());
        }
    }
}

