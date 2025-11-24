// FILE: src/main/java/com/example/backendlaptop/service/phieugiamgia/PhieuGiamGiaKhachHangService.java
package com.example.backendlaptop.service.phieugiamgia;

import com.example.backendlaptop.entity.KhachHang;
import com.example.backendlaptop.entity.PhieuGiamGia;
import com.example.backendlaptop.entity.PhieuGiamGiaKhachHang;
import com.example.backendlaptop.expection.ApiException;
import com.example.backendlaptop.model.request.phieugiamgia.GanPhieuGiamGiaChoKhachHangRequest;
import com.example.backendlaptop.model.request.phieugiamgia.KiemTraPhieuGiamGiaRequest;
import com.example.backendlaptop.model.response.phieugiamgia.KhachHangPhieuGiamGiaResponse;
import com.example.backendlaptop.model.response.phieugiamgia.KiemTraPhieuGiamGiaResponse;
import com.example.backendlaptop.model.response.phieugiamgia.PhieuGiamGiaResponse;
import com.example.backendlaptop.repository.KhachHangRepository;
import com.example.backendlaptop.repository.PhieuGiamGiaKhachHangRepository;
import com.example.backendlaptop.repository.PhieuGiamGiaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PhieuGiamGiaKhachHangService {
    
    @Autowired
    private PhieuGiamGiaKhachHangRepository repository;
    
    @Autowired
    private PhieuGiamGiaRepository phieuGiamGiaRepository;
    
    @Autowired
    private KhachHangRepository khachHangRepository;
    
    @Autowired
    private PhieuGiamGiaEmailService emailService;
    
    @Transactional
    public void ganPhieuGiamGiaChoKhachHang(GanPhieuGiamGiaChoKhachHangRequest request) {
        PhieuGiamGia phieuGiamGia = phieuGiamGiaRepository.findById(request.getPhieuGiamGiaId())
                .orElseThrow(() -> new ApiException("Không tìm thấy phiếu giảm giá", "NOT_FOUND"));
        
        if (phieuGiamGia.getRiengTu() == null || !phieuGiamGia.getRiengTu()) {
            throw new ApiException("Chỉ có thể gán phiếu giảm giá cá nhân (rieng_tu = true)", "BAD_REQUEST");
        }
        
        List<PhieuGiamGiaKhachHang> toSave = new ArrayList<>();
        
        for (UUID khachHangId : request.getKhachHangIds()) {
            if (repository.existsByPhieuGiamGia_IdAndKhachHang_Id(request.getPhieuGiamGiaId(), khachHangId)) {
                continue;
            }
            
            KhachHang khachHang = khachHangRepository.findById(khachHangId)
                    .orElseThrow(() -> new ApiException("Không tìm thấy khách hàng với ID: " + khachHangId, "NOT_FOUND"));
            
            PhieuGiamGiaKhachHang pkgkh = new PhieuGiamGiaKhachHang();
            pkgkh.setPhieuGiamGia(phieuGiamGia);
            pkgkh.setKhachHang(khachHang);
            toSave.add(pkgkh);
        }
        
        if (!toSave.isEmpty()) {
            repository.saveAll(toSave);
        }
    }
    
    @Transactional
    public void capNhatKhachHangChoPhieuGiamGia(UUID phieuGiamGiaId, List<UUID> khachHangIds) {
        PhieuGiamGia phieuGiamGia = phieuGiamGiaRepository.findById(phieuGiamGiaId)
                .orElseThrow(() -> new ApiException("Không tìm thấy phiếu giảm giá", "NOT_FOUND"));
        
        if (phieuGiamGia.getRiengTu() == null || !phieuGiamGia.getRiengTu()) {
            throw new ApiException("Chỉ có thể cập nhật khách hàng cho phiếu giảm giá cá nhân", "BAD_REQUEST");
        }
        
        List<PhieuGiamGiaKhachHang> existing = repository.findByPhieuGiamGia_Id(phieuGiamGiaId);
        Set<UUID> existingIds = existing.stream()
                .map(pkgkh -> pkgkh.getKhachHang().getId())
                .collect(java.util.stream.Collectors.toSet());
        
        Set<UUID> newIds = new HashSet<>(khachHangIds);
        
        List<UUID> toRemove = existingIds.stream()
                .filter(id -> !newIds.contains(id))
                .collect(java.util.stream.Collectors.toList());
        
        List<UUID> toAdd = newIds.stream()
                .filter(id -> !existingIds.contains(id))
                .collect(java.util.stream.Collectors.toList());
        
        for (UUID id : toRemove) {
            repository.deleteByPhieuGiamGia_IdAndKhachHang_Id(phieuGiamGiaId, id);
        }
        
        if (!toAdd.isEmpty()) {
            GanPhieuGiamGiaChoKhachHangRequest addRequest = new GanPhieuGiamGiaChoKhachHangRequest();
            addRequest.setPhieuGiamGiaId(phieuGiamGiaId);
            addRequest.setKhachHangIds(toAdd);
            ganPhieuGiamGiaChoKhachHang(addRequest);
        }
    }
    
    public List<PhieuGiamGiaResponse> getPhieuGiamGiaCuaKhachHang(UUID khachHangId) {
        Instant now = Instant.now();
        
        List<PhieuGiamGia> phieuGiamGias = new ArrayList<>();
        
        List<PhieuGiamGia> phieuPublic = phieuGiamGiaRepository.findByTrangThaiAndRiengTu(1, false, org.springframework.data.domain.Pageable.unpaged()).getContent();
        phieuGiamGias.addAll(phieuPublic.stream()
                .filter(pgg -> isValidPhieuGiamGia(pgg, now))
                .collect(Collectors.toList()));
        
        List<PhieuGiamGiaKhachHang> phieuCaNhan = repository.findByKhachHang_Id(khachHangId);
        phieuGiamGias.addAll(phieuCaNhan.stream()
                .map(PhieuGiamGiaKhachHang::getPhieuGiamGia)
                .filter(pgg -> isValidPhieuGiamGia(pgg, now))
                .collect(Collectors.toList()));
        
        return phieuGiamGias.stream()
                .map(PhieuGiamGiaResponse::new)
                .distinct()
                .collect(Collectors.toList());
    }
    
    private boolean isValidPhieuGiamGia(PhieuGiamGia pgg, Instant now) {
        if (pgg.getTrangThai() == null || pgg.getTrangThai() != 1) {
            return false;
        }
        if (pgg.getNgayBatDau() != null && pgg.getNgayBatDau().isAfter(now)) {
            return false;
        }
        if (pgg.getNgayKetThuc() != null && pgg.getNgayKetThuc().isBefore(now)) {
            return false;
        }
        if (pgg.getSoLuongDung() != null && pgg.getSoLuongDung() <= 0) {
            return false;
        }
        return true;
    }
    
    public KiemTraPhieuGiamGiaResponse kiemTraVaApDungPhieuGiamGia(KiemTraPhieuGiamGiaRequest request) {
        PhieuGiamGia phieuGiamGia = phieuGiamGiaRepository.findByMaIgnoreCase(request.getMa())
                .orElse(null);
        
        if (phieuGiamGia == null) {
            return new KiemTraPhieuGiamGiaResponse(false, "Mã phiếu giảm giá không tồn tại", null, null, null, null);
        }
        
        Instant now = Instant.now();
        
        if (phieuGiamGia.getTrangThai() == null || phieuGiamGia.getTrangThai() != 1) {
            return new KiemTraPhieuGiamGiaResponse(false, "Phiếu giảm giá không hoạt động", null, null, null, null);
        }
        
        if (phieuGiamGia.getNgayBatDau() != null && phieuGiamGia.getNgayBatDau().isAfter(now)) {
            return new KiemTraPhieuGiamGiaResponse(false, "Phiếu giảm giá chưa có hiệu lực", null, null, null, null);
        }
        
        if (phieuGiamGia.getNgayKetThuc() != null && phieuGiamGia.getNgayKetThuc().isBefore(now)) {
            return new KiemTraPhieuGiamGiaResponse(false, "Phiếu giảm giá đã hết hạn", null, null, null, null);
        }
        
        if (phieuGiamGia.getSoLuongDung() != null && phieuGiamGia.getSoLuongDung() <= 0) {
            return new KiemTraPhieuGiamGiaResponse(false, "Phiếu giảm giá đã hết lượt sử dụng", null, null, null, null);
        }
        
        if (phieuGiamGia.getHoaDonToiThieu() != null && request.getTongTienHoaDon().compareTo(phieuGiamGia.getHoaDonToiThieu()) < 0) {
            return new KiemTraPhieuGiamGiaResponse(false, 
                    "Hóa đơn tối thiểu: " + phieuGiamGia.getHoaDonToiThieu() + " VND", 
                    null, null, null, null);
        }
        
        if (Boolean.TRUE.equals(phieuGiamGia.getRiengTu())) {
            boolean coQuyen = repository.existsByPhieuGiamGia_IdAndKhachHang_Id(phieuGiamGia.getId(), request.getKhachHangId());
            if (!coQuyen) {
                return new KiemTraPhieuGiamGiaResponse(false, "Bạn không có quyền sử dụng phiếu giảm giá này", null, null, null, null);
            }
        }
        
        BigDecimal tienDuocGiam = calculateTienGiam(phieuGiamGia, request.getTongTienHoaDon());
        BigDecimal tongTienSauGiam = request.getTongTienHoaDon().subtract(tienDuocGiam);
        
        return new KiemTraPhieuGiamGiaResponse(true, "Phiếu giảm giá hợp lệ", 
                phieuGiamGia.getId(), phieuGiamGia.getTenPhieuGiamGia(), 
                tienDuocGiam, tongTienSauGiam);
    }
    
    private BigDecimal calculateTienGiam(PhieuGiamGia pgg, BigDecimal tongTien) {
        BigDecimal tienGiam = BigDecimal.ZERO;
        
        if (pgg.getLoaiPhieuGiamGia() != null && pgg.getLoaiPhieuGiamGia() == 0) {
            if (pgg.getGiaTriGiamGia() != null && pgg.getGiaTriGiamGia().compareTo(BigDecimal.ZERO) > 0) {
                tienGiam = tongTien.multiply(pgg.getGiaTriGiamGia())
                        .divide(new BigDecimal(100), 2, java.math.RoundingMode.HALF_UP);
                
                if (pgg.getSoTienGiamToiDa() != null && tienGiam.compareTo(pgg.getSoTienGiamToiDa()) > 0) {
                    tienGiam = pgg.getSoTienGiamToiDa();
                }
            }
        } else {
            if (pgg.getGiaTriGiamGia() != null) {
                tienGiam = pgg.getGiaTriGiamGia();
            }
        }
        
        if (tienGiam.compareTo(tongTien) > 0) {
            tienGiam = tongTien;
        }
        
        return tienGiam;
    }
    
    public List<KhachHangPhieuGiamGiaResponse> getKhachHangCuaPhieuGiamGia(UUID phieuGiamGiaId) {
        List<PhieuGiamGiaKhachHang> list = repository.findByPhieuGiamGia_Id(phieuGiamGiaId);
        return list.stream()
                .map(PhieuGiamGiaKhachHang::getKhachHang)
                .map(KhachHangPhieuGiamGiaResponse::new)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public void xoaKhachHangKhoiPhieuGiamGia(UUID phieuGiamGiaId, UUID khachHangId, boolean guiEmailXinLoi) {
        PhieuGiamGia phieuGiamGia = phieuGiamGiaRepository.findById(phieuGiamGiaId)
                .orElseThrow(() -> new ApiException("Không tìm thấy phiếu giảm giá", "NOT_FOUND"));
        
        if (phieuGiamGia.getRiengTu() == null || !phieuGiamGia.getRiengTu()) {
            throw new ApiException("Chỉ có thể xóa khách hàng khỏi phiếu giảm giá cá nhân", "BAD_REQUEST");
        }
        
        if (!repository.existsByPhieuGiamGia_IdAndKhachHang_Id(phieuGiamGiaId, khachHangId)) {
            throw new ApiException("Khách hàng không có trong danh sách phiếu giảm giá này", "NOT_FOUND");
        }
        
        // Xóa khách hàng khỏi phiếu giảm giá
        repository.deleteByPhieuGiamGia_IdAndKhachHang_Id(phieuGiamGiaId, khachHangId);
        
        // Gửi email xin lỗi nếu được yêu cầu
        if (guiEmailXinLoi) {
            try {
                emailService.guiEmailXinLoiKhiXoaKhachHang(phieuGiamGiaId, khachHangId);
            } catch (Exception e) {
                // Log lỗi nhưng không throw để đảm bảo việc xóa vẫn thành công
                System.err.println("Lỗi khi gửi email xin lỗi cho khách hàng " + khachHangId + ": " + e.getMessage());
            }
        }
    }
}

