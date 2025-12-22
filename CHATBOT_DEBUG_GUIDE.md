# 🤖 Hướng dẫn Debug Bot AI không trả lời

## ✅ Đã sửa

1. **Thêm log chi tiết ở Backend**: Log tất cả bot responses với flag `isBotMessage`
2. **Thêm log chi tiết ở Frontend**: Log bot messages khi nhận được từ WebSocket
3. **Guaranteed Fallback**: Đảm bảo bot LUÔN trả lời, kể cả khi có lỗi
4. **Đảm bảo `isBotMessage` flag**: Set đúng flag để frontend nhận biết bot message

## 🔍 Cách kiểm tra

### 1. Kiểm tra Backend Logs

Khi khách hàng gửi tin nhắn, bạn sẽ thấy các log sau:

```
🤖 [WebSocket] Triggering chatbot for customer message: [nội dung tin nhắn]
✨ [WebSocket] Attempting Gemini AI response
✅ [WebSocket] Gemini AI responded successfully
🤖 [WebSocket] ✅ Sent bot response to topic: /topic/conversation/{id}, intent: {intent}, isBotMessage: true
```

Nếu Groq fail:
```
⚠️ [WebSocket] Gemini returned null, falling back to ChatbotService
🔄 [WebSocket] Groq null → fallback ChatbotService
```

Nếu cả 2 fail:
```
🔄 [WebSocket] Both Groq and ChatbotService failed, using guaranteed fallback with menu
🤖 [WebSocket] ✅ Sent guaranteed fallback response to topic: /topic/conversation/{id}
```

### 2. Kiểm tra Frontend Console

Mở Developer Console (F12) và xem log:

```
📨 [ChatWidget] Received WebSocket message: {
  id: "...",
  from: "BOT",  // hoặc "STAFF" hoặc "CUSTOMER"
  isBotMessage: true,  // QUAN TRỌNG: phải là true
  content: "...",
  quickReplies: 4
}

🤖 [ChatWidget] Bot message received: {
  id: "...",
  content: "...",
  quickReplies: 4,
  intent: "..."
}
```

### 3. Kiểm tra WebSocket Connection

Trong frontend, kiểm tra:
- Connection status indicator (màu xanh = connected)
- Console log: `✅ WebSocket connected`
- Console log: `✅ Subscribed to conversation: {id}`

### 4. Kiểm tra Groq API

Nếu Groq API fail, kiểm tra:
- API key trong `application.properties`: `groq.api.key`
- API key có hợp lệ không
- Network connection đến Groq API

## 🐛 Các vấn đề thường gặp

### Vấn đề 1: Bot không trả lời

**Nguyên nhân có thể:**
- WebSocket chưa kết nối
- Conversation đã escalated to human
- Groq API key không hợp lệ
- ChatbotService không có intents trong database

**Giải pháp:**
1. Kiểm tra log backend xem bot có được trigger không
2. Kiểm tra frontend console xem có nhận được message không
3. Kiểm tra Groq API key
4. Chạy migration để seed intents: `chatbot_seed_data.sql`

### Vấn đề 2: Bot trả lời nhưng không hiển thị

**Nguyên nhân:**
- Frontend không nhận biết `isBotMessage = true`
- Message bị filter bởi duplicate check

**Giải pháp:**
1. Kiểm tra console log: `isBotMessage: true`
2. Kiểm tra message có trong `messages.value` không
3. Kiểm tra duplicate check logic

### Vấn đề 3: Bot trả lời nhưng không có quick replies

**Nguyên nhân:**
- Bot response không có `quickReplies`
- Frontend không update `currentQuickReplies`

**Giải pháp:**
1. Kiểm tra backend log: `📋 [WebSocket] Bot response includes X quick replies`
2. Kiểm tra frontend log: `✅ [ChatWidget] Updated quick replies: X`
3. Đảm bảo `ChatbotResponse` có `quickReplies`

## 📝 Test Cases

### Test 1: Bot trả lời tin nhắn đơn giản
1. Khách hàng gửi: "Xin chào"
2. Bot phải trả lời với greeting message
3. Kiểm tra log: `🤖 [WebSocket] ✅ Sent bot response`

### Test 2: Bot trả lời với quick replies
1. Khách hàng gửi: "Tôi muốn mua laptop"
2. Bot phải trả lời với quick replies
3. Kiểm tra frontend: Quick replies hiển thị

### Test 3: Guaranteed fallback
1. Tắt Groq API (set `groq.api.enabled=false`)
2. Khách hàng gửi tin nhắn bất kỳ
3. Bot phải trả lời với guaranteed fallback menu
4. Kiểm tra log: `🤖 [WebSocket] ✅ Sent guaranteed fallback response`

## 🔧 Cấu hình

### Groq API
File: `application.properties`
```properties
groq.api.key=${GROQ_API_KEY:your-key-here}
groq.api.model=${GROQ_MODEL:llama-3.1-8b-instant}
groq.api.url=${GROQ_API_URL:https://api.groq.com/openai/v1/chat/completions}
groq.api.enabled=true
```

### Database Intents
Chạy migration để seed intents:
```sql
-- Xem file: src/main/resources/db/migration/chatbot_seed_data.sql
```

## 📞 Liên hệ

Nếu vẫn gặp vấn đề, kiểm tra:
1. Backend logs đầy đủ
2. Frontend console logs
3. WebSocket connection status
4. Database có intents không

