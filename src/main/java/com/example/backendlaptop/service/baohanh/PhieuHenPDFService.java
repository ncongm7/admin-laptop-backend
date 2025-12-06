package com.example.backendlaptop.service.baohanh;

import com.example.backendlaptop.entity.PhieuHenBaoHanh;
import com.example.backendlaptop.repository.PhieuHenBaoHanhRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PhieuHenPDFService {
    private final PhieuHenBaoHanhRepository phieuHenBaoHanhRepository;

    public String generatePhieuHenHTML(UUID idPhieuHen) {
        PhieuHenBaoHanh phieuHen = phieuHenBaoHanhRepository.findById(idPhieuHen)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu hẹn với ID: " + idPhieuHen));

        String ngayHen = phieuHen.getNgayHen() != null ?
                java.time.LocalDateTime.ofInstant(phieuHen.getNgayHen(), java.time.ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
        String gioHen = phieuHen.getGioHen() != null ? phieuHen.getGioHen().toString() : "";

        return "<!DOCTYPE html><html><head><meta charset='UTF-8'><style>" +
                "body{font-family:Arial,sans-serif;padding:20px;max-width:800px;margin:0 auto;}" +
                ".header{text-align:center;border-bottom:3px solid #2563eb;padding-bottom:20px;margin-bottom:30px;}" +
                ".header h1{color:#2563eb;margin:0;}" +
                ".info-section{margin:20px 0;}" +
                ".info-row{display:flex;justify-content:space-between;padding:10px 0;border-bottom:1px solid #e5e7eb;}" +
                ".info-label{font-weight:bold;color:#374151;}" +
                ".info-value{color:#111827;}" +
                ".footer{text-align:center;margin-top:40px;padding-top:20px;border-top:2px solid #e5e7eb;color:#6b7280;}" +
                "</style></head><body>" +
                "<div class='header'><h1>PHIẾU HẸN BẢO HÀNH</h1></div>" +
                "<div class='info-section'>" +
                "<div class='info-row'><span class='info-label'>Mã phiếu hẹn:</span><span class='info-value'>" + phieuHen.getMaPhieuHen() + "</span></div>" +
                "<div class='info-row'><span class='info-label'>Ngày hẹn:</span><span class='info-value'>" + ngayHen + "</span></div>" +
                "<div class='info-row'><span class='info-label'>Giờ hẹn:</span><span class='info-value'>" + gioHen + "</span></div>" +
                "<div class='info-row'><span class='info-label'>Địa điểm:</span><span class='info-value'>" + (phieuHen.getDiaDiem() != null ? phieuHen.getDiaDiem() : "Trung tâm bảo hành") + "</span></div>" +
                (phieuHen.getGhiChu() != null ? "<div class='info-row'><span class='info-label'>Ghi chú:</span><span class='info-value'>" + phieuHen.getGhiChu() + "</span></div>" : "") +
                "</div><div class='footer'><p>VietLaptop - Dịch vụ bảo hành chuyên nghiệp</p></div>" +
                "</body></html>";
    }
}

