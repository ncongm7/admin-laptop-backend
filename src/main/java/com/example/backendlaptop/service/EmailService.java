// FILE: src/main/java/com/example/backendlaptop/service/EmailService.java
package com.example.backendlaptop.service;

import com.example.backendlaptop.expection.ApiException;
import jakarta.mail.AuthenticationFailedException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
public class EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${app.mail.from:dellvietlaptopmail@gmail.com}")
    private String fromEmail;
    
    @Value("${app.mail.from-name:Dell Viet Laptop}")
    private String fromName;
    
    public void sendPhieuGiamGiaEmail(String toEmail, String subject, String htmlContent) {
        try {
            logger.info("=== BẮT ĐẦU GỬI EMAIL ===");
            logger.info("Đang gửi email đến: {}", toEmail);
            logger.info("From email: {}", fromEmail);
            logger.info("From name: {}", fromName);
            logger.debug("Subject: {}", subject);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail, fromName);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            logger.info("Đang kết nối và gửi email...");
            mailSender.send(message);
            logger.info("✅ Email đã được gửi thành công đến: {}", toEmail);
            logger.info("=== KẾT THÚC GỬI EMAIL ===");
        } catch (MailAuthenticationException e) {
            logger.error("Lỗi xác thực email khi gửi đến {}: {}", toEmail, e.getMessage(), e);
            String errorMessage = "Lỗi xác thực email. Vui lòng kiểm tra:\n" +
                    "1. App Password của Gmail đã được tạo đúng chưa\n" +
                    "2. App Password đã được nhập đúng trong application.properties\n" +
                    "3. Tài khoản Gmail đã bật 2-Step Verification\n" +
                    "4. Xem hướng dẫn tại: https://support.google.com/mail/?p=BadCredentials";
            throw new ApiException(errorMessage, "EMAIL_AUTH_FAILED");
        } catch (AuthenticationFailedException e) {
            logger.error("Lỗi xác thực email khi gửi đến {}: {}", toEmail, e.getMessage(), e);
            String errorMessage = "Lỗi xác thực email. Vui lòng kiểm tra:\n" +
                    "1. App Password của Gmail đã được tạo đúng chưa\n" +
                    "2. App Password đã được nhập đúng trong application.properties\n" +
                    "3. Tài khoản Gmail đã bật 2-Step Verification\n" +
                    "4. Xem hướng dẫn tại: https://support.google.com/mail/?p=BadCredentials";
            throw new ApiException(errorMessage, "EMAIL_AUTH_FAILED");
        } catch (MailException e) {
            logger.error("Lỗi MailException khi gửi email đến {}: {}", toEmail, e.getMessage(), e);
            String errorMessage = "Lỗi khi gửi email: " + e.getMessage();
            if (e.getMessage() != null && e.getMessage().contains("Authentication failed")) {
                errorMessage += "\nVui lòng kiểm tra App Password của Gmail trong application.properties";
            }
            throw new ApiException(errorMessage, "EMAIL_SEND_FAILED");
        } catch (MessagingException | UnsupportedEncodingException e) {
            logger.error("Lỗi MessagingException khi gửi email đến {}: {}", toEmail, e.getMessage(), e);
            throw new ApiException("Lỗi khi gửi email: " + e.getMessage(), "EMAIL_SEND_FAILED");
        } catch (Exception e) {
            logger.error("Lỗi không mong đợi khi gửi email đến {}: {}", toEmail, e.getMessage(), e);
            throw new ApiException("Lỗi không mong đợi khi gửi email: " + e.getMessage(), "EMAIL_SEND_FAILED");
        }
    }
}

