package com.example.backendlaptop.service.banhang;

import com.example.backendlaptop.entity.HoaDon;
import com.example.backendlaptop.entity.HoaDonChiTiet;
import com.example.backendlaptop.entity.SerialDaBan;
import com.example.backendlaptop.repository.banhang.HoaDonRepository;
import com.example.backendlaptop.repository.SerialDaBanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service xử lý in hóa đơn
 * Generate HTML invoice với format đẹp
 */
@Service
@RequiredArgsConstructor
public class InHoaDonService {

    private final HoaDonRepository hoaDonRepository;
    private final SerialDaBanRepository serialDaBanRepository;

    /**
     * Generate HTML invoice từ hóa đơn
     * @param idHoaDon UUID của hóa đơn
     * @return HTML string
     */
    public String generateInvoiceHTML(UUID idHoaDon) {
        HoaDon hoaDon = hoaDonRepository.findById(idHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn với ID: " + idHoaDon));

        // Lấy danh sách serial đã bán cho từng chi tiết hóa đơn
        Map<UUID, List<String>> serialMap = new HashMap<>();
        if (hoaDon.getHoaDonChiTiets() != null) {
            for (HoaDonChiTiet hdct : hoaDon.getHoaDonChiTiets()) {
                List<SerialDaBan> serials = serialDaBanRepository.findByIdHoaDonChiTiet_Id(hdct.getId());
                List<String> serialNumbers = serials.stream()
                        .map(s -> s.getIdSerial() != null ? s.getIdSerial().getSerialNo() : "")
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList());
                serialMap.put(hdct.getId(), serialNumbers);
            }
        }

        // Format ngày tháng
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String ngayTao = hoaDon.getNgayTao() != null 
                ? LocalDateTime.ofInstant(hoaDon.getNgayTao(), ZoneId.systemDefault()).format(dateFormatter) 
                : "N/A";
        String ngayThanhToan = hoaDon.getNgayThanhToan() != null 
                ? LocalDateTime.ofInstant(hoaDon.getNgayThanhToan(), ZoneId.systemDefault()).format(dateFormatter) 
                : "Chưa thanh toán";

        // Thông tin khách hàng
        String tenKhachHang = hoaDon.getTenKhachHang() != null && !hoaDon.getTenKhachHang().isEmpty()
                ? hoaDon.getTenKhachHang()
                : "Khách lẻ";
        String sdt = hoaDon.getSdt() != null && !hoaDon.getSdt().isEmpty()
                ? hoaDon.getSdt()
                : "N/A";

        // Tính tổng tiền
        BigDecimal tongTien = hoaDon.getTongTien() != null ? hoaDon.getTongTien() : BigDecimal.ZERO;
        BigDecimal tienDuocGiam = hoaDon.getTienDuocGiam() != null ? hoaDon.getTienDuocGiam() : BigDecimal.ZERO;
        BigDecimal tongTienSauGiam = hoaDon.getTongTienSauGiam() != null ? hoaDon.getTongTienSauGiam() : tongTien;
        Integer soDiemSuDung = hoaDon.getSoDiemSuDung() != null ? hoaDon.getSoDiemSuDung() : 0;
        BigDecimal soTienQuyDoi = hoaDon.getSoTienQuyDoi() != null ? hoaDon.getSoTienQuyDoi() : BigDecimal.ZERO;

        // Tên voucher
        String tenVoucher = hoaDon.getIdPhieuGiamGia() != null 
                ? (hoaDon.getIdPhieuGiamGia().getTenPhieuGiamGia() != null 
                    ? hoaDon.getIdPhieuGiamGia().getTenPhieuGiamGia() 
                    : hoaDon.getIdPhieuGiamGia().getMa() != null 
                        ? hoaDon.getIdPhieuGiamGia().getMa() 
                        : "Voucher")
                : "";

        // Build HTML
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html lang='vi'>");
        html.append("<head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        html.append("<title>Hóa đơn ").append(hoaDon.getMa()).append("</title>");
        html.append("<style>");
        html.append(getInvoiceCSS());
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        html.append("<div class='invoice-container'>");
        
        // Header với logo
        html.append("<div class='invoice-header'>");
        html.append("<div class='logo-section'>");
        html.append("<h1 class='store-name'>CỬA HÀNG LAPTOP</h1>");
        html.append("<p class='store-info'>Địa chỉ: 123 Đường ABC, Quận XYZ, TP.HCM</p>");
        html.append("<p class='store-info'>Điện thoại: 0123 456 789 | Email: info@laptopstore.com</p>");
        html.append("</div>");
        html.append("</div>");

        // Tiêu đề hóa đơn
        html.append("<div class='invoice-title'>");
        html.append("<h2>HÓA ĐƠN BÁN HÀNG</h2>");
        html.append("</div>");

        // Thông tin hóa đơn
        html.append("<div class='invoice-info'>");
        html.append("<div class='info-row'>");
        html.append("<span class='info-label'>Mã hóa đơn:</span>");
        html.append("<span class='info-value'>").append(hoaDon.getMa()).append("</span>");
        html.append("</div>");
        html.append("<div class='info-row'>");
        html.append("<span class='info-label'>Ngày tạo:</span>");
        html.append("<span class='info-value'>").append(ngayTao).append("</span>");
        html.append("</div>");
        if (hoaDon.getTrangThaiThanhToan() != null && hoaDon.getTrangThaiThanhToan() == 1) {
            html.append("<div class='info-row'>");
            html.append("<span class='info-label'>Ngày thanh toán:</span>");
            html.append("<span class='info-value'>").append(ngayThanhToan).append("</span>");
            html.append("</div>");
        }
        html.append("</div>");

        // Thông tin khách hàng
        html.append("<div class='customer-info'>");
        html.append("<h3>Thông tin khách hàng</h3>");
        html.append("<div class='info-row'>");
        html.append("<span class='info-label'>Tên khách hàng:</span>");
        html.append("<span class='info-value'>").append(tenKhachHang).append("</span>");
        html.append("</div>");
        html.append("<div class='info-row'>");
        html.append("<span class='info-label'>Số điện thoại:</span>");
        html.append("<span class='info-value'>").append(sdt).append("</span>");
        html.append("</div>");
        if (hoaDon.getDiaChi() != null && !hoaDon.getDiaChi().isEmpty()) {
            html.append("<div class='info-row'>");
            html.append("<span class='info-label'>Địa chỉ:</span>");
            html.append("<span class='info-value'>").append(hoaDon.getDiaChi()).append("</span>");
            html.append("</div>");
        }
        html.append("</div>");

        // Danh sách sản phẩm
        html.append("<div class='products-section'>");
        html.append("<h3>Danh sách sản phẩm</h3>");
        html.append("<table class='products-table'>");
        html.append("<thead>");
        html.append("<tr>");
        html.append("<th>STT</th>");
        html.append("<th>Tên sản phẩm</th>");
        html.append("<th>Số lượng</th>");
        html.append("<th>Đơn giá</th>");
        html.append("<th>Thành tiền</th>");
        html.append("<th>Serial/IMEI</th>");
        html.append("</tr>");
        html.append("</thead>");
        html.append("<tbody>");

        int stt = 1;
        if (hoaDon.getHoaDonChiTiets() != null) {
            for (HoaDonChiTiet hdct : hoaDon.getHoaDonChiTiets()) {
                String tenSanPham = hdct.getChiTietSanPham() != null 
                        && hdct.getChiTietSanPham().getSanPham() != null
                        ? hdct.getChiTietSanPham().getSanPham().getTenSanPham()
                        : "Sản phẩm";
                
                Integer soLuong = hdct.getSoLuong() != null ? hdct.getSoLuong() : 0;
                BigDecimal donGia = hdct.getDonGia() != null ? hdct.getDonGia() : BigDecimal.ZERO;
                BigDecimal thanhTien = donGia.multiply(new BigDecimal(soLuong));

                // Lấy serial numbers
                List<String> serials = serialMap.getOrDefault(hdct.getId(), List.of());
                String serialText = serials.isEmpty() 
                        ? "N/A" 
                        : String.join(", ", serials);

                html.append("<tr>");
                html.append("<td>").append(stt++).append("</td>");
                html.append("<td>").append(tenSanPham).append("</td>");
                html.append("<td class='text-center'>").append(soLuong).append("</td>");
                html.append("<td class='text-right'>").append(formatCurrency(donGia)).append("</td>");
                html.append("<td class='text-right'>").append(formatCurrency(thanhTien)).append("</td>");
                html.append("<td class='serial-cell'>").append(serialText).append("</td>");
                html.append("</tr>");
            }
        }

        html.append("</tbody>");
        html.append("</table>");
        html.append("</div>");

        // Tổng tiền
        html.append("<div class='total-section'>");
        html.append("<div class='total-row'>");
        html.append("<span class='total-label'>Tổng tiền:</span>");
        html.append("<span class='total-value'>").append(formatCurrency(tongTien)).append("</span>");
        html.append("</div>");
        
        if (tienDuocGiam.compareTo(BigDecimal.ZERO) > 0) {
            html.append("<div class='total-row'>");
            html.append("<span class='total-label'>Giảm giá (").append(tenVoucher).append("):</span>");
            html.append("<span class='total-value discount'>-").append(formatCurrency(tienDuocGiam)).append("</span>");
            html.append("</div>");
        }

        if (soDiemSuDung > 0 && soTienQuyDoi.compareTo(BigDecimal.ZERO) > 0) {
            html.append("<div class='total-row'>");
            html.append("<span class='total-label'>Điểm tích lũy (").append(soDiemSuDung).append(" điểm):</span>");
            html.append("<span class='total-value discount'>-").append(formatCurrency(soTienQuyDoi)).append("</span>");
            html.append("</div>");
        }

        html.append("<div class='total-row final-total'>");
        html.append("<span class='total-label'>Tổng cần trả:</span>");
        html.append("<span class='total-value'>").append(formatCurrency(tongTienSauGiam)).append("</span>");
        html.append("</div>");
        html.append("</div>");

        // Footer
        html.append("<div class='invoice-footer'>");
        html.append("<p class='thank-you'>Cảm ơn quý khách đã mua hàng!</p>");
        html.append("<p class='footer-note'>Hóa đơn này có giá trị pháp lý và được lưu trữ trong hệ thống.</p>");
        html.append("</div>");

        html.append("</div>"); // invoice-container
        html.append("</body>");
        html.append("</html>");

        return html.toString();
    }

    /**
     * CSS cho hóa đơn
     */
    private String getInvoiceCSS() {
        return """
            * {
                margin: 0;
                padding: 0;
                box-sizing: border-box;
            }
            body {
                font-family: 'Arial', 'Helvetica', sans-serif;
                background: #f5f5f5;
                padding: 20px;
            }
            .invoice-container {
                max-width: 800px;
                margin: 0 auto;
                background: white;
                padding: 40px;
                box-shadow: 0 0 10px rgba(0,0,0,0.1);
            }
            .invoice-header {
                text-align: center;
                border-bottom: 3px solid #007bff;
                padding-bottom: 20px;
                margin-bottom: 30px;
            }
            .store-name {
                color: #007bff;
                font-size: 28px;
                margin-bottom: 10px;
            }
            .store-info {
                color: #666;
                font-size: 14px;
                margin: 5px 0;
            }
            .invoice-title {
                text-align: center;
                margin: 30px 0;
            }
            .invoice-title h2 {
                color: #333;
                font-size: 24px;
                text-transform: uppercase;
            }
            .invoice-info, .customer-info {
                margin: 20px 0;
                padding: 15px;
                background: #f9f9f9;
                border-radius: 5px;
            }
            .customer-info h3, .products-section h3 {
                color: #007bff;
                margin-bottom: 15px;
                font-size: 18px;
            }
            .info-row {
                display: flex;
                justify-content: space-between;
                padding: 8px 0;
                border-bottom: 1px solid #eee;
            }
            .info-row:last-child {
                border-bottom: none;
            }
            .info-label {
                font-weight: bold;
                color: #555;
                width: 40%;
            }
            .info-value {
                color: #333;
                width: 60%;
                text-align: right;
            }
            .products-section {
                margin: 30px 0;
            }
            .products-table {
                width: 100%;
                border-collapse: collapse;
                margin-top: 15px;
            }
            .products-table th {
                background: #007bff;
                color: white;
                padding: 12px;
                text-align: left;
                font-weight: bold;
            }
            .products-table td {
                padding: 10px 12px;
                border-bottom: 1px solid #ddd;
            }
            .products-table tbody tr:hover {
                background: #f5f5f5;
            }
            .text-center {
                text-align: center;
            }
            .text-right {
                text-align: right;
            }
            .serial-cell {
                font-size: 11px;
                color: #666;
                max-width: 200px;
                word-break: break-all;
            }
            .total-section {
                margin-top: 30px;
                padding: 20px;
                background: #f9f9f9;
                border-radius: 5px;
            }
            .total-row {
                display: flex;
                justify-content: space-between;
                padding: 10px 0;
                font-size: 16px;
            }
            .total-label {
                font-weight: bold;
                color: #555;
            }
            .total-value {
                font-weight: bold;
                color: #333;
            }
            .total-value.discount {
                color: #dc3545;
            }
            .final-total {
                border-top: 2px solid #007bff;
                margin-top: 10px;
                padding-top: 15px;
                font-size: 20px;
            }
            .final-total .total-label {
                font-size: 20px;
            }
            .final-total .total-value {
                font-size: 24px;
                color: #007bff;
            }
            .invoice-footer {
                margin-top: 40px;
                text-align: center;
                padding-top: 20px;
                border-top: 2px solid #eee;
            }
            .thank-you {
                font-size: 18px;
                color: #007bff;
                font-weight: bold;
                margin-bottom: 10px;
            }
            .footer-note {
                font-size: 12px;
                color: #999;
            }
            @media print {
                body {
                    background: white;
                    padding: 0;
                }
                .invoice-container {
                    box-shadow: none;
                    padding: 20px;
                }
            }
            """;
    }

    /**
     * Format số tiền theo định dạng VNĐ
     */
    private String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            return "0 ₫";
        }
        return String.format("%,d ₫", amount.longValue());
    }
}

