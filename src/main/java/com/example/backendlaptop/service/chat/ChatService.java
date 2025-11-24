package com.example.backendlaptop.service.chat;

import com.example.backendlaptop.dto.chat.ChatRequest;
import com.example.backendlaptop.dto.chat.ChatResponse;
import com.example.backendlaptop.dto.chat.ConversationResponse;
import com.example.backendlaptop.entity.Chat;
import com.example.backendlaptop.entity.KhachHang;
import com.example.backendlaptop.entity.NhanVien;
import com.example.backendlaptop.expection.ApiException;
import com.example.backendlaptop.repository.ChatRepository;
import com.example.backendlaptop.repository.KhachHangRepository;
import com.example.backendlaptop.repository.NhanVienRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final KhachHangRepository khachHangRepository;
    private final NhanVienRepository nhanVienRepository;

    /**
     * Gửi tin nhắn mới
     */
    @Transactional
    public ChatResponse sendMessage(ChatRequest request) {
        // Validate request
        if (request.getKhachHangId() == null) {
            throw new ApiException("ID khách hàng không được để trống", "BAD_REQUEST");
        }
        if (request.getIsFromCustomer() == null) {
            throw new ApiException("isFromCustomer không được để trống", "BAD_REQUEST");
        }
        if (request.getNoiDung() == null || request.getNoiDung().trim().isEmpty()) {
            throw new ApiException("Nội dung tin nhắn không được để trống", "BAD_REQUEST");
        }

        // Validate khách hàng
        System.out.println("🔍 Tìm khách hàng với ID: " + request.getKhachHangId());
        KhachHang khachHang = khachHangRepository.findById(request.getKhachHangId())
                .orElseThrow(() -> {
                    System.err.println("❌ Không tìm thấy khách hàng với ID: " + request.getKhachHangId());
                    return new ApiException("Không tìm thấy khách hàng với ID: " + request.getKhachHangId(), "NOT_FOUND");
                });
        System.out.println("✅ Tìm thấy khách hàng: " + khachHang.getHoTen());

        // Validate nhân viên nếu có
        NhanVien nhanVien = null;
        if (request.getNhanVienId() != null) {
            nhanVien = nhanVienRepository.findById(request.getNhanVienId())
                    .orElseThrow(() -> new ApiException("Không tìm thấy nhân viên", "NOT_FOUND"));
        }

        // Tạo tin nhắn mới
        Chat chat = new Chat();
        chat.setId(UUID.randomUUID());
        chat.setKhachHang(khachHang);
        chat.setNhanVien(nhanVien);
        chat.setNoiDung(request.getNoiDung());
        chat.setIsFromCustomer(request.getIsFromCustomer());
        chat.setMessageType(request.getMessageType() != null ? request.getMessageType() : "text");
        chat.setFileUrl(request.getFileUrl());
        chat.setIsRead(false);

        // Xử lý conversation_id
        UUID conversationId = request.getConversationId();
        if (conversationId == null) {
            // Nếu là tin nhắn đầu tiên, tạo conversation mới (dùng chính id của tin nhắn)
            conversationId = chat.getId();
            System.out.println("🆕 Tạo conversation mới với ID: " + conversationId);
        } else {
            // Kiểm tra xem conversation đã tồn tại chưa (tránh duplicate)
            List<Chat> existingChats = chatRepository.findByConversationIdOrderByNgayPhanHoiAsc(conversationId);
            if (existingChats.isEmpty()) {
                System.out.println("⚠️ Conversation ID được cung cấp nhưng không tìm thấy tin nhắn nào. Tạo conversation mới.");
                conversationId = chat.getId();
            } else {
                System.out.println("✅ Sử dụng conversation hiện có: " + conversationId);
            }
        }
        chat.setConversationId(conversationId);

        // Xử lý reply
        if (request.getReplyToId() != null) {
            Chat replyTo = chatRepository.findById(request.getReplyToId())
                    .orElseThrow(() -> new ApiException("Không tìm thấy tin nhắn được reply", "NOT_FOUND"));
            chat.setReplyTo(replyTo);
        }

        chat = chatRepository.save(chat);

        return mapToResponse(chat);
    }

    /**
     * Lấy danh sách tin nhắn trong một conversation
     */
    public List<ChatResponse> getMessagesByConversationId(UUID conversationId) {
        List<Chat> chats = chatRepository.findByConversationIdOrderByNgayPhanHoiAsc(conversationId);
        return chats.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách conversation của một khách hàng
     */
    public List<ConversationResponse> getConversationsByKhachHang(UUID khachHangId) {
        List<Chat> lastMessages = chatRepository.findLastMessagesByKhachHang(khachHangId);
        return lastMessages.stream()
                .map(chat -> mapToConversationResponse(chat, true))
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách conversation của một nhân viên
     */
    public List<ConversationResponse> getConversationsByNhanVien(UUID nhanVienId) {
        List<Chat> lastMessages = chatRepository.findLastMessagesByNhanVien(nhanVienId);
        return lastMessages.stream()
                .map(chat -> mapToConversationResponse(chat, false))
                .collect(Collectors.toList());
    }

    /**
     * Lấy tất cả conversation (cho admin)
     * Lấy tất cả conversation unique, mỗi conversation lấy tin nhắn cuối cùng
     */
    public List<ConversationResponse> getAllConversations() {
        // Lấy tất cả tin nhắn có conversationId
        List<Chat> allChats = chatRepository.findAll();
        
        // Group by conversationId và lấy tin nhắn cuối cùng của mỗi conversation
        return allChats.stream()
                .filter(chat -> chat.getConversationId() != null && chat.getKhachHang() != null)
                .collect(Collectors.groupingBy(Chat::getConversationId))
                .values()
                .stream()
                .map(conversationChats -> {
                    Chat lastMessage = conversationChats.stream()
                            .max((c1, c2) -> {
                                if (c1.getNgayPhanHoi() == null) return -1;
                                if (c2.getNgayPhanHoi() == null) return 1;
                                return c1.getNgayPhanHoi().compareTo(c2.getNgayPhanHoi());
                            })
                            .orElse(null);
                    if (lastMessage != null) {
                        return mapToConversationResponse(lastMessage, false);
                    }
                    return null;
                })
                .filter(conv -> conv != null)
                .sorted((c1, c2) -> {
                    if (c1.getLastMessageTime() == null) return 1;
                    if (c2.getLastMessageTime() == null) return -1;
                    return c2.getLastMessageTime().compareTo(c1.getLastMessageTime());
                })
                .collect(Collectors.toList());
    }

    /**
     * Đánh dấu tin nhắn đã đọc
     */
    @Transactional
    public void markAsRead(UUID conversationId, Boolean isFromCustomer) {
        List<Chat> unreadMessages = chatRepository.findByConversationIdOrderByNgayPhanHoiAsc(conversationId)
                .stream()
                .filter(chat -> !chat.getIsRead() && chat.getIsFromCustomer().equals(!isFromCustomer))
                .collect(Collectors.toList());

        unreadMessages.forEach(chat -> chat.setIsRead(true));
        chatRepository.saveAll(unreadMessages);
    }

    /**
     * Đếm số tin nhắn chưa đọc của khách hàng
     */
    public Long countUnreadByKhachHang(UUID khachHangId) {
        return chatRepository.countUnreadMessagesByKhachHang(khachHangId);
    }

    /**
     * Đếm số tin nhắn chưa đọc của nhân viên
     */
    public Long countUnreadByNhanVien(UUID nhanVienId) {
        return chatRepository.countUnreadMessagesByNhanVien(nhanVienId);
    }

    /**
     * Tìm conversation giữa khách hàng và nhân viên
     */
    public UUID findOrCreateConversation(UUID khachHangId, UUID nhanVienId) {
        try {
            List<UUID> conversationIds = chatRepository.findConversationIds(khachHangId, nhanVienId);
            if (conversationIds != null && !conversationIds.isEmpty()) {
                return conversationIds.get(0);
            }
            // Tạo conversation mới (sẽ được tạo khi gửi tin nhắn đầu tiên)
            return null;
        } catch (Exception e) {
            // Log lỗi và trả về null để tạo conversation mới
            System.err.println("Lỗi khi tìm conversation: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Map Chat entity sang ChatResponse
     */
    private ChatResponse mapToResponse(Chat chat) {
        ChatResponse response = new ChatResponse();
        response.setId(chat.getId());
        response.setNoiDung(chat.getNoiDung());
        response.setNgayPhanHoi(chat.getNgayPhanHoi());
        response.setIsFromCustomer(chat.getIsFromCustomer());
        response.setIsRead(chat.getIsRead());
        response.setConversationId(chat.getConversationId());
        response.setMessageType(chat.getMessageType());
        response.setFileUrl(chat.getFileUrl());
        response.setCreatedAt(chat.getCreatedAt());
        response.setUpdatedAt(chat.getUpdatedAt());

        if (chat.getKhachHang() != null) {
            response.setKhachHangId(chat.getKhachHang().getId());
            response.setKhachHangTen(chat.getKhachHang().getHoTen());
            // Có thể thêm avatar sau
        }

        if (chat.getNhanVien() != null) {
            response.setNhanVienId(chat.getNhanVien().getId());
            response.setNhanVienTen(chat.getNhanVien().getHoTen());
            // Có thể thêm avatar sau
        }

        if (chat.getReplyTo() != null) {
            response.setReplyToId(chat.getReplyTo().getId());
            response.setReplyTo(mapToResponse(chat.getReplyTo()));
        }

        return response;
    }

    /**
     * Map Chat entity sang ConversationResponse
     */
    private ConversationResponse mapToConversationResponse(Chat lastMessage, Boolean isCustomerView) {
        ConversationResponse response = new ConversationResponse();
        response.setConversationId(lastMessage.getConversationId());
        response.setLastMessage(mapToResponse(lastMessage));
        response.setLastMessageTime(lastMessage.getNgayPhanHoi());

        if (lastMessage.getKhachHang() != null) {
            response.setKhachHangId(lastMessage.getKhachHang().getId());
            response.setKhachHangTen(lastMessage.getKhachHang().getHoTen());
            response.setKhachHangMa(lastMessage.getKhachHang().getMaKhachHang());
        }

        if (lastMessage.getNhanVien() != null) {
            response.setNhanVienId(lastMessage.getNhanVien().getId());
            response.setNhanVienTen(lastMessage.getNhanVien().getHoTen());
        }

        // Đếm số tin nhắn chưa đọc
        if (isCustomerView) {
            response.setUnreadCount(chatRepository.countUnreadMessagesInConversation(
                    lastMessage.getConversationId(), false));
        } else {
            response.setUnreadCount(chatRepository.countUnreadMessagesInConversation(
                    lastMessage.getConversationId(), true));
        }

        return response;
    }
}

