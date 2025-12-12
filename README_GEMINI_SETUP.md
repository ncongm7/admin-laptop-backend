# Hướng dẫn cấu hình Gemini API

## Tổng quan

Hệ thống chat tư vấn tự động sử dụng Google Gemini API (free tier) để tư vấn sản phẩm laptop cho khách hàng. Gemini AI sẽ phân tích yêu cầu của khách hàng và đề xuất sản phẩm phù hợp dựa trên dữ liệu thực từ database.

## Bước 1: Lấy Gemini API Key

1. Truy cập [Google AI Studio](https://makersuite.google.com/app/apikey) hoặc [Google Cloud Console](https://console.cloud.google.com/)
2. Đăng nhập bằng tài khoản Google của bạn
3. Tạo API key mới:
   - Vào **API & Services** > **Credentials**
   - Click **Create Credentials** > **API Key**
   - Copy API key vừa tạo

## Bước 2: Cấu hình API Key

### Cách 1: Environment Variable (Khuyến nghị)

Thiết lập biến môi trường `GEMINI_API_KEY`:

**Windows (PowerShell):**
```powershell
$env:GEMINI_API_KEY="your-api-key-here"
```

**Windows (Command Prompt):**
```cmd
set GEMINI_API_KEY=your-api-key-here
```

**Linux/Mac:**
```bash
export GEMINI_API_KEY="your-api-key-here"
```

### Cách 2: Trực tiếp trong application.properties

Mở file `back-end-lap-top/src/main/resources/application.properties` và thêm:

```properties
gemini.api.key=your-api-key-here
```

**⚠️ Lưu ý:** Không commit API key vào Git repository. Sử dụng environment variable hoặc file `.env` riêng.

## Bước 3: Kiểm tra cấu hình

File `application.properties` đã có các cấu hình sau:

```properties
# Gemini API Configuration
gemini.api.key=${GEMINI_API_KEY:}
gemini.api.url=https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent
gemini.api.timeout=30000
gemini.api.enabled=true
```

- `gemini.api.key`: API key (lấy từ environment variable `GEMINI_API_KEY` hoặc set trực tiếp)
- `gemini.api.url`: URL endpoint của Gemini API
- `gemini.api.timeout`: Timeout cho HTTP request (30 giây)
- `gemini.api.enabled`: Bật/tắt Gemini API (true/false)

## Bước 4: Test API Key

### Test bằng cURL:

```bash
curl "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=YOUR_API_KEY" \
  -H 'Content-Type: application/json' \
  -d '{
    "contents": [{
      "parts": [{
        "text": "Xin chào"
      }]
    }]
  }'
```

Nếu thành công, bạn sẽ nhận được response JSON với text từ Gemini.

### Test trong ứng dụng:

1. Khởi động backend server
2. Mở frontend và vào trang chat
3. Click nút **"Tư vấn chọn laptop tự động"**
4. Điền thông tin consultation flow
5. Kiểm tra xem có nhận được response từ Gemini không

## Giới hạn Free Tier

Gemini API free tier có các giới hạn sau:

- **60 requests per minute** (RPM)
- **1,500 requests per day** (RPD)
- **32,000 tokens per minute** (TPM)

Nếu vượt quá giới hạn, API sẽ trả về lỗi 429 (Too Many Requests). Hệ thống sẽ tự động fallback về `ChatbotService` khi gặp lỗi.

## Xử lý lỗi

### Lỗi 401 (Unauthorized)
- Kiểm tra API key có đúng không
- Đảm bảo API key chưa bị revoke

### Lỗi 429 (Rate Limit Exceeded)
- Đã vượt quá giới hạn free tier
- Đợi một lúc rồi thử lại
- Hệ thống sẽ tự động fallback về ChatbotService

### Lỗi 500 (Internal Server Error)
- Lỗi từ phía Google
- Thử lại sau vài phút

### Timeout
- Kiểm tra kết nối internet
- Tăng `gemini.api.timeout` trong application.properties nếu cần

## Tắt Gemini API

Nếu muốn tắt Gemini API và chỉ dùng ChatbotService:

```properties
gemini.api.enabled=false
```

Hoặc không set `GEMINI_API_KEY`, hệ thống sẽ tự động fallback về ChatbotService.

## Troubleshooting

### Gemini API không hoạt động

1. Kiểm tra log backend:
   ```
   🤖 [Gemini] Processing consultation request...
   ```

2. Kiểm tra API key:
   ```bash
   echo $GEMINI_API_KEY  # Linux/Mac
   echo %GEMINI_API_KEY% # Windows CMD
   ```

3. Kiểm tra network connection đến Google API

4. Xem log lỗi chi tiết trong console backend

### Fallback về ChatbotService

Nếu Gemini API fail, hệ thống sẽ tự động fallback về ChatbotService (keyword matching). Điều này đảm bảo khách hàng vẫn nhận được tư vấn, dù không thông minh bằng Gemini.

## Bảo mật

- **KHÔNG** commit API key vào Git
- Sử dụng environment variables hoặc secret management
- Rotate API key định kỳ
- Giới hạn IP whitelist nếu có thể (trong Google Cloud Console)

## Tài liệu tham khảo

- [Gemini API Documentation](https://ai.google.dev/docs)
- [Google AI Studio](https://makersuite.google.com/)
- [Gemini API Pricing](https://ai.google.dev/pricing)

## Hỗ trợ

Nếu gặp vấn đề, kiểm tra:
1. Log backend để xem lỗi chi tiết
2. Network connection
3. API key validity
4. Rate limit status

