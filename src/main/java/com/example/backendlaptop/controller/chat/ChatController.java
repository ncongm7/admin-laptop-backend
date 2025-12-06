package com.example.backendlaptop.controller.chat;

import com.example.backendlaptop.dto.chat.ChatRequest;
import com.example.backendlaptop.dto.chat.ChatResponse;
import com.example.backendlaptop.dto.chat.ConversationResponse;
import com.example.backendlaptop.exception.*;
import com.example.backendlaptop.model.response.ResponseObject;
import com.example.backendlaptop.service.chat.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatService chatService;

    /**
     * Gửi tin nhắn mới
     */
    @PostMapping("/send")
    public ResponseEntity<ResponseObject<ChatResponse>> sendMessage(@Valid @RequestBody ChatRequest request) {
        log.info("📨 Nhận request gửi tin nhắn: khachHangId={}, nhanVienId={}, isFromCustomer={}", 
                request.getKhachHangId(), request.getNhanVienId(), request.getIsFromCustomer());
        
        // Validate message length
        if (request.getNoiDung() != null && request.getNoiDung().length() > 5000) {
            throw new ChatMessageTooLongException(5000, request.getNoiDung().length());
        }
        
        ChatResponse response = chatService.sendMessage(request);
        return ResponseEntity.ok(new ResponseObject<>(response, "Gửi tin nhắn thành công"));
    }

    /**
     * Lấy danh sách tin nhắn trong một conversation
     */
    @GetMapping("/conversation/{conversationId}/messages")
    public ResponseEntity<ResponseObject<List<ChatResponse>>> getMessages(
            @PathVariable UUID conversationId) {
        List<ChatResponse> messages = chatService.getMessagesByConversationId(conversationId);
        return ResponseEntity.ok(new ResponseObject<>(messages, "Lấy danh sách tin nhắn thành công"));
    }

    /**
     * Lấy danh sách conversation của khách hàng
     */
    @GetMapping("/customer/{khachHangId}/conversations")
    public ResponseEntity<ResponseObject<List<ConversationResponse>>> getCustomerConversations(
            @PathVariable UUID khachHangId) {
        List<ConversationResponse> conversations = chatService.getConversationsByKhachHang(khachHangId);
        return ResponseEntity.ok(new ResponseObject<>(conversations, "Lấy danh sách cuộc trò chuyện thành công"));
    }

    /**
     * Lấy danh sách conversation của nhân viên
     */
    @GetMapping("/staff/{nhanVienId}/conversations")
    public ResponseEntity<ResponseObject<List<ConversationResponse>>> getStaffConversations(
            @PathVariable UUID nhanVienId) {
        List<ConversationResponse> conversations = chatService.getConversationsByNhanVien(nhanVienId);
        return ResponseEntity.ok(new ResponseObject<>(conversations, "Lấy danh sách cuộc trò chuyện thành công"));
    }

    /**
     * Lấy tất cả conversation (cho admin)
     */
    @GetMapping("/conversations")
    public ResponseEntity<ResponseObject<List<ConversationResponse>>> getAllConversations() {
        List<ConversationResponse> conversations = chatService.getAllConversations();
        return ResponseEntity.ok(new ResponseObject<>(conversations, "Lấy danh sách cuộc trò chuyện thành công"));
    }

    /**
     * Đánh dấu tin nhắn đã đọc
     */
    @PutMapping("/conversation/{conversationId}/read")
    public ResponseEntity<ResponseObject<Void>> markAsRead(
            @PathVariable UUID conversationId,
            @RequestParam Boolean isFromCustomer) {
        chatService.markAsRead(conversationId, isFromCustomer);
        return ResponseEntity.ok(new ResponseObject<>(null, "Đã đánh dấu đọc"));
    }

    /**
     * Đếm số tin nhắn chưa đọc của khách hàng
     */
    @GetMapping("/customer/{khachHangId}/unread-count")
    public ResponseEntity<ResponseObject<Long>> getUnreadCountByCustomer(
            @PathVariable UUID khachHangId) {
        Long count = chatService.countUnreadByKhachHang(khachHangId);
        return ResponseEntity.ok(new ResponseObject<>(count, "Lấy số tin nhắn chưa đọc thành công"));
    }

    /**
     * Đếm số tin nhắn chưa đọc của nhân viên
     */
    @GetMapping("/staff/{nhanVienId}/unread-count")
    public ResponseEntity<ResponseObject<Long>> getUnreadCountByStaff(
            @PathVariable UUID nhanVienId) {
        Long count = chatService.countUnreadByNhanVien(nhanVienId);
        return ResponseEntity.ok(new ResponseObject<>(count, "Lấy số tin nhắn chưa đọc thành công"));
    }

    /**
     * Tìm hoặc tạo conversation giữa khách hàng và nhân viên
     */
    @GetMapping("/find-conversation")
    public ResponseEntity<ResponseObject<UUID>> findOrCreateConversation(
            @RequestParam UUID khachHangId,
            @RequestParam(required = false) UUID nhanVienId) {
        try {
            UUID conversationId = chatService.findOrCreateConversation(khachHangId, nhanVienId);
            return ResponseEntity.ok(new ResponseObject<>(conversationId, "Tìm conversation thành công"));
        } catch (Exception e) {
            // Log lỗi và trả về null (sẽ tạo conversation mới khi gửi tin nhắn)
            System.err.println("Lỗi khi tìm conversation: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(new ResponseObject<>(null, "Chưa có conversation, sẽ tạo mới khi gửi tin nhắn"));
        }
    }

    // Exception handlers
    @ExceptionHandler(ChatRateLimitExceededException.class)
    public ResponseEntity<ResponseObject<Object>> handleRateLimitExceeded(ChatRateLimitExceededException e) {
        log.warn("Rate limit exceeded: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(new ResponseObject<Object>(false, null, e.getUserMessage()));
    }

    @ExceptionHandler(ChatMessageTooLongException.class)
    public ResponseEntity<ResponseObject<Object>> handleMessageTooLong(ChatMessageTooLongException e) {
        log.warn("Message too long: {} characters (max: {})", e.getActualLength(), e.getMaxLength());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ResponseObject<Object>(false, null, e.getUserMessage()));
    }

    @ExceptionHandler(ChatConversationNotFoundException.class)
    public ResponseEntity<ResponseObject<Object>> handleConversationNotFound(ChatConversationNotFoundException e) {
        log.warn("Conversation not found: {}", e.getConversationId());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ResponseObject<Object>(false, null, e.getUserMessage()));
    }

    @ExceptionHandler(ChatException.class)
    public ResponseEntity<ResponseObject<Object>> handleChatException(ChatException e) {
        log.error("Chat error: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ResponseObject<Object>(false, null, e.getUserMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseObject<Object>> handleGenericException(Exception e) {
        log.error("Unexpected error in chat controller: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseObject<Object>(false, null, "Đã xảy ra lỗi. Vui lòng thử lại sau."));
    }
}

