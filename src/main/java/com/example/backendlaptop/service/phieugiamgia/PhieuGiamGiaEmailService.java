// FILE: src/main/java/com/example/backendlaptop/service/phieugiamgia/PhieuGiamGiaEmailService.java
package com.example.backendlaptop.service.phieugiamgia;

import com.example.backendlaptop.entity.KhachHang;
import com.example.backendlaptop.entity.PhieuGiamGia;
import com.example.backendlaptop.expection.ApiException;
import com.example.backendlaptop.repository.KhachHangRepository;
import com.example.backendlaptop.repository.PhieuGiamGiaRepository;
import com.example.backendlaptop.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class PhieuGiamGiaEmailService {
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private PhieuGiamGiaRepository phieuGiamGiaRepository;
    
    @Autowired
    private KhachHangRepository khachHangRepository;
    
    public void guiPhieuGiamGiaChoKhachHang(UUID phieuGiamGiaId, UUID customerId) {
        PhieuGiamGia phieuGiamGia = phieuGiamGiaRepository.findById(phieuGiamGiaId)
                .orElseThrow(() -> new ApiException("Không tìm thấy phiếu giảm giá", "NOT_FOUND"));
        
        KhachHang khachHang = khachHangRepository.findById(customerId)
                .orElseThrow(() -> new ApiException("Không tìm thấy khách hàng", "NOT_FOUND"));
        
        if (khachHang.getEmail() == null || khachHang.getEmail().trim().isEmpty()) {
            throw new ApiException("Khách hàng chưa có email", "BAD_REQUEST");
        }
        
        String subject = "Phiếu giảm giá từ Dell Viet Laptop";
        String htmlContent = buildPhieuGiamGiaEmailContent(khachHang, phieuGiamGia);
        
        emailService.sendPhieuGiamGiaEmail(khachHang.getEmail(), subject, htmlContent);
    }
    
    public void guiPhieuGiamGiaChoNhieuKhachHang(UUID phieuGiamGiaId, List<UUID> customerIds) {
        for (UUID customerId : customerIds) {
            try {
                guiPhieuGiamGiaChoKhachHang(phieuGiamGiaId, customerId);
            } catch (Exception e) {
                // Log lỗi nhưng tiếp tục gửi cho các khách hàng khác
                System.err.println("Lỗi khi gửi email cho khách hàng " + customerId + ": " + e.getMessage());
            }
        }
    }
    
    private String buildPhieuGiamGiaEmailContent(KhachHang khachHang, PhieuGiamGia phieuGiamGia) {
        String tenKhachHang = khachHang.getHoTen() != null ? khachHang.getHoTen() : "Quý khách";
        String maPhieu = phieuGiamGia.getMa() != null ? phieuGiamGia.getMa() : "";
        String tenPhieu = phieuGiamGia.getTenPhieuGiamGia() != null ? phieuGiamGia.getTenPhieuGiamGia() : "";
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                .withZone(ZoneId.of("Asia/Ho_Chi_Minh"));
        String ngayHetHan = phieuGiamGia.getNgayKetThuc() != null 
                ? formatter.format(phieuGiamGia.getNgayKetThuc()) 
                : "Không xác định";
        
        String giaTriGiamGia = "";
        if (phieuGiamGia.getLoaiPhieuGiamGia() != null && phieuGiamGia.getLoaiPhieuGiamGia() == 0) {
            giaTriGiamGia = phieuGiamGia.getGiaTriGiamGia() + "%";
        } else {
            giaTriGiamGia = String.format("%,.0f VND", phieuGiamGia.getGiaTriGiamGia() != null ? phieuGiamGia.getGiaTriGiamGia().doubleValue() : 0);
        }
        
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #007bff; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background-color: #f9f9f9; }
                    .voucher-box { border: 2px dashed #007bff; padding: 20px; margin: 20px 0; text-align: center; background-color: white; }
                    .voucher-code { font-size: 24px; font-weight: bold; color: #007bff; margin: 10px 0; }
                    .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Dell Viet Laptop</h1>
                    </div>
                    <div class="content">
                        <p>Xin chào <strong>%s</strong>,</p>
                        <p>Chúng tôi gửi tặng bạn phiếu giảm giá đặc biệt:</p>
                        <div class="voucher-box">
                            <h2>%s</h2>
                            <div class="voucher-code">%s</div>
                            <p>Giảm giá: <strong>%s</strong></p>
                            <p>Hết hạn: <strong>%s</strong></p>
                        </div>
                        <p>Hãy sử dụng mã phiếu giảm giá này khi thanh toán để nhận được ưu đãi!</p>
                        <p>Trân trọng,<br>Đội ngũ Dell Viet Laptop</p>
                    </div>
                    <div class="footer">
                        <p>© 2024 Dell Viet Laptop. Tất cả quyền được bảo lưu.</p>
                    </div>
                </div>
            </body>
            </html>
            """, tenKhachHang, tenPhieu, maPhieu, giaTriGiamGia, ngayHetHan);
    }
}

