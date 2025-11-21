package com.example.backendlaptop.controller.phieugiamgia;

import com.example.backendlaptop.expection.ApiException;
import com.example.backendlaptop.model.response.ResponseObject;
import com.example.backendlaptop.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

/**
 * Controller để test kết nối email
 * Chỉ dùng trong development để kiểm tra cấu hình email
 */
@RestController
@RequestMapping("/api/test-email")
@CrossOrigin(origins = "*")
public class EmailTestController {
    
    @Autowired
    private EmailService emailService;
    
    @Value("${app.mail.from:dellvietlaptopmail@gmail.com}")
    private String fromEmail;
    
    /**
     * Test gửi email đơn giản
     * GET /api/test-email?to=your-email@gmail.com
     */
    @GetMapping("/send")
    public ResponseObject<?> testSendEmail(@RequestParam(required = false) String to) {
        try {
            String testEmail = to != null && !to.isEmpty() ? to : fromEmail;
            String subject = "Test Email từ Spring Boot";
            String htmlContent = """
                <html>
                <body>
                    <h2>Test Email</h2>
                    <p>Đây là email test để kiểm tra cấu hình email.</p>
                    <p>Nếu bạn nhận được email này, cấu hình email đã hoạt động đúng!</p>
                    <p>Thời gian: %s</p>
                </body>
                </html>
                """.formatted(java.time.LocalDateTime.now());
            
            emailService.sendPhieuGiamGiaEmail(testEmail, subject, htmlContent);
            
            return new ResponseObject<>(
                "Email đã được gửi thành công đến: " + testEmail,
                "Test email thành công! Kiểm tra hộp thư của bạn."
            );
        } catch (ApiException e) {
            throw e; // Để GlobalExceptionHandler xử lý
        } catch (Exception e) {
            throw new ApiException("Lỗi khi test email: " + e.getMessage(), "EMAIL_TEST_FAILED");
        }
    }
    
    /**
     * Kiểm tra cấu hình email (không gửi email)
     * GET /api/test-email/check-config
     */
    @GetMapping("/check-config")
    public ResponseObject<?> checkEmailConfig() {
        try {
            // Lấy thông tin từ application.properties
            String configInfo = String.format(
                "Email Configuration:\n" +
                "- From Email: %s\n" +
                "- SMTP Host: smtp.gmail.com\n" +
                "- Port: 587 (STARTTLS)\n" +
                "\n" +
                "⚠️ Lưu ý:\n" +
                "1. Kiểm tra App Password trong application.properties\n" +
                "2. Đảm bảo 2-Step Verification đã bật\n" +
                "3. Xem file KIEM_TRA_APP_PASSWORD.md để biết chi tiết",
                fromEmail
            );
            
            return new ResponseObject<>(configInfo, "Cấu hình email hiện tại");
        } catch (Exception e) {
            throw new ApiException("Lỗi khi kiểm tra cấu hình: " + e.getMessage(), "EMAIL_CONFIG_CHECK_FAILED");
        }
    }
}

