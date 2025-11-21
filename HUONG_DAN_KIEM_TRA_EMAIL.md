# Hướng Dẫn Kiểm Tra và Cấu Hình Email Gmail

## 🔴 Lỗi "Authentication failed" - Cách Khắc Phục

### Bước 1: Kiểm Tra 2-Step Verification

1. Đăng nhập vào tài khoản Gmail: https://myaccount.google.com/
2. Vào **Security** (Bảo mật)
3. Kiểm tra **2-Step Verification** đã được bật chưa
   - Nếu chưa bật: Bật 2-Step Verification
   - Nếu đã bật: Tiếp tục bước 2

### Bước 2: Tạo App Password

1. Vào trang quản lý App Passwords: https://myaccount.google.com/apppasswords
   - Hoặc: **Security** → **2-Step Verification** → **App passwords**

2. Chọn:
   - **App**: Chọn "Mail" hoặc "Other (Custom name)"
   - **Device**: Chọn "Other (Custom name)" và nhập "Spring Boot App"
   - Click **Generate**

3. **SAO CHÉP** App Password (16 ký tự, không có khoảng trắng)
   - Ví dụ: `abcd efgh ijkl mnop` → Dùng: `abcdefghijklmnop`

### Bước 3: Cập Nhật application.properties

Mở file: `src/main/resources/application.properties`

```properties
# Mail Configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=465
spring.mail.username=dellvietlaptopmail@gmail.com
spring.mail.password=YOUR_APP_PASSWORD_HERE  # ← Dán App Password vào đây (không có khoảng trắng)
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.ssl.enable=true
spring.mail.properties.mail.smtp.ssl.required=true
spring.mail.properties.mail.smtp.connectiontimeout=30000
spring.mail.properties.mail.smtp.timeout=30000
spring.mail.properties.mail.smtp.writetimeout=30000
spring.mail.properties.mail.smtp.socketFactory.port=465
spring.mail.properties.mail.smtp.socketFactory.class=javax.net.ssl.SSLSocketFactory
spring.mail.properties.mail.smtp.socketFactory.fallback=false
app.mail.from=dellvietlaptopmail@gmail.com
app.mail.from-name=Dell Viet Laptop
```

**Lưu ý quan trọng:**
- ✅ Dán App Password **KHÔNG CÓ khoảng trắng**
- ✅ Không dùng mật khẩu Gmail thông thường
- ✅ App Password chỉ hiển thị 1 lần, nếu quên phải tạo mới

### Bước 4: Restart Spring Boot

Sau khi cập nhật `application.properties`:
1. Dừng ứng dụng Spring Boot (Ctrl+C)
2. Khởi động lại: `mvnw spring-boot:run` hoặc chạy lại từ IDE

### Bước 5: Test Gửi Email

1. Vào màn hình quản lý phiếu giảm giá
2. Chọn phiếu cá nhân → Click "KH cá nhân"
3. Chọn khách hàng → Click "Gửi email"
4. Kiểm tra:
   - ✅ Nếu thành công: Hiển thị "Đã gửi email thành công"
   - ❌ Nếu lỗi: Hiển thị message lỗi chi tiết

## 🔧 Troubleshooting

### Lỗi 1: "Authentication failed" vẫn còn

**Nguyên nhân:**
- App Password sai hoặc có khoảng trắng
- Chưa bật 2-Step Verification
- Tài khoản Gmail bị khóa tạm thời

**Giải pháp:**
1. Tạo lại App Password mới
2. Xóa khoảng trắng trong `application.properties`
3. Kiểm tra tài khoản Gmail có bị khóa không: https://accounts.google.com/DisplayUnlockCaptcha

### Lỗi 2: "Connection timeout"

**Nguyên nhân:**
- Firewall chặn port 465
- Network không cho phép kết nối SMTP

**Giải pháp:**
1. Kiểm tra firewall Windows/antivirus
2. Thử dùng port 587 với STARTTLS (xem cấu hình bên dưới)

### Lỗi 3: "SSL handshake failed"

**Nguyên nhân:**
- Cấu hình SSL không đúng

**Giải pháp:**
- Đảm bảo cấu hình đúng như trong `application.properties` ở trên

## 🔄 Cấu Hình Thay Thế: Port 587 (STARTTLS)

Nếu port 465 không hoạt động, thử dùng port 587:

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=dellvietlaptopmail@gmail.com
spring.mail.password=YOUR_APP_PASSWORD_HERE
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
spring.mail.properties.mail.smtp.connectiontimeout=30000
spring.mail.properties.mail.smtp.timeout=30000
spring.mail.properties.mail.smtp.writetimeout=30000
app.mail.from=dellvietlaptopmail@gmail.com
app.mail.from-name=Dell Viet Laptop
```

**Lưu ý:** Bỏ các dòng `ssl.enable`, `ssl.required`, `socketFactory` khi dùng port 587.

## 📝 Kiểm Tra Logs

Kiểm tra logs trong console để xem lỗi chi tiết:

```
[EmailService] Đang gửi email đến: customer@example.com
[EmailService] Lỗi xác thực email khi gửi đến customer@example.com: ...
```

## 🔗 Tài Liệu Tham Khảo

- [Gmail App Passwords](https://support.google.com/accounts/answer/185833)
- [Spring Boot Mail Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/io.html#io.email)
- [Gmail SMTP Settings](https://support.google.com/mail/answer/7126229)

## ⚠️ Lưu Ý Bảo Mật

- **KHÔNG** commit App Password vào Git
- **KHÔNG** chia sẻ App Password
- Nếu App Password bị lộ, xóa ngay và tạo mới
- Sử dụng environment variables cho production

## 🚀 Production Setup

Cho môi trường production, nên sử dụng environment variables:

```properties
spring.mail.password=${GMAIL_APP_PASSWORD}
```

Và set biến môi trường:
```bash
export GMAIL_APP_PASSWORD=your_app_password_here
```

