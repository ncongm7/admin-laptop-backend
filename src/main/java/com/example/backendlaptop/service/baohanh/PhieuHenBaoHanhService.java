package com.example.backendlaptop.service.baohanh;

import com.example.backendlaptop.entity.NhanVien;
import com.example.backendlaptop.entity.PhieuBaoHanh;
import com.example.backendlaptop.entity.PhieuHenBaoHanh;
import com.example.backendlaptop.expection.ApiException;
import com.example.backendlaptop.model.TrangThaiPhieuHen;
import com.example.backendlaptop.model.request.baohanh.PhieuHenBaoHanhRequest;
import com.example.backendlaptop.model.response.baohanh.PhieuHenBaoHanhResponse;
import com.example.backendlaptop.repository.NhanVienRepository;
import com.example.backendlaptop.repository.PhieuBaoHanhRepository;
import com.example.backendlaptop.repository.PhieuHenBaoHanhRepository;
import com.example.backendlaptop.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PhieuHenBaoHanhService {
    private final PhieuHenBaoHanhRepository phieuHenBaoHanhRepository;
    private final PhieuBaoHanhRepository phieuBaoHanhRepository;
    private final NhanVienRepository nhanVienRepository;
    private final EmailService emailService;

    @Transactional
    public PhieuHenBaoHanhResponse taoPhieuHen(UUID idBaoHanh, PhieuHenBaoHanhRequest request) {
        // Validate input
        if (request == null) {
            throw new ApiException("Request không được để trống", "INVALID_REQUEST");
        }
        if (request.getNgayHen() == null) {
            throw new ApiException("Ngày hẹn không được để trống", "INVALID_REQUEST");
        }
        if (request.getGioHen() == null) {
            throw new ApiException("Giờ hẹn không được để trống", "INVALID_REQUEST");
        }
        if (request.getDiaDiem() == null || request.getDiaDiem().trim().isEmpty()) {
            throw new ApiException("Địa điểm không được để trống", "INVALID_REQUEST");
        }

        PhieuBaoHanh phieuBaoHanh = phieuBaoHanhRepository.findById(idBaoHanh)
                .orElseThrow(() -> new ApiException("Không tìm thấy phiếu bảo hành với ID: " + idBaoHanh, "NOT_FOUND"));

        PhieuHenBaoHanh phieuHen = new PhieuHenBaoHanh();
        // KHÔNG set ID thủ công - để Hibernate tự generate thông qua @UuidGenerator
        // phieuHen.setId(UUID.randomUUID()); // ❌ XÓA DÒNG NÀY
        phieuHen.setIdBaoHanh(phieuBaoHanh);
        phieuHen.setNgayHen(request.getNgayHen());
        phieuHen.setGioHen(request.getGioHen());
        phieuHen.setDiaDiem(request.getDiaDiem().trim());
        phieuHen.setGhiChu(request.getGhiChu() != null ? request.getGhiChu().trim() : null);
        phieuHen.setTrangThai(TrangThaiPhieuHen.CHO_XAC_NHAN.getValue());
        phieuHen.setEmailDaGui(false);
        phieuHen.setNgayTao(Instant.now());

        if (request.getIdNhanVienTiepNhan() != null) {
            NhanVien nhanVien = nhanVienRepository.findById(request.getIdNhanVienTiepNhan())
                    .orElseThrow(() -> new ApiException("Không tìm thấy nhân viên với ID: " + request.getIdNhanVienTiepNhan(), "NOT_FOUND"));
            phieuHen.setIdNhanVienTiepNhan(nhanVien);
        }

        // Generate mã phiếu hẹn: PH-YYYYMMDD-XXX
        String maPhieuHen = generateMaPhieuHen();
        phieuHen.setMaPhieuHen(maPhieuHen);

        try {
            phieuHen = phieuHenBaoHanhRepository.save(phieuHen);
        } catch (Exception e) {
            throw new ApiException("Lỗi khi lưu phiếu hẹn: " + e.getMessage(), "SAVE_ERROR");
        }

        return new PhieuHenBaoHanhResponse(phieuHen);
    }

    @Transactional
    public void guiEmailPhieuHen(UUID idPhieuHen) {
        PhieuHenBaoHanh phieuHen = phieuHenBaoHanhRepository.findById(idPhieuHen)
                .orElseThrow(() -> new ApiException("Không tìm thấy phiếu hẹn", "NOT_FOUND"));

        if (Boolean.TRUE.equals(phieuHen.getEmailDaGui())) {
            throw new ApiException("Email đã được gửi trước đó", "EMAIL_ALREADY_SENT");
        }

        try {
            emailService.guiEmailPhieuHen(phieuHen);
            phieuHen.setEmailDaGui(true);
            phieuHenBaoHanhRepository.save(phieuHen);
        } catch (Exception e) {
            throw new ApiException("Lỗi khi gửi email: " + e.getMessage(), "EMAIL_SEND_ERROR");
        }
    }

    @Transactional
    public PhieuHenBaoHanhResponse xacNhanPhieuHen(UUID idPhieuHen) {
        PhieuHenBaoHanh phieuHen = phieuHenBaoHanhRepository.findById(idPhieuHen)
                .orElseThrow(() -> new ApiException("Không tìm thấy phiếu hẹn", "NOT_FOUND"));

        if (phieuHen.getTrangThai() != TrangThaiPhieuHen.CHO_XAC_NHAN.getValue()) {
            throw new ApiException("Phiếu hẹn không ở trạng thái chờ xác nhận", "INVALID_STATUS");
        }

        phieuHen.setTrangThai(TrangThaiPhieuHen.DA_XAC_NHAN.getValue());
        phieuHen = phieuHenBaoHanhRepository.save(phieuHen);

        return new PhieuHenBaoHanhResponse(phieuHen);
    }

    public PhieuHenBaoHanhResponse getPhieuHenById(UUID id) {
        PhieuHenBaoHanh phieuHen = phieuHenBaoHanhRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy phiếu hẹn", "NOT_FOUND"));
        return new PhieuHenBaoHanhResponse(phieuHen);
    }

    public List<PhieuHenBaoHanhResponse> getPhieuHenByBaoHanh(UUID idBaoHanh) {
        List<PhieuHenBaoHanh> phieuHenList = phieuHenBaoHanhRepository.findByIdBaoHanh_IdOrderByNgayHenDesc(idBaoHanh);
        return phieuHenList.stream()
                .map(PhieuHenBaoHanhResponse::new)
                .collect(Collectors.toList());
    }

    private String generateMaPhieuHen() {
        String prefix = "PH";
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String sequence = String.format("%03d", (int) (Math.random() * 1000));
        return prefix + "-" + dateStr + "-" + sequence;
    }
}

