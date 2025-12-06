package com.example.backendlaptop.service.baohanh;

import com.example.backendlaptop.entity.PhieuBaoHanh;
import com.example.backendlaptop.repository.PhieuBaoHanhRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PhieuBaoHanhPDFService {
    private final PhieuBaoHanhRepository phieuBaoHanhRepository;

    public String generatePhieuBaoHanhHTML(UUID idBaoHanh) {
        PhieuBaoHanh phieuBaoHanh = phieuBaoHanhRepository.findByIdWithRelations(idBaoHanh)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu bảo hành với ID: " + idBaoHanh));

        String ngayBatDau = phieuBaoHanh.getNgayBatDau() != null ?
                java.time.LocalDateTime.ofInstant(phieuBaoHanh.getNgayBatDau(), java.time.ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
        String ngayKetThuc = phieuBaoHanh.getNgayKetThuc() != null ?
                java.time.LocalDateTime.ofInstant(phieuBaoHanh.getNgayKetThuc(), java.time.ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";

        String tenKhachHang = phieuBaoHanh.getIdKhachHang() != null ? phieuBaoHanh.getIdKhachHang().getHoTen() : "N/A";
        String soDienThoai = phieuBaoHanh.getIdKhachHang() != null ? phieuBaoHanh.getIdKhachHang().getSoDienThoai() : "N/A";
        String tenSanPham = "N/A";
        String serial = "N/A";

        if (phieuBaoHanh.getIdSerialDaBan() != null) {
            if (phieuBaoHanh.getIdSerialDaBan().getIdSerial() != null) {
                serial = phieuBaoHanh.getIdSerialDaBan().getIdSerial().getSerialNo();
            }
            if (phieuBaoHanh.getIdSerialDaBan().getIdHoaDonChiTiet() != null &&
                phieuBaoHanh.getIdSerialDaBan().getIdHoaDonChiTiet().getChiTietSanPham() != null &&
                phieuBaoHanh.getIdSerialDaBan().getIdHoaDonChiTiet().getChiTietSanPham().getSanPham() != null) {
                tenSanPham = phieuBaoHanh.getIdSerialDaBan().getIdHoaDonChiTiet().getChiTietSanPham().getSanPham().getTenSanPham();
            }
        }

        return "<!DOCTYPE html><html><head><meta charset='UTF-8'><style>" +
                "body{font-family:Arial,sans-serif;padding:20px;max-width:800px;margin:0 auto;}" +
                ".header{text-align:center;border-bottom:3px solid #10b981;padding-bottom:20px;margin-bottom:30px;}" +
                ".header h1{color:#10b981;margin:0;}" +
                ".info-section{margin:20px 0;}" +
                ".info-row{display:flex;justify-content:space-between;padding:10px 0;border-bottom:1px solid #e5e7eb;}" +
                ".info-label{font-weight:bold;color:#374151;}" +
                ".info-value{color:#111827;}" +
                ".footer{text-align:center;margin-top:40px;padding-top:20px;border-top:2px solid #e5e7eb;color:#6b7280;}" +
                "</style></head><body>" +
                "<div class='header'><h1>PHIẾU BẢO HÀNH</h1></div>" +
                "<div class='info-section'>" +
                "<div class='info-row'><span class='info-label'>Mã phiếu:</span><span class='info-value'>" + (phieuBaoHanh.getMaPhieuBaoHanh() != null ? phieuBaoHanh.getMaPhieuBaoHanh() : "N/A") + "</span></div>" +
                "<div class='info-row'><span class='info-label'>Khách hàng:</span><span class='info-value'>" + tenKhachHang + "</span></div>" +
                "<div class='info-row'><span class='info-label'>Số điện thoại:</span><span class='info-value'>" + soDienThoai + "</span></div>" +
                "<div class='info-row'><span class='info-label'>Sản phẩm:</span><span class='info-value'>" + tenSanPham + "</span></div>" +
                "<div class='info-row'><span class='info-label'>Serial:</span><span class='info-value'>" + serial + "</span></div>" +
                "<div class='info-row'><span class='info-label'>Ngày bắt đầu:</span><span class='info-value'>" + ngayBatDau + "</span></div>" +
                "<div class='info-row'><span class='info-label'>Ngày kết thúc:</span><span class='info-value'>" + ngayKetThuc + "</span></div>" +
                (phieuBaoHanh.getMoTa() != null ? "<div class='info-row'><span class='info-label'>Mô tả:</span><span class='info-value'>" + phieuBaoHanh.getMoTa() + "</span></div>" : "") +
                "</div><div class='footer'><p>VietLaptop - Dịch vụ bảo hành chuyên nghiệp</p></div>" +
                "</body></html>";
    }
}

