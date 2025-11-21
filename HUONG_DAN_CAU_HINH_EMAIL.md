# HƯỚNG DẪN CẤU HÌNH EMAIL GMAIL

## Lỗi: Connect timed out

Lỗi này xảy ra khi không thể kết nối đến Gmail SMTP server. Có thể do:

1. **Firewall/Network block port 587**
2. **Timeout quá ngắn**
3. **Proxy/VPN chặn kết nối**

## Các bước khắc phục:

### Giải pháp 1: Kiểm tra Firewall/Network
- Đảm bảo port 587 không bị firewall chặn
- Kiểm tra xem có proxy/VPN đang chặn kết nối không
- Thử tắt firewall tạm thời để test

### Giải pháp 2: Thử port 465 (SSL)
Nếu port 587 không hoạt động, thử đổi sang port 465:

```properties
spring.mail.port=465
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.ssl.enable=true
spring.mail.properties.mail.smtp.ssl.required=true
spring.mail.properties.mail.smtp.socketFactory.port=465
spring.mail.properties.mail.smtp.socketFactory.class=javax.net.ssl.SSLSocketFactory
spring.mail.properties.mail.smtp.socketFactory.fallback=false
```

### Giải pháp 3: Kiểm tra App Password
1. Vào https://myaccount.google.com/apppasswords
2. Tạo App Password mới:
   - Chọn "Mail" → "Other (Custom name)" → "Spring Boot"
   - Copy App Password (16 ký tự)
   - **Bỏ tất cả khoảng trắng** khi dán vào `application.properties`
3. Đảm bảo 2-Step Verification đã bật: https://myaccount.google.com/security

### Giải pháp 4: Test kết nối từ máy local
Thử telnet để kiểm tra kết nối:
```bash
telnet smtp.gmail.com 587
```
Nếu không kết nối được, có thể do firewall/network.

### Giải pháp 5: Dùng SMTP khác (nếu Gmail không được)
Có thể dùng:
- SendGrid
- Mailgun
- Amazon SES
- Hoặc SMTP server nội bộ

## Cấu hình hiện tại (Port 587 - STARTTLS):
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=dellvietlaptopmail@gmail.com
spring.mail.password=ueutldqsccodjjcn
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
spring.mail.properties.mail.smtp.connectiontimeout=30000
spring.mail.properties.mail.smtp.timeout=30000
spring.mail.properties.mail.smtp.writetimeout=30000
```

## Lưu ý:
- Timeout đã tăng lên 30 giây (30000ms)
- Nếu vẫn lỗi, thử đổi sang port 465 với SSL
- Kiểm tra firewall/antivirus có chặn kết nối không
