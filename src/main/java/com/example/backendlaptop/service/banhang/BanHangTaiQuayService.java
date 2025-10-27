package com.example.backendlaptop.service.banhang;

import com.example.backendlaptop.dto.banhang.HoaDonResponse;
import com.example.backendlaptop.dto.banhang.TaoHoaDonRequest;
import com.example.backendlaptop.dto.banhang.ThanhToanRequest;
import com.example.backendlaptop.dto.banhang.ThemSanPhamRequest;
import com.example.backendlaptop.entity.*;
import com.example.backendlaptop.expection.ApiException;
import com.example.backendlaptop.model.TrangThaiHoaDon;
import com.example.backendlaptop.repository.*;
import com.example.backendlaptop.repository.banhang.HoaDonChiTietRepository;
import com.example.backendlaptop.repository.banhang.HoaDonRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

@Service
public class BanHangTaiQuayService {

    @Autowired
    private HoaDonRepository hoaDonRepository;

    @Autowired
    private HoaDonChiTietRepository hoaDonChiTietRepository;

    @Autowired
    private ChiTietSanPhamRepository chiTietSanPhamRepository;

    @Autowired
    private NhanVienRepository nhanVienRepository;

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Autowired
    private PhuongThucThanhToanRepository phuongThucThanhToanRepository;

    @Autowired
    private ChiTietThanhToanRepository chiTietThanhToanRepository;

    /**
     * API 1: Tạo Hóa Đơn Chờ Mới
     * Endpoint: POST /api/v1/ban-hang/hoa-don/tao-moi
     */
    @Transactional
    public HoaDonResponse taoHoaDonChoiMoi(TaoHoaDonRequest request) {
        HoaDon hoaDon = new HoaDon();
        
        // Sinh mã hóa đơn tự động
        hoaDon.setMa("HD" + System.currentTimeMillis());
        hoaDon.setNgayTao(Instant.now());
        hoaDon.setLoaiHoaDon(0); // 0: Tại quầy
        hoaDon.setTrangThai(TrangThaiHoaDon.CHO_THANH_TOAN);
        hoaDon.setTongTien(BigDecimal.ZERO);
        hoaDon.setTienDuocGiam(BigDecimal.ZERO);
        hoaDon.setTongTienSauGiam(BigDecimal.ZERO);
        hoaDon.setTrangThaiThanhToan(0); // 0: Chưa thanh toán
        hoaDon.setHoaDonChiTiets(new HashSet<>());

        // Gán nhân viên (lấy từ authentication context hoặc request)
        if (request.getNhanVienId() != null) {
            NhanVien nhanVien = nhanVienRepository.findById(request.getNhanVienId())
                    .orElseThrow(() -> new ApiException("Không tìm thấy nhân viên với ID: " + request.getNhanVienId(), "NOT_FOUND"));
            hoaDon.setIdNhanVien(nhanVien);
        }

        // Gán khách hàng (có thể null nếu là khách vãng lai)
        if (request.getKhachHangId() != null) {
            KhachHang khachHang = khachHangRepository.findById(request.getKhachHangId())
                    .orElseThrow(() -> new ApiException("Không tìm thấy khách hàng với ID: " + request.getKhachHangId(), "NOT_FOUND"));
            hoaDon.setIdKhachHang(khachHang);
            hoaDon.setTenKhachHang(khachHang.getHoTen());
            hoaDon.setSdt(khachHang.getSoDienThoai());
        } else {
            hoaDon.setTenKhachHang("Khách lẻ");
        }

        HoaDon saved = hoaDonRepository.save(hoaDon);
        return new HoaDonResponse(saved);
    }

    /**
     * API 2: Thêm Sản Phẩm Vào Hóa Đơn Chờ
     * Endpoint: POST /api/v1/ban-hang/hoa-don/{idHoaDon}/them-san-pham
     */
    @Transactional
    public HoaDonResponse themSanPhamVaoHoaDon(UUID idHoaDon, ThemSanPhamRequest request) {
        // 1. Tìm hóa đơn
        HoaDon hoaDon = hoaDonRepository.findById(idHoaDon)
                .orElseThrow(() -> new ApiException("Không tìm thấy hóa đơn với ID: " + idHoaDon, "NOT_FOUND"));

        // 2. Kiểm tra trạng thái hóa đơn
        if (hoaDon.getTrangThai() != TrangThaiHoaDon.CHO_THANH_TOAN) {
            throw new ApiException("Chỉ có thể thêm sản phẩm vào hóa đơn đang chờ thanh toán", "BAD_REQUEST");
        }

        // 3. Tìm chi tiết sản phẩm
        ChiTietSanPham ctsp = chiTietSanPhamRepository.findById(request.getChiTietSanPhamId())
                .orElseThrow(() -> new ApiException("Không tìm thấy chi tiết sản phẩm với ID: " + request.getChiTietSanPhamId(), "NOT_FOUND"));

        // 4. Kiểm tra tồn kho
        int soLuongTon = ctsp.getSoLuongTon() != null ? ctsp.getSoLuongTon() : 0;
        int soLuongTamGiu = ctsp.getSoLuongTamGiu() != null ? ctsp.getSoLuongTamGiu() : 0;
        int soLuongKhaDung = soLuongTon - soLuongTamGiu;

        if (request.getSoLuong() > soLuongKhaDung) {
            throw new ApiException("Không đủ hàng. Số lượng khả dụng: " + soLuongKhaDung, "INSUFFICIENT_STOCK");
        }

        // 5. Tạm giữ tồn kho
        ctsp.setSoLuongTamGiu(soLuongTamGiu + request.getSoLuong());
        chiTietSanPhamRepository.save(ctsp);

        // 6. Kiểm tra sản phẩm đã có trong hóa đơn chưa
        Optional<HoaDonChiTiet> existingHdct = hoaDon.getHoaDonChiTiets().stream()
                .filter(hdct -> hdct.getChiTietSanPham().getId().equals(ctsp.getId()))
                .findFirst();

        if (existingHdct.isPresent()) {
            // Cập nhật số lượng
            HoaDonChiTiet hdct = existingHdct.get();
            hdct.setSoLuong(hdct.getSoLuong() + request.getSoLuong());
            hoaDonChiTietRepository.save(hdct);
        } else {
            // Tạo mới
            HoaDonChiTiet hdct = new HoaDonChiTiet();
            hdct.setHoaDon(hoaDon);
            hdct.setChiTietSanPham(ctsp);
            hdct.setSoLuong(request.getSoLuong());
            hdct.setDonGia(ctsp.getGiaBan());
            hoaDon.getHoaDonChiTiets().add(hdct);
            hoaDonChiTietRepository.save(hdct);
        }

        // 7. Tính lại tổng tiền
        BigDecimal tongTien = tinhLaiTongTien(hoaDon);
        hoaDon.setTongTien(tongTien);
        hoaDon.setTongTienSauGiam(tongTien.subtract(hoaDon.getTienDuocGiam() != null ? hoaDon.getTienDuocGiam() : BigDecimal.ZERO));

        // 8. Lưu hóa đơn
        HoaDon saved = hoaDonRepository.save(hoaDon);
        return new HoaDonResponse(saved);
    }

    /**
     * API 3: Xóa Sản Phẩm Khỏi Hóa Đơn Chờ
     * Endpoint: DELETE /api/v1/ban-hang/hoa-don/xoa-san-pham/{idHoaDonChiTiet}
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
        ctsp.setSoLuongTamGiu(soLuongTamGiu - soLuong);
        chiTietSanPhamRepository.save(ctsp);

        // 5. Xóa chi tiết hóa đơn
        hoaDon.getHoaDonChiTiets().remove(hdct);
        hoaDonChiTietRepository.delete(hdct);

        // 6. Tính lại tổng tiền
        BigDecimal tongTien = tinhLaiTongTien(hoaDon);
        hoaDon.setTongTien(tongTien);
        hoaDon.setTongTienSauGiam(tongTien.subtract(hoaDon.getTienDuocGiam() != null ? hoaDon.getTienDuocGiam() : BigDecimal.ZERO));

        // 7. Lưu hóa đơn
        HoaDon saved = hoaDonRepository.save(hoaDon);
        return new HoaDonResponse(saved);
    }

    /**
     * API 4: Hoàn Tất Thanh Toán Hóa Đơn
     * Endpoint: POST /api/v1/ban-hang/hoa-don/{idHoaDon}/thanh-toan
     */
    @Transactional
    public HoaDonResponse thanhToanHoaDon(UUID idHoaDon, ThanhToanRequest request) {
        // 1. Tìm hóa đơn
        HoaDon hoaDon = hoaDonRepository.findById(idHoaDon)
                .orElseThrow(() -> new ApiException("Không tìm thấy hóa đơn với ID: " + idHoaDon, "NOT_FOUND"));

        // 2. Kiểm tra trạng thái
        if (hoaDon.getTrangThai() != TrangThaiHoaDon.CHO_THANH_TOAN) {
            throw new ApiException("Hóa đơn này không ở trạng thái chờ thanh toán", "BAD_REQUEST");
        }

        if (hoaDon.getHoaDonChiTiets() == null || hoaDon.getHoaDonChiTiets().isEmpty()) {
            throw new ApiException("Không thể thanh toán hóa đơn trống", "BAD_REQUEST");
        }

        // 3. Cập nhật tồn kho chính thức
        for (HoaDonChiTiet hdct : hoaDon.getHoaDonChiTiets()) {
            ChiTietSanPham ctsp = hdct.getChiTietSanPham();
            int soLuongTon = ctsp.getSoLuongTon() != null ? ctsp.getSoLuongTon() : 0;
            int soLuongTamGiu = ctsp.getSoLuongTamGiu() != null ? ctsp.getSoLuongTamGiu() : 0;
            int soLuongMua = hdct.getSoLuong();

            // Trừ tồn kho và giải phóng tạm giữ
            ctsp.setSoLuongTon(soLuongTon - soLuongMua);
            ctsp.setSoLuongTamGiu(soLuongTamGiu - soLuongMua);
            chiTietSanPhamRepository.save(ctsp);
        }

        // 4. Cập nhật trạng thái hóa đơn
        hoaDon.setTrangThai(TrangThaiHoaDon.DA_THANH_TOAN);
        hoaDon.setTrangThaiThanhToan(1); // 1: Đã thanh toán
        hoaDon.setNgayThanhToan(Instant.now());

        // 5. Ghi nhận chi tiết thanh toán
        PhuongThucThanhToan pttt = phuongThucThanhToanRepository.findById(request.getIdPhuongThucThanhToan())
                .orElseThrow(() -> new ApiException("Không tìm thấy phương thức thanh toán với ID: " + request.getIdPhuongThucThanhToan(), "NOT_FOUND"));

        ChiTietThanhToan cttt = new ChiTietThanhToan();
        cttt.setId(UUID.randomUUID());
        cttt.setIdHoaDon(hoaDon);
        cttt.setPhuongThucThanhToan(pttt);
        cttt.setSoTienThanhToan(request.getSoTienThanhToan());
        cttt.setMaGiaoDich(request.getMaGiaoDich());
        cttt.setGhiChu(request.getGhiChu());
        chiTietThanhToanRepository.save(cttt);

        // 6. Lưu hóa đơn
        HoaDon saved = hoaDonRepository.save(hoaDon);
        return new HoaDonResponse(saved);
    }

    /**
     * Tính lại tổng tiền của hóa đơn
     */
    private BigDecimal tinhLaiTongTien(HoaDon hoaDon) {
        if (hoaDon.getHoaDonChiTiets() == null || hoaDon.getHoaDonChiTiets().isEmpty()) {
            return BigDecimal.ZERO;
        }
        return hoaDon.getHoaDonChiTiets().stream()
                .map(hdct -> hdct.getDonGia().multiply(new BigDecimal(hdct.getSoLuong())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}