# 🔧 Hướng Dẫn Cấu Hình IPv4 cho JavaMail

## ⚠️ LỖI: "Connection timed out" - Không thể kết nối đến smtp.gmail.com:587

Lỗi này thường do Java đang cố kết nối qua IPv6 nhưng network/firewall chặn.

## 📋 GIẢI PHÁP: Ép Java dùng IPv4

### Cách 1: Cấu hình trong IntelliJ IDEA (Khuyến nghị)

1. Vào **Run** → **Edit Configurations...**
2. Tìm và chọn cấu hình **BackEndLapTopApplication**
3. Trong tab **Configuration**, tìm ô **VM options**
4. Thêm vào:
   ```
   -Djava.net.preferIPv4Stack=true -Djava.net.preferIPv6Addresses=false
   ```
5. Click **Apply** → **OK**
6. **Restart** ứng dụng

### Cách 2: Thêm vào file .vmoptions

1. Tạo file `backendlaptop.vmoptions` trong thư mục gốc của project
2. Thêm nội dung:
   ```
   -Djava.net.preferIPv4Stack=true
   -Djava.net.preferIPv6Addresses=false
   ```
3. Trong IntelliJ, vào **Run** → **Edit Configurations...**
4. Chọn **BackEndLapTopApplication**
5. Trong **VM options**, thêm:
   ```
   @backendlaptop.vmoptions
   ```
6. **Restart** ứng dụng

### Cách 3: Thêm vào Main Class (Code)

Mở file: `src/main/java/com/example/backendlaptop/BackEndLapTopApplication.java`

Thêm vào đầu hàm `main()`:

```java
public static void main(String[] args) {
    // Ép Java dùng IPv4
    System.setProperty("java.net.preferIPv4Stack", "true");
    System.setProperty("java.net.preferIPv6Addresses", "false");
    
    SpringApplication.run(BackEndLapTopApplication.class, args);
}
```

### Cách 4: Chạy từ Command Line

Nếu chạy từ terminal:

```bash
.\mvnw.cmd spring-boot:run -Djava.net.preferIPv4Stack=true -Djava.net.preferIPv6Addresses=false
```

Hoặc:

```bash
java -Djava.net.preferIPv4Stack=true -Djava.net.preferIPv6Addresses=false -jar target/back-end-lap-top.jar
```

## ✅ KIỂM TRA SAU KHI CẤU HÌNH

1. **Restart** Spring Boot application
2. Test gửi email
3. Kiểm tra logs - không còn lỗi "Connection timed out"

## 🔍 TROUBLESHOOTING

### Vẫn bị "Connection timed out"

**Nguyên nhân có thể:**
1. Firewall/antivirus chặn port 587
2. Network/VPN chặn kết nối SMTP
3. ISP chặn port 587

**Giải pháp:**
1. Tắt tạm thời firewall/antivirus để test
2. Kiểm tra network có chặn port 587 không
3. Thử dùng VPN khác
4. Thử dùng port 465 (SSL) thay vì 587

### Thử port 465 (SSL)

Nếu port 587 vẫn không hoạt động, thử port 465:

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=465
spring.mail.username=dellvietlaptopmail@gmail.com
spring.mail.password=jigwtqqylfzasbri
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.ssl.enable=true
spring.mail.properties.mail.smtp.ssl.required=true
spring.mail.properties.mail.smtp.connectiontimeout=30000
spring.mail.properties.mail.smtp.timeout=30000
spring.mail.properties.mail.smtp.writetimeout=30000
app.mail.from=dellvietlaptopmail@gmail.com
app.mail.from-name=Dell Viet Laptop
```

## 📝 LƯU Ý

- Cấu hình IPv4 chỉ cần làm 1 lần
- Sau khi cấu hình, restart application
- Nếu vẫn lỗi, kiểm tra firewall/network

