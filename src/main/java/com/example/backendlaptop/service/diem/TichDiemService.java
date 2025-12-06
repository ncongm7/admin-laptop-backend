package com.example.backendlaptop.service.diem;

import com.example.backendlaptop.entity.*;
import com.example.backendlaptop.model.LoaiDiem;
import com.example.backendlaptop.model.TrangThaiDiem;
import com.example.backendlaptop.model.response.diem.LichSuDiemResponse;
import com.example.backendlaptop.model.response.diem.TichDiemResponse;
import com.example.backendlaptop.model.request.diem.TichDiemRequest;
import com.example.backendlaptop.repository.TichDiemRepository;
import com.example.backendlaptop.repository.LichSuDiemRepository;
import com.example.backendlaptop.repository.ChiTietLichSuDiemRepository;
import com.example.backendlaptop.repository.QuyDoiDiemRepository;
import com.example.backendlaptop.repository.KhachHangRepository;
import com.example.backendlaptop.repository.banhang.HoaDonRepository;
import com.example.backendlaptop.expection.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TichDiemService {
    
    @Autowired
    private TichDiemRepository tichDiemRepository;
    
    @Autowired
    private LichSuDiemRepository lichSuDiemRepository;
    
    @Autowired
    private ChiTietLichSuDiemRepository chiTietLichSuDiemRepository;
    
    @Autowired
    private QuyDoiDiemRepository quyDoiDiemRepository;
    
    @Autowired
    private HoaDonRepository hoaDonRepository;
    
    @Autowired
    private KhachHangRepository khachHangRepository;
    
    /**
     * Lấy thông tin ví điểm của khách hàng
     * Tự động tạo ví điểm nếu chưa có
     */
    public TichDiemResponse getTichDiemByUserId(UUID userId) {
        // Tự động tạo ví điểm nếu chưa có
        TichDiem tichDiem = tichDiemRepository.findByUser_Id(userId)
            .orElseGet(() -> taoViDiemChoKhachHang(userId));
        
        KhachHang khachHang = tichDiem.getUser();
        
        TichDiemResponse response = new TichDiemResponse();
        response.setId(tichDiem.getId());
        response.setUserId(userId);
        response.setTenKhachHang(khachHang != null ? khachHang.getHoTen() : null);
        response.setSoDienThoai(khachHang != null ? khachHang.getSoDienThoai() : null);
        response.setEmail(khachHang != null ? khachHang.getEmail() : null);
        response.setDiemDaDung(tichDiem.getDiemDaDung() != null ? tichDiem.getDiemDaDung() : 0);
        response.setDiemDaCong(tichDiem.getDiemDaCong() != null ? tichDiem.getDiemDaCong() : 0);
        response.setTongDiem(tichDiem.getTongDiem() != null ? tichDiem.getTongDiem() : 0);
        response.setTrangThai(tichDiem.getTrangThai() != null ? tichDiem.getTrangThai() : 1);
        
        return response;
    }
    
    /**
     * Tạo ví điểm cho khách hàng mới
     */
    @Transactional
    public TichDiem taoViDiemChoKhachHang(UUID userId) {
        Optional<TichDiem> existing = tichDiemRepository.findByUser_Id(userId);
        if (existing.isPresent()) {
            return existing.get();
        }
        
        KhachHang khachHang = khachHangRepository.findById(userId)
            .orElseThrow(() -> new ApiException("Không tìm thấy khách hàng", "CUSTOMER_NOT_FOUND"));
        
        TichDiem tichDiem = new TichDiem();
        tichDiem.setId(UUID.randomUUID());
        tichDiem.setUser(khachHang);
        tichDiem.setDiemDaDung(0);
        tichDiem.setDiemDaCong(0);
        tichDiem.setTongDiem(0);
        tichDiem.setTrangThai(1);
        
        return tichDiemRepository.save(tichDiem);
    }
    
    /**
     * Tích điểm sau khi thanh toán thành công
     */
    @Transactional
    public void tichDiemSauThanhToan(UUID hoaDonId) {
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
            .orElseThrow(() -> new ApiException("Không tìm thấy hóa đơn", "INVOICE_NOT_FOUND"));
        
        // Chỉ tích điểm cho khách hàng thành viên
        if (hoaDon.getIdKhachHang() == null) {
            System.out.println("⚠️ [TichDiemService] Hóa đơn không có khách hàng, bỏ qua tích điểm");
            return;
        }
        
        UUID khachHangId = hoaDon.getIdKhachHang().getId();
        
        // Kiểm tra xem đã tích điểm cho hóa đơn này chưa
        List<LichSuDiem> existing = lichSuDiemRepository.findByHoaDonId(hoaDonId);
        boolean daTichDiem = existing.stream()
            .anyMatch(ls -> ls.getLoaiDiem() != null && ls.getLoaiDiem().equals(LoaiDiem.TICH_DIEM.getValue()));
        
        if (daTichDiem) {
            System.out.println("⚠️ [TichDiemService] Hóa đơn " + hoaDonId + " đã được tích điểm rồi, bỏ qua");
            return;
        }
        
        // Lấy hoặc tạo ví điểm
        TichDiem tichDiem = tichDiemRepository.findByUser_Id(khachHangId)
            .orElseGet(() -> taoViDiemChoKhachHang(khachHangId));
        
        // Lấy quy đổi điểm đang hoạt động
        QuyDoiDiem quyDoiDiem = quyDoiDiemRepository.findFirstByTrangThaiOrderByIdAsc(1);
        if (quyDoiDiem == null || quyDoiDiem.getTienTichDiem() == null) {
            System.out.println("⚠️ [TichDiemService] Không có quy đổi điểm đang hoạt động, bỏ qua tích điểm");
            return;
        }
        
        // Tính điểm được tích dựa trên tổng tiền sau giảm (không tính phần đã dùng điểm)
        BigDecimal tongTienSauGiam = hoaDon.getTongTienSauGiam() != null 
            ? hoaDon.getTongTienSauGiam() 
            : hoaDon.getTongTien();
        
        if (tongTienSauGiam == null || tongTienSauGiam.compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("⚠️ [TichDiemService] Tổng tiền hóa đơn <= 0, bỏ qua tích điểm");
            return;
        }
        
        BigDecimal tienTichDiem = quyDoiDiem.getTienTichDiem();
        if (tienTichDiem == null || tienTichDiem.compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("⚠️ [TichDiemService] Tỷ lệ tích điểm không hợp lệ, bỏ qua");
            return;
        }
        
        // Tính số điểm được tích: tổng tiền / tiền tích điểm (làm tròn xuống)
        Integer soDiemCong = tongTienSauGiam.divide(tienTichDiem, 0, java.math.RoundingMode.DOWN).intValue();
        
        if (soDiemCong <= 0) {
            System.out.println("⚠️ [TichDiemService] Số điểm tính được <= 0, bỏ qua tích điểm");
            return;
        }
        
        // Tạo lịch sử tích điểm
        LichSuDiem lichSuDiem = new LichSuDiem();
        lichSuDiem.setId(UUID.randomUUID());
        lichSuDiem.setTichDiem(tichDiem);
        lichSuDiem.setHoaDonId(hoaDonId);
        lichSuDiem.setIdQuyDoiDiem(quyDoiDiem);
        lichSuDiem.setLoaiDiem(LoaiDiem.TICH_DIEM.getValue());
        lichSuDiem.setSoDiemCong(soDiemCong);
        lichSuDiem.setSoDiemDaDung(0);
        lichSuDiem.setThoiGian(Instant.now());
        lichSuDiem.setHanSuDung(LocalDate.now().plusMonths(12)); // Hạn 12 tháng
        lichSuDiem.setTrangThai(TrangThaiDiem.CHUA_SU_DUNG.getValue());
        lichSuDiem.setGhiChu("Tích điểm từ hóa đơn " + (hoaDon.getMa() != null ? hoaDon.getMa() : hoaDonId.toString()));
        
        lichSuDiemRepository.save(lichSuDiem);
        
        // Cập nhật ví điểm
        Integer diemDaCong = tichDiem.getDiemDaCong() != null ? tichDiem.getDiemDaCong() : 0;
        Integer tongDiem = tichDiem.getTongDiem() != null ? tichDiem.getTongDiem() : 0;
        
        tichDiem.setDiemDaCong(diemDaCong + soDiemCong);
        tichDiem.setTongDiem(tongDiem + soDiemCong);
        tichDiemRepository.save(tichDiem);
        
        System.out.println("✅ [TichDiemService] Đã tích " + soDiemCong + " điểm cho khách hàng " + khachHangId + " từ hóa đơn " + hoaDonId);
    }
    
    /**
     * Trừ điểm khi thanh toán (refactor từ ThanhToanService)
     */
    @Transactional
    public void truDiemKhiThanhToan(UUID hoaDonId, Integer soDiemSuDung) {
        if (soDiemSuDung == null || soDiemSuDung <= 0) {
            return;
        }
        
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
            .orElseThrow(() -> new ApiException("Không tìm thấy hóa đơn", "INVOICE_NOT_FOUND"));
        
        if (hoaDon.getIdKhachHang() == null) {
            throw new ApiException("Không thể sử dụng điểm tích lũy cho khách lẻ", "POINTS_REQUIRE_CUSTOMER");
        }
        
        UUID khachHangId = hoaDon.getIdKhachHang().getId();
        
        // Lấy ví điểm
        TichDiem tichDiem = tichDiemRepository.findByUser_Id(khachHangId)
            .orElseThrow(() -> new ApiException("Khách hàng chưa có tài khoản điểm tích lũy", "POINTS_ACCOUNT_NOT_FOUND"));
        
        // Kiểm tra đủ điểm không
        Integer tongDiem = tichDiem.getTongDiem() != null ? tichDiem.getTongDiem() : 0;
        if (tongDiem < soDiemSuDung) {
            throw new ApiException(
                String.format("Khách hàng không đủ điểm tích lũy. Hiện có: %d điểm, cần: %d điểm", tongDiem, soDiemSuDung),
                "INSUFFICIENT_POINTS"
            );
        }
        
        // Lấy quy đổi điểm
        QuyDoiDiem quyDoiDiem = quyDoiDiemRepository.findFirstByTrangThaiOrderByIdAsc(1);
        if (quyDoiDiem == null || quyDoiDiem.getTienTieuDiem() == null) {
            throw new ApiException("Hệ thống quy đổi điểm đang tạm dừng", "POINTS_CONVERSION_UNAVAILABLE");
        }
        
        BigDecimal tienTieuDiem = quyDoiDiem.getTienTieuDiem();
        BigDecimal soTienQuyDoi = BigDecimal.valueOf(soDiemSuDung).multiply(tienTieuDiem);
        
        // Tạo lịch sử tiêu điểm
        LichSuDiem lichSuDiem = new LichSuDiem();
        lichSuDiem.setId(UUID.randomUUID());
        lichSuDiem.setTichDiem(tichDiem);
        lichSuDiem.setHoaDonId(hoaDonId);
        lichSuDiem.setIdQuyDoiDiem(quyDoiDiem);
        lichSuDiem.setLoaiDiem(LoaiDiem.TIEU_DIEM.getValue());
        lichSuDiem.setSoDiemDaDung(soDiemSuDung);
        lichSuDiem.setSoDiemCong(0);
        lichSuDiem.setThoiGian(Instant.now());
        lichSuDiem.setTrangThai(TrangThaiDiem.DA_SU_DUNG.getValue());
        lichSuDiem.setGhiChu("Tiêu " + soDiemSuDung + " điểm cho hóa đơn " + (hoaDon.getMa() != null ? hoaDon.getMa() : hoaDonId.toString()));
        
        lichSuDiemRepository.save(lichSuDiem);
        
        // Trừ điểm từ các lịch sử tích điểm cũ (FIFO - First In First Out)
        List<LichSuDiem> lichSuTichDiem = lichSuDiemRepository
            .findByTrangThaiAndTichDiem_User_Id(TrangThaiDiem.CHUA_SU_DUNG.getValue(), khachHangId)
            .stream()
            .filter(ls -> ls.getLoaiDiem() != null && ls.getLoaiDiem().equals(LoaiDiem.TICH_DIEM.getValue()))
            .filter(ls -> ls.getHanSuDung() == null || ls.getHanSuDung().isAfter(LocalDate.now()) || ls.getHanSuDung().equals(LocalDate.now()))
            .sorted((a, b) -> {
                if (a.getThoiGian() == null || b.getThoiGian() == null) return 0;
                return a.getThoiGian().compareTo(b.getThoiGian());
            })
            .collect(Collectors.toList());
        
        int diemConLaiCanTru = soDiemSuDung;
        for (LichSuDiem ls : lichSuTichDiem) {
            if (diemConLaiCanTru <= 0) break;
            
            Integer diemCoTheTru = ls.getSoDiemCong() != null ? ls.getSoDiemCong() : 0;
            Integer diemDaTru = chiTietLichSuDiemRepository.findByLichSuDiem_Id(ls.getId())
                .stream()
                .mapToInt(ct -> ct.getSoDiemDaTru() != null ? ct.getSoDiemDaTru() : 0)
                .sum();
            int diemConLai = diemCoTheTru - diemDaTru;
            
            if (diemConLai > 0) {
                int diemTruTrongLanNay = Math.min(diemConLai, diemConLaiCanTru);
                
                // Tạo chi tiết lịch sử điểm
                ChiTietLichSuDiem chiTiet = new ChiTietLichSuDiem();
                chiTiet.setId(UUID.randomUUID());
                chiTiet.setUser(hoaDon.getIdKhachHang());
                chiTiet.setLichSuDiem(ls);
                chiTiet.setSoDiemDaTru(diemTruTrongLanNay);
                chiTiet.setNgayTru(Instant.now());
                chiTietLichSuDiemRepository.save(chiTiet);
                
                diemConLaiCanTru -= diemTruTrongLanNay;
                
                // Cập nhật trạng thái nếu đã dùng hết
                if (diemTruTrongLanNay >= diemConLai) {
                    ls.setTrangThai(TrangThaiDiem.DA_SU_DUNG.getValue());
                    lichSuDiemRepository.save(ls);
                }
            }
        }
        
        // Cập nhật ví điểm
        Integer diemDaDung = tichDiem.getDiemDaDung() != null ? tichDiem.getDiemDaDung() : 0;
        Integer tongDiemMoi = tongDiem - soDiemSuDung;
        
        tichDiem.setDiemDaDung(diemDaDung + soDiemSuDung);
        tichDiem.setTongDiem(tongDiemMoi);
        tichDiemRepository.save(tichDiem);
        
        System.out.println("✅ [TichDiemService] Đã trừ " + soDiemSuDung + " điểm cho hóa đơn " + hoaDonId);
    }
    
    /**
     * Lấy lịch sử điểm có phân trang
     */
    public Page<LichSuDiemResponse> getLichSuDiemByUserId(UUID userId, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<LichSuDiem> lichSuDiemPage = lichSuDiemRepository.findByTichDiem_User_IdOrderByThoiGianDesc(userId, pageable);
        
        return lichSuDiemPage.map(this::convertToLichSuDiemResponse);
    }
    
    /**
     * Lấy lịch sử điểm theo hóa đơn
     */
    public List<LichSuDiemResponse> getLichSuDiemByHoaDonId(UUID hoaDonId) {
        List<LichSuDiem> lichSuDiemList = lichSuDiemRepository.findByHoaDonId(hoaDonId);
        return lichSuDiemList.stream()
            .map(this::convertToLichSuDiemResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Thêm/bớt điểm thủ công (admin)
     */
    @Transactional
    public void themBotDiemThuCong(TichDiemRequest request) {
        UUID userId = request.getUserId();
        Integer soDiemCong = request.getSoDiemCong();
        
        if (soDiemCong == null || soDiemCong == 0) {
            throw new ApiException("Số điểm không hợp lệ", "INVALID_POINTS");
        }
        
        // Lấy hoặc tạo ví điểm
        TichDiem tichDiem = tichDiemRepository.findByUser_Id(userId)
            .orElseGet(() -> taoViDiemChoKhachHang(userId));
        
        // Nếu là trừ điểm, kiểm tra đủ điểm không
        if (soDiemCong < 0) {
            Integer tongDiem = tichDiem.getTongDiem() != null ? tichDiem.getTongDiem() : 0;
            if (tongDiem < Math.abs(soDiemCong)) {
                throw new ApiException(
                    String.format("Không đủ điểm để trừ. Hiện có: %d điểm, cần trừ: %d điểm", tongDiem, Math.abs(soDiemCong)),
                    "INSUFFICIENT_POINTS"
                );
            }
        }
        
        // Tạo lịch sử điểm
        LichSuDiem lichSuDiem = new LichSuDiem();
        lichSuDiem.setId(UUID.randomUUID());
        lichSuDiem.setTichDiem(tichDiem);
        lichSuDiem.setHoaDonId(null);
        lichSuDiem.setIdQuyDoiDiem(null);
        lichSuDiem.setLoaiDiem(soDiemCong > 0 ? LoaiDiem.TICH_DIEM.getValue() : LoaiDiem.TIEU_DIEM.getValue());
        lichSuDiem.setSoDiemCong(soDiemCong > 0 ? soDiemCong : 0);
        lichSuDiem.setSoDiemDaDung(soDiemCong < 0 ? Math.abs(soDiemCong) : 0);
        lichSuDiem.setThoiGian(Instant.now());
        lichSuDiem.setHanSuDung(soDiemCong > 0 ? LocalDate.now().plusMonths(12) : null);
        lichSuDiem.setTrangThai(soDiemCong > 0 ? TrangThaiDiem.CHUA_SU_DUNG.getValue() : TrangThaiDiem.DA_SU_DUNG.getValue());
        lichSuDiem.setGhiChu("Thủ công: " + (request.getLyDo() != null ? request.getLyDo() : "") + 
            (request.getGhiChu() != null ? " - " + request.getGhiChu() : ""));
        
        lichSuDiemRepository.save(lichSuDiem);
        
        // Cập nhật ví điểm
        Integer diemDaCong = tichDiem.getDiemDaCong() != null ? tichDiem.getDiemDaCong() : 0;
        Integer diemDaDung = tichDiem.getDiemDaDung() != null ? tichDiem.getDiemDaDung() : 0;
        Integer tongDiem = tichDiem.getTongDiem() != null ? tichDiem.getTongDiem() : 0;
        
        if (soDiemCong > 0) {
            tichDiem.setDiemDaCong(diemDaCong + soDiemCong);
            tichDiem.setTongDiem(tongDiem + soDiemCong);
        } else {
            tichDiem.setDiemDaDung(diemDaDung + Math.abs(soDiemCong));
            tichDiem.setTongDiem(tongDiem + soDiemCong); // soDiemCong đã là số âm
        }
        
        tichDiemRepository.save(tichDiem);
    }
    
    /**
     * Convert LichSuDiem entity to LichSuDiemResponse
     */
    private LichSuDiemResponse convertToLichSuDiemResponse(LichSuDiem lichSuDiem) {
        LichSuDiemResponse response = new LichSuDiemResponse();
        response.setId(lichSuDiem.getId());
        response.setHoaDonId(lichSuDiem.getHoaDonId());
        
        // Lấy mã hóa đơn nếu có
        if (lichSuDiem.getHoaDonId() != null) {
            hoaDonRepository.findById(lichSuDiem.getHoaDonId())
                .ifPresent(hoaDon -> response.setMaHoaDon(hoaDon.getMa()));
        }
        
        response.setLoaiDiem(lichSuDiem.getLoaiDiem());
        LoaiDiem loaiDiem = LoaiDiem.fromValue(lichSuDiem.getLoaiDiem());
        response.setTenLoaiDiem(loaiDiem != null ? loaiDiem.getDescription() : null);
        
        response.setSoDiemDaDung(lichSuDiem.getSoDiemDaDung());
        response.setSoDiemCong(lichSuDiem.getSoDiemCong());
        response.setThoiGian(lichSuDiem.getThoiGian());
        response.setHanSuDung(lichSuDiem.getHanSuDung());
        response.setTrangThai(lichSuDiem.getTrangThai());
        TrangThaiDiem trangThai = TrangThaiDiem.fromValue(lichSuDiem.getTrangThai());
        response.setTenTrangThai(trangThai != null ? trangThai.getDescription() : null);
        response.setGhiChu(lichSuDiem.getGhiChu());
        
        // Lấy chi tiết lịch sử điểm
        List<ChiTietLichSuDiem> chiTietList = chiTietLichSuDiemRepository.findByLichSuDiem_Id(lichSuDiem.getId());
        List<LichSuDiemResponse.ChiTietLichSuDiemResponse> chiTietResponseList = chiTietList.stream()
            .map(ct -> {
                LichSuDiemResponse.ChiTietLichSuDiemResponse ctResponse = new LichSuDiemResponse.ChiTietLichSuDiemResponse();
                ctResponse.setId(ct.getId());
                ctResponse.setSoDiemDaTru(ct.getSoDiemDaTru());
                ctResponse.setNgayTru(ct.getNgayTru());
                return ctResponse;
            })
            .collect(Collectors.toList());
        response.setChiTietLichSuDiem(chiTietResponseList);
        
        return response;
    }
}

