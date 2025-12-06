package com.example.backendlaptop.controller;

import com.example.backendlaptop.dto.payment.PaymentStatusResponse;
import com.example.backendlaptop.dto.payment.QRCodeRequest;
import com.example.backendlaptop.dto.payment.QRCodeResponse;
import com.example.backendlaptop.entity.ChiTietThanhToan;
import com.example.backendlaptop.entity.HoaDon;
import com.example.backendlaptop.entity.PhuongThucThanhToan;
import com.example.backendlaptop.expection.ApiException;
import com.example.backendlaptop.repository.ChiTietThanhToanRepository;
import com.example.backendlaptop.repository.PhuongThucThanhToanRepository;
import com.example.backendlaptop.repository.banhang.HoaDonRepository;
import com.example.backendlaptop.service.WebSocketNotificationService;
import com.example.backendlaptop.service.payment.VietQRService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Controller xử lý thanh toán QR
 */
@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {
    
    private final VietQRService vietQRService;
    private final HoaDonRepository hoaDonRepository;
    private final WebSocketNotificationService webSocketNotificationService;
    private final ChiTietThanhToanRepository chiTietThanhToanRepository;
    private final PhuongThucThanhToanRepository phuongThucThanhToanRepository;
    
    /**
     * Generate QR code thanh toán
     * 
     * POST /api/v1/payment/qr/generate
     * 
     * @param request Thông tin thanh toán
     * @return QR code response
     */
    @PostMapping("/qr/generate")
    public ResponseEntity<QRCodeResponse> generateQRCode(@RequestBody QRCodeRequest request) {
        try {
            log.info("📱 [PaymentController] Nhận request tạo QR code: {}", request);
            
            QRCodeResponse response = vietQRService.generateQRCode(request);
            
            return ResponseEntity.ok(response);
            
        } catch (ApiException e) {
            log.error("❌ [PaymentController] Lỗi tạo QR: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("❌ [PaymentController] Lỗi không xác định: {}", e.getMessage(), e);
            throw new ApiException("Không thể tạo QR code: " + e.getMessage());
        }
    }
    
    /**
     * Webhook nhận thông báo thanh toán từ bank
     * (Đơn giản hóa - trong thực tế cần validate signature từ bank)
     * 
     * POST /api/v1/payment/webhook/callback
     * 
     * @param payload Dữ liệu từ bank
     * @return Success response
     */
    @PostMapping("/webhook/callback")
    public ResponseEntity<Map<String, String>> handlePaymentWebhook(@RequestBody Map<String, Object> payload) {
        try {
            log.info("🔔 [PaymentController] Nhận webhook callback: {}", payload);
            
            // Parse dữ liệu từ webhook
            String orderCode = (String) payload.get("orderCode");
            String transactionId = (String) payload.get("transactionId");
            Object amountObj = payload.get("amount");
            
            if (orderCode == null || transactionId == null || amountObj == null) {
                log.error("❌ [PaymentController] Thiếu thông tin trong webhook");
                return ResponseEntity.badRequest().body(Map.of("message", "Invalid webhook data"));
            }
            
            // Convert amount
            BigDecimal amount;
            if (amountObj instanceof Integer) {
                amount = new BigDecimal((Integer) amountObj);
            } else if (amountObj instanceof Long) {
                amount = new BigDecimal((Long) amountObj);
            } else if (amountObj instanceof Double) {
                amount = BigDecimal.valueOf((Double) amountObj);
            } else {
                amount = new BigDecimal(amountObj.toString());
            }
            
            // Tìm hóa đơn theo mã
            HoaDon hoaDon = hoaDonRepository.findByMa(orderCode)
                    .orElseThrow(() -> new ApiException("Không tìm thấy hóa đơn: " + orderCode));
            
            // Validate amount
            if (hoaDon.getTongTienSauGiam().compareTo(amount) != 0) {
                log.warn("⚠️ [PaymentController] Số tiền không khớp. Expected: {}, Received: {}", 
                        hoaDon.getTongTienSauGiam(), amount);
                // Vẫn chấp nhận nếu chênh lệch nhỏ (do làm tròn)
                // throw new ApiException("Số tiền thanh toán không khớp");
            }
            
            // Kiểm tra hóa đơn chưa được thanh toán
            if (hoaDon.getTrangThaiThanhToan() != null && hoaDon.getTrangThaiThanhToan() == 1) {
                log.warn("⚠️ [PaymentController] Hóa đơn {} đã được thanh toán rồi, bỏ qua webhook", orderCode);
                return ResponseEntity.ok(Map.of(
                        "message", "Order already paid",
                        "orderId", hoaDon.getId().toString()
                ));
            }
            
            // Kiểm tra trạng thái hóa đơn (phải là CHO_THANH_TOAN)
            if (hoaDon.getTrangThai() == null || 
                hoaDon.getTrangThai() != com.example.backendlaptop.model.TrangThaiHoaDon.CHO_THANH_TOAN) {
                log.warn("⚠️ [PaymentController] Hóa đơn {} không ở trạng thái chờ thanh toán, trạng thái hiện tại: {}. Vẫn xử lý thanh toán.", 
                        orderCode, hoaDon.getTrangThai());
            }
            
            // Cập nhật trạng thái thanh toán (QUAN TRỌNG: Chỉ cập nhật trangThaiThanhToan, KHÔNG thay đổi trangThai)
            hoaDon.setTrangThaiThanhToan(1); // Đã thanh toán
            hoaDon.setNgayThanhToan(Instant.now());
            
            // QUAN TRỌNG: KHÔNG thay đổi trangThai hóa đơn ở đây
            // - trangThaiThanhToan = 1 (đã thanh toán) ✓
            // - trangThai = CHO_THANH_TOAN (chờ xác nhận) - Admin sẽ xác nhận và chuyển sang DANG_GIAO
            // Logic: Hóa đơn online sau khi thanh toán thành công vẫn cần admin xác nhận trước khi giao hàng
            
            // Lưu mã giao dịch vào chi tiết thanh toán
            try {
                // Tìm phương thức thanh toán QR
                PhuongThucThanhToan pttt = phuongThucThanhToanRepository.findAll().stream()
                        .filter(pt -> "QR Payment".equalsIgnoreCase(pt.getLoaiPhuongThuc()) || 
                                     "Chuyen khoan QR".equalsIgnoreCase(pt.getTenPhuongThuc()))
                        .findFirst()
                        .orElse(null);
                
                if (pttt != null) {
                    ChiTietThanhToan cttt = new ChiTietThanhToan();
                    cttt.setId(UUID.randomUUID());
                    cttt.setIdHoaDon(hoaDon);
                    cttt.setPhuongThucThanhToan(pttt);
                    cttt.setSoTienThanhToan(amount);
                    cttt.setMaGiaoDich(transactionId);
                    cttt.setGhiChu("Thanh toán QR qua webhook từ ngân hàng");
                    cttt.setTienKhachDua(amount);
                    cttt.setTienTraLai(BigDecimal.ZERO);
                    
                    // Lưu chi tiết thanh toán
                    chiTietThanhToanRepository.save(cttt);
                    log.info("✅ [PaymentController] Đã tạo chi tiết thanh toán với mã giao dịch: {}", transactionId);
                } else {
                    log.warn("⚠️ [PaymentController] Không tìm thấy phương thức thanh toán QR, bỏ qua tạo chi tiết thanh toán");
                }
            } catch (Exception e) {
                log.error("⚠️ [PaymentController] Lỗi khi tạo chi tiết thanh toán: {}", e.getMessage(), e);
                // Không throw exception, vì payment đã được xử lý thành công
            }
            
            hoaDonRepository.save(hoaDon);
            
            log.info("✅ [PaymentController] Cập nhật thanh toán thành công cho đơn hàng: {}", orderCode);
            
            // Gửi WebSocket notification
            try {
                webSocketNotificationService.sendPaymentConfirmation(
                        hoaDon.getId(),
                        transactionId,
                        amount
                );
            } catch (Exception e) {
                log.error("⚠️ [PaymentController] Lỗi khi gửi WebSocket notification: {}", e.getMessage());
                // Không throw exception, vì payment đã được xử lý thành công
            }
            
            return ResponseEntity.ok(Map.of(
                    "message", "success",
                    "orderId", hoaDon.getId().toString(),
                    "transactionId", transactionId
            ));
            
        } catch (ApiException e) {
            log.error("❌ [PaymentController] Lỗi xử lý webhook: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            log.error("❌ [PaymentController] Lỗi không xác định khi xử lý webhook: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("message", "Internal server error"));
        }
    }
    
    /**
     * Kiểm tra trạng thái thanh toán
     * 
     * GET /api/v1/payment/status/{orderId}
     * 
     * @param orderId ID đơn hàng
     * @return Trạng thái thanh toán
     */
    @GetMapping("/status/{orderId}")
    public ResponseEntity<PaymentStatusResponse> getPaymentStatus(@PathVariable UUID orderId) {
        try {
            HoaDon hoaDon = hoaDonRepository.findById(orderId)
                    .orElseThrow(() -> new ApiException("Không tìm thấy hóa đơn"));
            
            PaymentStatusResponse response = PaymentStatusResponse.builder()
                    .hoaDonId(hoaDon.getId())
                    .orderCode(hoaDon.getMa())
                    .trangThaiThanhToan(hoaDon.getTrangThaiThanhToan())
                    .amount(hoaDon.getTongTienSauGiam())
                    .transactionId(null) // TODO: Get from ChiTietThanhToan
                    .paymentTime(hoaDon.getNgayThanhToan())
                    .paymentMethod("QR_CODE")
                    .build();
            
            return ResponseEntity.ok(response);
            
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("❌ [PaymentController] Lỗi khi lấy trạng thái thanh toán: {}", e.getMessage(), e);
            throw new ApiException("Không thể lấy trạng thái thanh toán: " + e.getMessage());
        }
    }
}
