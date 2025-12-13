package com.example.backendlaptop.service.chat.flow;

import com.example.backendlaptop.dto.chat.ChatbotResponse;
import com.example.backendlaptop.dto.chat.QuickReplyDTO;
import com.example.backendlaptop.entity.ChatSession;
import com.example.backendlaptop.model.ConversationState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class AccountFlowHandler {
    
    public ChatbotResponse handle(ChatSession session, ConversationState state, String userMessage, UUID khachHangId) {
        switch (state) {
            case ACCOUNT_CHECK_LOGIN:
                return handleCheckLogin(session, khachHangId);
            case ACCOUNT_GUIDE:
                return handleGuide(session);
            default:
                return ChatbotResponse.simpleResponse("Bạn cần hỗ trợ về tài khoản?", state.name(), BigDecimal.ONE);
        }
    }
    
    private ChatbotResponse handleCheckLogin(ChatSession session, UUID khachHangId) {
        if (khachHangId == null) {
            List<QuickReplyDTO> loginOptions = Arrays.asList(
                QuickReplyDTO.builder()
                    .replyText("🔓 Đăng nhập")
                    .replyValue("login_redirect")
                    .replyType("ACTION")
                    .icon("🔓")
                    .displayOrder(1)
                    .build(),
                QuickReplyDTO.builder()
                    .replyText("📝 Đăng ký tài khoản")
                    .replyValue("register_redirect")
                    .replyType("ACTION")
                    .icon("📝")
                    .displayOrder(2)
                    .build(),
                QuickReplyDTO.builder()
                    .replyText("💬 Hỗ trợ")
                    .replyValue("ESCALATE")
                    .replyType("ACTION")
                    .icon("💬")
                    .displayOrder(3)
                    .build()
            );
            
            return ChatbotResponse.builder()
                    .responseText(
                        "Để quản lý tài khoản, bạn cần **đăng nhập** trước nhé! 🔐\n\n" +
                        "Với tài khoản, bạn có thể:\n" +
                        "• Theo dõi đơn hàng\n" +
                        "• Quản lý bảo hành\n" +
                        "• Lưu địa chỉ giao hàng\n" +
                        "• Tích điểm thành viên"
                    )
                    .quickReplies(loginOptions)
                    .intentCode("ACCOUNT_REQUIRE_LOGIN")
                    .confidence(BigDecimal.ONE)
                    .shouldSave(false)
                    .shouldEscalate(false)
                    .build();
        }
        
        // User is logged in, show guide
        return handleGuide(session);
    }
    
    private ChatbotResponse handleGuide(ChatSession session) {
        List<QuickReplyDTO> accountOptions = Arrays.asList(
            QuickReplyDTO.builder()
                .replyText("👤 Cập nhật thông tin")
                .replyValue("profile_page")
                .replyType("ACTION")
                .icon("👤")
                .displayOrder(1)
                .build(),
            QuickReplyDTO.builder()
                .replyText("🔑 Đổi mật khẩu")
                .replyValue("change_password")
                .replyType("ACTION")
                .icon("🔑")
                .displayOrder(2)
                .build(),
            QuickReplyDTO.builder()
                .replyText("📍 Quản lý địa chỉ")
                .replyValue("manage_address")
                .replyType("ACTION")
                .icon("📍")
                .displayOrder(3)
                .build(),
            QuickReplyDTO.builder()
                .replyText("⭐ Tích điểm của tôi")
                .replyValue("points_page")
                .replyType("ACTION")
                .icon("⭐")
                .displayOrder(4)
                .build(),
            QuickReplyDTO.builder()
                .replyText("❓ Trợ giúp khác")
                .replyValue("ESCALATE")
                .replyType("ACTION")
                .icon("❓")
                .displayOrder(5)
                .build()
        );
        
        return ChatbotResponse.builder()
                .responseText(
                    "👤 **Quản lý tài khoản**\n\n" +
                    "Bạn muốn thực hiện thao tác nào? 👇\n\n" +
                    "💡 *Tip: Bạn có thể vào trang Profile để quản lý tất cả thông tin!*"
                )
                .quickReplies(accountOptions)
                .intentCode("ACCOUNT_GUIDE")
                .confidence(BigDecimal.ONE)
                .shouldSave(true)
                .shouldEscalate(false)
                .build();
    }
}
