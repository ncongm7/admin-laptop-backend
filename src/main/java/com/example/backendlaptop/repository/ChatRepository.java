package com.example.backendlaptop.repository;

import com.example.backendlaptop.entity.Chat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChatRepository extends JpaRepository<Chat, UUID> {
    
    // Lấy tất cả tin nhắn trong một conversation, sắp xếp theo thời gian (với pagination)
    @Query("SELECT c FROM Chat c LEFT JOIN FETCH c.khachHang LEFT JOIN FETCH c.nhanVien WHERE c.conversationId = :conversationId ORDER BY c.ngayPhanHoi ASC")
    Page<Chat> findByConversationIdOrderByNgayPhanHoiAsc(@Param("conversationId") UUID conversationId, Pageable pageable);
    
    // Lấy tất cả tin nhắn trong một conversation (không pagination - cho backward compatibility)
    @Query("SELECT c FROM Chat c LEFT JOIN FETCH c.khachHang LEFT JOIN FETCH c.nhanVien WHERE c.conversationId = :conversationId ORDER BY c.ngayPhanHoi ASC")
    List<Chat> findByConversationIdOrderByCreatedAtAsc(@Param("conversationId") UUID conversationId);

    // Lấy tin nhắn giữa khách hàng và nhân viên (không có conversation_id)
    @Query("SELECT c FROM Chat c WHERE " +
           "((c.khachHang.id = :khachHangId AND c.nhanVien.id = :nhanVienId) OR " +
           "(c.khachHang.id = :khachHangId AND c.nhanVien.id IS NULL)) " +
           "ORDER BY c.ngayPhanHoi ASC")
    List<Chat> findConversationBetweenCustomerAndStaff(
        @Param("khachHangId") UUID khachHangId,
        @Param("nhanVienId") UUID nhanVienId
    );

    // Lấy danh sách conversation của một khách hàng (lấy tin nhắn cuối cùng của mỗi conversation) - với pagination
    @Query("SELECT c FROM Chat c LEFT JOIN FETCH c.khachHang LEFT JOIN FETCH c.nhanVien WHERE c.id IN (" +
           "SELECT MAX(c2.id) FROM Chat c2 WHERE c2.khachHang.id = :khachHangId " +
           "GROUP BY COALESCE(c2.conversationId, c2.id)" +
           ") ORDER BY c.ngayPhanHoi DESC")
    Page<Chat> findLastMessagesByKhachHang(@Param("khachHangId") UUID khachHangId, Pageable pageable);

    // Lấy danh sách conversation của một nhân viên - với pagination
    @Query("SELECT c FROM Chat c LEFT JOIN FETCH c.khachHang LEFT JOIN FETCH c.nhanVien WHERE c.id IN (" +
           "SELECT MAX(c2.id) FROM Chat c2 WHERE c2.nhanVien.id = :nhanVienId " +
           "GROUP BY COALESCE(c2.conversationId, c2.id)" +
           ") ORDER BY c.ngayPhanHoi DESC")
    Page<Chat> findLastMessagesByNhanVien(@Param("nhanVienId") UUID nhanVienId, Pageable pageable);

    // Đếm số tin nhắn chưa đọc của một khách hàng
    @Query("SELECT COUNT(c) FROM Chat c WHERE c.khachHang.id = :khachHangId AND c.isRead = false AND c.isFromCustomer = false")
    Long countUnreadMessagesByKhachHang(@Param("khachHangId") UUID khachHangId);

    // Đếm số tin nhắn chưa đọc của một nhân viên
    @Query("SELECT COUNT(c) FROM Chat c WHERE c.nhanVien.id = :nhanVienId AND c.isRead = false AND c.isFromCustomer = true")
    Long countUnreadMessagesByNhanVien(@Param("nhanVienId") UUID nhanVienId);

    // Đếm số tin nhắn chưa đọc trong một conversation
    @Query("SELECT COUNT(c) FROM Chat c WHERE c.conversationId = :conversationId AND c.isRead = false AND c.isFromCustomer = :isFromCustomer")
    Long countUnreadMessagesInConversation(
        @Param("conversationId") UUID conversationId,
        @Param("isFromCustomer") Boolean isFromCustomer
    );

    // Lấy tin nhắn cuối cùng của một conversation
    @Query("SELECT c FROM Chat c WHERE c.conversationId = :conversationId ORDER BY c.ngayPhanHoi DESC")
    Page<Chat> findLastMessageByConversationId(@Param("conversationId") UUID conversationId, Pageable pageable);

    // Tìm conversation giữa khách hàng và nhân viên
    @Query("SELECT DISTINCT CASE WHEN c.conversationId IS NOT NULL THEN c.conversationId ELSE c.id END FROM Chat c WHERE " +
           "c.khachHang.id = :khachHangId AND " +
           "(:nhanVienId IS NULL OR c.nhanVien.id = :nhanVienId)")
    List<UUID> findConversationIds(@Param("khachHangId") UUID khachHangId, @Param("nhanVienId") UUID nhanVienId);

}

