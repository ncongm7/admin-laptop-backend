package com.example.backendlaptop.service.banhang;

import com.example.backendlaptop.dto.banhang.CapNhatKhachHangRequest;
import com.example.backendlaptop.dto.banhang.HoaDonResponse;
import com.example.backendlaptop.dto.banhang.TaoHoaDonRequest;
import com.example.backendlaptop.entity.HoaDon;
import com.example.backendlaptop.entity.KhachHang;
import com.example.backendlaptop.entity.NhanVien;
import com.example.backendlaptop.expection.ApiException;
import com.example.backendlaptop.model.TrangThaiHoaDon;
import com.example.backendlaptop.repository.KhachHangRepository;
import com.example.backendlaptop.repository.NhanVienRepository;
import com.example.backendlaptop.repository.banhang.HoaDonRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service quản lý vòng đời của Hóa đơn trong nghiệp vụ Bán hàng tại quầy
 * Nhiệm vụ: Tạo, cập nhật, xóa, lấy danh sách hóa đơn
 */
@Service
public class BanHangHoaDonService {

    @Autowired
    private HoaDonRepository hoaDonRepository;

    @Autowired
    private NhanVienRepository nhanVienRepository;

    @Autowired
    private KhachHangRepository khachHangRepository;

    /**
     * Tạo hóa đơn chờ mới
     */
    @Transactional
    public HoaDonResponse taoHoaDonChoMoi(TaoHoaDonRequest request) {
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
     * Xóa hóa đơn chờ
     * Lưu ý: Service này không giải phóng tồn kho tạm giữ
     * (Logic giải phóng tồn kho sẽ được xử lý bởi SanPhamTrongHoaDonService)
     */
    @Transactional
    public void xoaHoaDonCho(UUID idHoaDon) {
        HoaDon hoaDon = hoaDonRepository.findById(idHoaDon)
                .orElseThrow(() -> new ApiException("Không tìm thấy hóa đơn với ID: " + idHoaDon, "NOT_FOUND"));

        // Kiểm tra trạng thái (chỉ xóa được hóa đơn chờ)
        if (hoaDon.getTrangThai() != TrangThaiHoaDon.CHO_THANH_TOAN) {
            throw new ApiException("Chỉ có thể xóa hóa đơn đang chờ thanh toán", "INVALID_STATUS");
        }

        // Xóa hóa đơn (cascade sẽ tự động xóa chi tiết)
        hoaDonRepository.delete(hoaDon);
    }

    /**
     * Lấy danh sách hóa đơn chờ
     */
    public List<HoaDonResponse> getDanhSachHoaDonCho() {
        List<HoaDon> danhSachHoaDon = hoaDonRepository.findByTrangThai(TrangThaiHoaDon.CHO_THANH_TOAN);
        
        return danhSachHoaDon.stream()
                .map(HoaDonResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * Tìm hóa đơn theo ID
     */
    public HoaDon findById(UUID idHoaDon) {
        return hoaDonRepository.findById(idHoaDon)
                .orElseThrow(() -> new ApiException("Không tìm thấy hóa đơn với ID: " + idHoaDon, "NOT_FOUND"));
    }

    /**
     * Cập nhật khách hàng cho hóa đơn (thêm ID khách hàng hoặc set null để khách lẻ)
     */
    @Transactional
    public HoaDonResponse capNhatKhachHang(UUID idHoaDon, CapNhatKhachHangRequest request) {
        HoaDon hoaDon = findById(idHoaDon);

        if (request.getKhachHangId() != null) {
            KhachHang khachHang = khachHangRepository.findById(request.getKhachHangId())
                    .orElseThrow(() -> new ApiException("Không tìm thấy khách hàng với ID: " + request.getKhachHangId(), "NOT_FOUND"));
            hoaDon.setIdKhachHang(khachHang);
            hoaDon.setTenKhachHang(khachHang.getHoTen());
            hoaDon.setSdt(khachHang.getSoDienThoai());
        } else {
            // Nếu null => Khách lẻ
            hoaDon.setIdKhachHang(null);
            hoaDon.setTenKhachHang("Khách lẻ");
            hoaDon.setSdt(null);
        }

        HoaDon saved = hoaDonRepository.save(hoaDon);
        return new HoaDonResponse(saved);
    }

    /**
     * Lưu hóa đơn
     */
    @Transactional
    public HoaDon save(HoaDon hoaDon) {
        return hoaDonRepository.save(hoaDon);
    }

    /**
     * Tính lại tổng tiền của hóa đơn
     */
    public BigDecimal tinhLaiTongTien(HoaDon hoaDon) {
        if (hoaDon.getHoaDonChiTiets() == null || hoaDon.getHoaDonChiTiets().isEmpty()) {
            return BigDecimal.ZERO;
        }
        return hoaDon.getHoaDonChiTiets().stream()
                .filter(hdct -> hdct.getDonGia() != null) // Bỏ qua các item không có giá
                .map(hdct -> {
                    BigDecimal donGia = hdct.getDonGia() != null ? hdct.getDonGia() : BigDecimal.ZERO;
                    int soLuong = hdct.getSoLuong() != null ? hdct.getSoLuong() : 0;
                    return donGia.multiply(new BigDecimal(soLuong));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Cập nhật tổng tiền và tổng tiền sau giảm cho hóa đơn
     */
    @Transactional
    public void capNhatTongTien(HoaDon hoaDon) {
        BigDecimal tongTien = tinhLaiTongTien(hoaDon);
        hoaDon.setTongTien(tongTien);
        
        BigDecimal tienDuocGiam = hoaDon.getTienDuocGiam() != null ? hoaDon.getTienDuocGiam() : BigDecimal.ZERO;
        BigDecimal tongTienSauGiam = tongTien.subtract(tienDuocGiam);
        if (tongTienSauGiam.compareTo(BigDecimal.ZERO) < 0) {
            tongTienSauGiam = BigDecimal.ZERO;
        }
        hoaDon.setTongTienSauGiam(tongTienSauGiam);
        
        save(hoaDon);
    }
}

