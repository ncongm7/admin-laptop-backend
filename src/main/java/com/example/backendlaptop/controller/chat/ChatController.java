package com.example.backendlaptop.controller.chat;

import com.example.backendlaptop.dto.chat.ChatRequest;
import com.example.backendlaptop.dto.chat.ChatResponse;
import com.example.backendlaptop.dto.chat.ConversationResponse;
import com.example.backendlaptop.model.response.ResponseObject;
import com.example.backendlaptop.service.chat.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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
        try {
            System.out.println("📨 Nhận request gửi tin nhắn:");
            System.out.println("  - khachHangId: " + request.getKhachHangId());
            System.out.println("  - nhanVienId: " + request.getNhanVienId());
            System.out.println("  - noiDung: " + request.getNoiDung());
            System.out.println("  - isFromCustomer: " + request.getIsFromCustomer());
            System.out.println("  - conversationId: " + request.getConversationId());
            
            ChatResponse response = chatService.sendMessage(request);
            return ResponseEntity.ok(new ResponseObject<>(response, "Gửi tin nhắn thành công"));
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi gửi tin nhắn: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
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
}

