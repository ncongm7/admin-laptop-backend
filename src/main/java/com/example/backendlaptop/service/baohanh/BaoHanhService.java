package com.example.backendlaptop.service.baohanh;

import com.example.backendlaptop.dto.trahang.KiemTraDieuKienResponse;
import com.example.backendlaptop.entity.HoaDon;
import com.example.backendlaptop.entity.HoaDonChiTiet;
import com.example.backendlaptop.entity.KhachHang;
import com.example.backendlaptop.entity.PhieuBaoHanh;
import com.example.backendlaptop.entity.SerialDaBan;
import com.example.backendlaptop.expection.ApiException;
import com.example.backendlaptop.model.request.baohanh.TaoYeuCauBaoHanhRequest;
import com.example.backendlaptop.model.response.baohanh.PhieuBaoHanhResponse;
import com.example.backendlaptop.repository.KhachHangRepository;
import com.example.backendlaptop.repository.PhieuBaoHanhRepository;
import com.example.backendlaptop.repository.SerialDaBanRepository;
import com.example.backendlaptop.repository.banhang.HoaDonChiTietRepository;
import com.example.backendlaptop.repository.banhang.HoaDonRepository;
import com.example.backendlaptop.service.trahang.TraHangService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BaoHanhService {

    private final TraHangService traHangService;
    private final PhieuBaoHanhRepository phieuBaoHanhRepository;
    private final HoaDonRepository hoaDonRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final KhachHangRepository khachHangRepository;
    private final SerialDaBanRepository serialDaBanRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public KiemTraDieuKienResponse kiemTraDieuKien(UUID idHoaDon) {
        return traHangService.kiemTraDieuKien(idHoaDon);
    }

    @Transactional
    public PhieuBaoHanhResponse taoYeuCau(
            TaoYeuCauBaoHanhRequest request,
            List<MultipartFile> hinhAnhFiles
    ) {
        try {
            Instant now = Instant.now();

            HoaDon hoaDon = hoaDonRepository.findById(request.getIdHoaDon())
                    .orElseThrow(() -> new ApiException("Không tìm thấy hóa đơn", "NOT_FOUND"));

            KhachHang khachHang = khachHangRepository.findById(request.getIdKhachHang())
                    .orElseThrow(() -> new ApiException("Không tìm thấy khách hàng", "NOT_FOUND"));

            if (!hoaDon.getIdKhachHang().getId().equals(request.getIdKhachHang())) {
                throw new ApiException("Bạn không có quyền tạo bảo hành cho hóa đơn này", "UNAUTHORIZED");
            }

            HoaDonChiTiet hoaDonChiTiet = hoaDonChiTietRepository.findById(request.getIdHoaDonChiTiet())
                    .orElseThrow(() -> new ApiException("Không tìm thấy chi tiết hóa đơn", "NOT_FOUND"));

            if (!hoaDonChiTiet.getHoaDon().getId().equals(request.getIdHoaDon())) {
                throw new ApiException("Chi tiết hóa đơn không thuộc đơn hàng này", "INVALID_DETAIL");
            }

            SerialDaBan serialDaBan = resolveSerialDaBan(request, hoaDonChiTiet.getId());

            List<String> uploadedImages = uploadImages(hinhAnhFiles);

            if (serialDaBan != null) {
                phieuBaoHanhRepository.findByIdSerialDaBan_Id(serialDaBan.getId()).ifPresent(existing -> {
                    existing.setIdSerialDaBan(null);
                    phieuBaoHanhRepository.saveAndFlush(existing);
                });
            }

            PhieuBaoHanh phieuBaoHanh = new PhieuBaoHanh();
            phieuBaoHanh.setId(UUID.randomUUID());
            phieuBaoHanh.setIdKhachHang(khachHang);
            phieuBaoHanh.setIdSerialDaBan(serialDaBan);
            phieuBaoHanh.setNgayBatDau(now);
            phieuBaoHanh.setNgayKetThuc(now.plus(365, ChronoUnit.DAYS));
            phieuBaoHanh.setTrangThaiBaoHanh(1);
            phieuBaoHanh.setMoTa(
                    request.getMoTaTinhTrang() != null && !request.getMoTaTinhTrang().isBlank()
                            ? request.getMoTaTinhTrang()
                            : request.getLyDoTraHang()
            );
            phieuBaoHanh.setChiPhi(BigDecimal.ZERO);
            phieuBaoHanh.setSoLanSuaChua(0);

            if (!uploadedImages.isEmpty()) {
                try {
                    phieuBaoHanh.setHinhAnh(objectMapper.writeValueAsString(uploadedImages));
                } catch (Exception e) {
                    phieuBaoHanh.setHinhAnh(String.join(",", uploadedImages));
                }
            }

            PhieuBaoHanh saved = phieuBaoHanhRepository.save(phieuBaoHanh);
            return new PhieuBaoHanhResponse(saved);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Lỗi khi tạo phiếu bảo hành: " + e.getMessage(), "CREATE_WARRANTY_ERROR");
        }
    }

    private SerialDaBan resolveSerialDaBan(TaoYeuCauBaoHanhRequest request, UUID idHoaDonChiTiet) {
        SerialDaBan serialDaBan = null;
        if (request.getIdSerialDaBan() != null) {
            serialDaBan = serialDaBanRepository.findById(request.getIdSerialDaBan())
                    .orElse(null);
        }
        if (serialDaBan == null) {
            List<SerialDaBan> serials = serialDaBanRepository.findByIdHoaDonChiTiet_Id(idHoaDonChiTiet);
            if (!serials.isEmpty()) {
                serialDaBan = serials.get(0);
            }
        }
        return serialDaBan;
    }

    private List<String> uploadImages(List<MultipartFile> files) throws IOException {
        List<String> urls = new ArrayList<>();
        if (files == null || files.isEmpty()) {
            return urls;
        }

        String uploadDir = "uploads/bao-hanh/";
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".jpg";
            String filename = UUID.randomUUID() + extension;
            String filePath = uploadDir + filename;
            file.transferTo(new File(filePath));
            urls.add("/" + filePath);
        }
        return urls;
    }
}
