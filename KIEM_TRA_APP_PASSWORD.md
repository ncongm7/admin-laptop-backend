# 🔍 Hướng Dẫn Kiểm Tra App Password Gmail

## ⚠️ LỖI: "Authentication failed" - 535-5.7.8 Username and Password not accepted

Nếu bạn vẫn gặp lỗi này sau khi đã điền App Password, hãy làm theo các bước sau:

## 📋 BƯỚC 1: XÓA APP PASSWORD CŨ VÀ TẠO MỚI

### 1.1. Xóa App Password cũ (nếu có)
1. Vào: https://myaccount.google.com/apppasswords
2. Tìm App Password có tên "Spring Boot App" hoặc tương tự
3. Click **Xóa** (Delete)

### 1.2. Tạo App Password MỚI
1. Vào: https://myaccount.google.com/apppasswords
2. Chọn:
   - **Select app**: Chọn "Mail"
   - **Select device**: Chọn "Other (Custom name)"
   - Nhập tên: `Spring Boot Email`
   - Click **Generate**

3. **SAO CHÉP NGAY** App Password (16 ký tự)
   - ⚠️ **QUAN TRỌNG**: App Password chỉ hiển thị 1 lần!
   - Ví dụ: `abcd efgh ijkl mnop` → Dùng: `abcdefghijklmnop` (BỎ KHOẢNG TRẮNG)

## 📋 BƯỚC 2: KIỂM TRA 2-STEP VERIFICATION

1. Vào: https://myaccount.google.com/security
2. Kiểm tra **2-Step Verification** đã bật chưa
   - Nếu chưa bật: **BẮT BUỘC PHẢI BẬT** trước khi tạo App Password
   - Nếu đã bật: Tiếp tục

## 📋 BƯỚC 3: CẬP NHẬT application.properties

Mở file: `src/main/resources/application.properties`

Tìm dòng:
```properties
spring.mail.password=jigwtqqylfzasbri
```

**THAY THẾ** bằng App Password MỚI vừa tạo (không có khoảng trắng):
```properties
spring.mail.password=YOUR_NEW_APP_PASSWORD_HERE
```

**LƯU Ý:**
- ✅ Không có khoảng trắng
- ✅ Không có dấu gạch ngang
- ✅ Chỉ 16 ký tự chữ và số

## 📋 BƯỚC 4: KIỂM TRA TÀI KHOẢN GMAIL

### 4.1. Kiểm tra tài khoản có bị khóa không
1. Vào: https://accounts.google.com/DisplayUnlockCaptcha
2. Click **Continue** để mở khóa tài khoản (nếu bị khóa)

### 4.2. Kiểm tra "Less secure app access" (KHÔNG CẦN)
- ⚠️ Gmail đã bỏ tính năng này
- Chỉ cần App Password là đủ

## 📋 BƯỚC 5: RESTART SPRING BOOT

Sau khi cập nhật `application.properties`:
1. **Dừng** ứng dụng Spring Boot (Ctrl+C trong terminal)
2. **Khởi động lại**:
   ```bash
   .\mvnw.cmd spring-boot:run
   ```
   Hoặc restart từ IDE

## 📋 BƯỚC 6: TEST LẠI

1. Vào màn hình quản lý phiếu giảm giá
2. Chọn phiếu cá nhân → "KH cá nhân"
3. Chọn khách hàng → "Gửi email"
4. Kiểm tra logs trong console:
   ```
   === BẮT ĐẦU GỬI EMAIL ===
   Đang gửi email đến: longbadfpt@gmail.com
   From email: dellvietlaptopmail@gmail.com
   ...
   ✅ Email đã được gửi thành công đến: ...
   ```

## 🔧 TROUBLESHOOTING

### ❌ Vẫn bị lỗi "Authentication failed"

**Nguyên nhân có thể:**
1. App Password sai hoặc có khoảng trắng
2. App Password đã hết hạn (hiếm, nhưng có thể)
3. Tài khoản Gmail bị khóa tạm thời
4. Cấu hình SMTP không đúng

**Giải pháp:**
1. ✅ Tạo App Password MỚI (xem Bước 1)
2. ✅ Kiểm tra không có khoảng trắng trong `application.properties`
3. ✅ Mở khóa tài khoản: https://accounts.google.com/DisplayUnlockCaptcha
4. ✅ Restart Spring Boot
5. ✅ Kiểm tra logs để xem lỗi chi tiết

### ❌ Lỗi "Connection timeout"

**Nguyên nhân:**
- Firewall/antivirus chặn port 587
- Network không cho phép kết nối SMTP

**Giải pháp:**
1. Tắt tạm thời firewall/antivirus để test
2. Kiểm tra network có chặn port 587 không
3. Thử dùng port 465 (xem cấu hình thay thế bên dưới)

### ❌ Lỗi "SSL handshake failed"

**Nguyên nhân:**
- Cấu hình SSL không đúng

**Giải pháp:**
- Dùng cấu hình port 587 với STARTTLS (đã cấu hình sẵn)

## 🔄 CẤU HÌNH THAY THẾ: Port 465 (SSL)

Nếu port 587 không hoạt động, thử dùng port 465:

```properties
# Mail Configuration - Port 465 với SSL
spring.mail.host=smtp.gmail.com
spring.mail.port=465
spring.mail.username=dellvietlaptopmail@gmail.com
spring.mail.password=YOUR_APP_PASSWORD_HERE
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

**Lưu ý:** Bỏ các dòng `starttls.enable` và `starttls.required` khi dùng port 465.

## 📝 KIỂM TRA LOGS

Sau khi restart, kiểm tra logs để xem:
- ✅ Kết nối SMTP thành công
- ✅ Xác thực thành công
- ✅ Gửi email thành công

Nếu vẫn lỗi, logs sẽ hiển thị chi tiết lỗi để debug.

## 🔗 TÀI LIỆU THAM KHẢO

- [Gmail App Passwords](https://support.google.com/accounts/answer/185833)
- [Gmail SMTP Settings](https://support.google.com/mail/answer/7126229)
- [Troubleshoot Gmail Authentication](https://support.google.com/mail/?p=BadCredentials)

## ⚠️ LƯU Ý BẢO MẬT

- **KHÔNG** commit App Password vào Git
- **KHÔNG** chia sẻ App Password
- Nếu App Password bị lộ, xóa ngay và tạo mới
- Sử dụng environment variables cho production

