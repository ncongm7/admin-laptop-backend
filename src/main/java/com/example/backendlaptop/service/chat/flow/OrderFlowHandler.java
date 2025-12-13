package com.example.backendlaptop.service.chat.flow;

import com.example.backendlaptop.dto.chat.ChatbotResponse;
import com.example.backendlaptop.dto.chat.QuickReplyDTO;
import com.example.backendlaptop.entity.ChatSession;
import com.example.backendlaptop.entity.HoaDon;
import com.example.backendlaptop.model.ConversationState;
import com.example.backendlaptop.model.TrangThaiHoaDon;
import com.example.backendlaptop.repository.banhang.HoaDonRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderFlowHandler {
    
    private final HoaDonRepository hoaDonRepo;
    private final ObjectMapper objectMapper;
    
    private static final Pattern ORDER_CODE_PATTERN = Pattern.compile("(?:DH|HD|order)[\\s:]*([A-Z0-9]+)", Pattern.CASE_INSENSITIVE);
    
    public ChatbotResponse handle(ChatSession session, ConversationState state, String userMessage, UUID khachHangId) {
        switch (state) {
            case ORDER_REQUEST_CODE:
                return handleRequestCode(session, khachHangId, userMessage);
            case ORDER_SHOW_STATUS:
                return handleShowStatus(session, userMessage);
            case ORDER_ACTIONS:
                return handleActions(session);
            default:
                return ChatbotResponse.simpleResponse("Bạn muốn kiểm tra đơn hàng nào?", state.name(), BigDecimal.ONE);
        }
    }
    
    private ChatbotResponse handleRequestCode(ChatSession session, UUID khachHangId, String userMessage) {
        // If user is logged in, show recent orders
        if (khachHangId != null) {
            List<HoaDon> recentOrders = hoaDonRepo.findByIdKhachHang_IdOrderByNgayTaoDesc(khachHangId, PageRequest.of(0, 3))
                .getContent();
            
            if (!recentOrders.isEmpty()) {
                List<QuickReplyDTO> orderOptions = new ArrayList<>();
                StringBuilder response = new StringBuilder("📦 Mình tìm thấy **");
                response.append(recentOrders.size()).append(" đơn hàng** gần nhất của bạn:\n\n");
                
                int index = 1;
                for (HoaDon order : recentOrders) {
                    String status = getStatusLabel(order.getTrangThai());
                    String date = formatDate(order.getNgayTao());
                    
                    response.append(String.format("%d. **%s** (%s)\n", index, order.getMa(), date));
                    response.append(String.format("   • %s\n", status));
                    response.append(String.format("   • %s VNĐ\n\n", formatPrice(order.getTongTienSauGiam())));
                    
                    orderOptions.add(QuickReplyDTO.builder()
                        .replyText(String.format("%s - %s", order.getMa(), status))
                        .replyValue(order.getMa())
                        .replyType("ORDER_CODE")
                        .displayOrder(index)
                        .build());
                    
                    index++;
                }
                
                orderOptions.add(QuickReplyDTO.builder()
                    .replyText("✏️ Nhập mã khác")
                    .replyValue("INPUT_CODE")
                    .replyType("ACTION")
                    .icon("✏️")
                    .displayOrder(99)
                    .build());
                
                response.append("Bạn muốn xem đơn nào? 👆");
                
                return ChatbotResponse.builder()
                        .responseText(response.toString())
                        .quickReplies(orderOptions)
                        .intentCode("ORDER_REQUEST_CODE")
                        .confidence(BigDecimal.ONE)
                        .shouldSave(true)
                        .shouldEscalate(false)
                        .build();
            }
        }
        
        // No orders or not logged in - request code
        return ChatbotResponse.builder()
                .responseText(
                    "Vui lòng cung cấp **mã đơn hàng** để mình tra cứu giúp bạn nhé! 📦\n\n" +
                    "Ví dụ: HD123456789"
                )
                .quickReplies(Collections.emptyList())
                .intentCode("ORDER_REQUEST_CODE")
                .confidence(BigDecimal.ONE)
                .shouldSave(true)
                .shouldEscalate(false)
                .build();
    }
    
    private ChatbotResponse handleShowStatus(ChatSession session, String userMessage) {
        String orderCode = extractOrderCode(session, userMessage);
        
        if (orderCode == null || orderCode.trim().isEmpty()) {
            return ChatbotResponse.simpleResponse(
                "Mình chưa nhận được mã đơn hàng. Vui lòng nhập lại nhé!",
                "NO_ORDER_CODE",
                BigDecimal.valueOf(0.5)
            );
        }
        
        Optional<HoaDon> orderOpt = hoaDonRepo.findByMa(orderCode);
        
        if (orderOpt.isEmpty()) {
            return ChatbotResponse.builder()
                    .responseText(
                        String.format("❌ Mình không tìm thấy đơn hàng với mã **%s**\n\n" +
                                     "Bạn vui lòng kiểm tra lại mã đơn hàng nhé!", orderCode)
                    )
                    .quickReplies(Arrays.asList(
                        QuickReplyDTO.builder()
                            .replyText("🔄 Thử lại")
                            .replyValue("ORDER")
                            .replyType("INTENT")
                            .icon("🔄")
                            .build(),
                        QuickReplyDTO.builder()
                            .replyText("💬 Liên hệ hỗ trợ")
                            .replyValue("ESCALATE")
                            .replyType("ACTION")
                            .icon("💬")
                            .build()
                    ))
                    .intentCode("ORDER_NOT_FOUND")
                    .confidence(BigDecimal.valueOf(0.8))
                    .shouldSave(false)
                    .shouldEscalate(false)
                    .build();
        }
        
        HoaDon order = orderOpt.get();
        saveOrderToProgress(session, order);
        
        String status = getStatusLabel(order.getTrangThai());
        String statusEmoji = getStatusEmoji(order.getTrangThai());
        String statusMessage = getStatusMessage(order.getTrangThai());
        
        StringBuilder response = new StringBuilder();
        response.append(String.format("📦 **Đơn hàng %s**\n\n", order.getMa()));
        response.append(String.format("%s **Trạng thái:** %s\n\n", statusEmoji, status));
        response.append(String.format("📅 **Ngày đặt:** %s\n", formatDate(order.getNgayTao())));
        response.append(String.format("💰 **Tổng tiền:** %s VNĐ\n\n", formatPrice(order.getTongTienSauGiam())));
        
        if (statusMessage != null) {
            response.append(statusMessage);
        }
        
        // Quick replies based on status
        List<QuickReplyDTO> actions = getOrderActions(order);
        
        return ChatbotResponse.builder()
                .responseText(response.toString())
                .quickReplies(actions)
                .intentCode("ORDER_SHOW_STATUS")
                .confidence(BigDecimal.ONE)
                .shouldSave(true)
                .shouldEscalate(false)
                .build();
    }
    
    private ChatbotResponse handleActions(ChatSession session) {
        HoaDon order = getOrderFromProgress(session);
        
        if (order == null) {
            return ChatbotResponse.simpleResponse(
                "Bạn có cần hỗ trợ gì thêm về đơn hàng không?",
                "ORDER_ACTIONS",
                BigDecimal.ONE
            );
        }
        
        List<QuickReplyDTO> moreActions = Arrays.asList(
            QuickReplyDTO.builder()
                .replyText("🔍 Kiểm tra đơn khác")
                .replyValue("ORDER")
                .replyType("INTENT")
                .icon("🔍")
                .displayOrder(1)
                .build(),
            QuickReplyDTO.builder()
                .replyText("💬 Tư vấn nhân viên")
                .replyValue("ESCALATE")
                .replyType("ACTION")
                .icon("💬")
                .displayOrder(2)
                .build(),
            QuickReplyDTO.builder()
                .replyText("🏠 Menu chính")
                .replyValue("GREETING")
                .replyType("INTENT")
                .icon("🏠")
                .displayOrder(3)
                .build()
        );
        
        return ChatbotResponse.builder()
                .responseText("Bạn cần hỗ trợ gì thêm không? 😊")
                .quickReplies(moreActions)
                .intentCode("ORDER_ACTIONS")
                .confidence(BigDecimal.ONE)
                .shouldSave(false)
                .shouldEscalate(false)
                .build();
    }
    
    // Helper Methods
    
    private String extractOrderCode(ChatSession session, String userMessage) {
        // Try from message
        if (userMessage != null) {
            Matcher matcher = ORDER_CODE_PATTERN.matcher(userMessage);
            if (matcher.find()) {
                return matcher.group(1);
            }
            
            // Check if message itself is a code (starts with HD/DH)
            String msg = userMessage.trim().toUpperCase();
            if (msg.startsWith("HD") || msg.startsWith("DH")) {
                return msg;
            }
        }
        
        // Try from progress
        try {
            if (session.getProgressData() != null) {
                Map<String, Object> progress = objectMapper.readValue(
                    session.getProgressData(),
                    new TypeReference<Map<String, Object>>() {}
                );
                return (String) progress.get("order_code");
            }
        } catch (Exception e) {
            log.debug("Could not extract order code from progress", e);
        }
        
        return null;
    }
    
    private void saveOrderToProgress(ChatSession session, HoaDon order) {
        try {
            Map<String, Object> progress = new HashMap<>();
            progress.put("order_code", order.getMa());
            progress.put("order_id", order.getId().toString());
            progress.put("order_status", order.getTrangThai().name());
            progress.put("timestamp", System.currentTimeMillis());
            
            session.setProgressData(objectMapper.writeValueAsString(progress));
        } catch (Exception e) {
            log.error("Error saving order to progress", e);
        }
    }
    
    private HoaDon getOrderFromProgress(ChatSession session) {
        try {
            if (session.getProgressData() != null) {
                Map<String, Object> progress = objectMapper.readValue(
                    session.getProgressData(),
                    new TypeReference<Map<String, Object>>() {}
                );
                
                String orderCode = (String) progress.get("order_code");
                if (orderCode != null) {
                    return hoaDonRepo.findByMa(orderCode).orElse(null);
                }
            }
        } catch (Exception e) {
            log.debug("Could not get order from progress", e);
        }
        
        return null;
    }
    
    private List<QuickReplyDTO> getOrderActions(HoaDon order) {
        List<QuickReplyDTO> actions = new ArrayList<>();
        
        switch (order.getTrangThai()) {
            case CHO_THANH_TOAN:
                actions.add(QuickReplyDTO.builder()
                    .replyText("💳 Thanh toán ngay")
                    .replyValue(String.format("pay_order|%s", order.getId()))
                    .replyType("ACTION")
                    .icon("💳")
                    .displayOrder(1)
                    .build());
                actions.add(QuickReplyDTO.builder()
                    .replyText("❌ Hủy đơn")
                    .replyValue(String.format("cancel_order|%s", order.getId()))
                    .replyType("ACTION")
                    .icon("❌")
                    .displayOrder(2)
                    .build());
                break;
                
            case DA_THANH_TOAN:
            case DANG_GIAO:
                actions.add(QuickReplyDTO.builder()
                    .replyText("📞 Liên hệ shipper")
                    .replyValue("ESCALATE")
                    .replyType("ACTION")
                    .icon("📞")
                    .displayOrder(1)
                    .build());
                actions.add(QuickReplyDTO.builder()
                    .replyText("📱 Chi tiết đơn hàng")
                    .replyValue(String.format("view_order|%s", order.getId()))
                    .replyType("ACTION")
                    .icon("📱")
                    .displayOrder(2)
                    .build());
                break;
                
            case HOAN_THANH:
                actions.add(QuickReplyDTO.builder()
                    .replyText("⭐ Đánh giá")
                    .replyValue(String.format("review_order|%s", order.getId()))
                    .replyType("ACTION")
                    .icon("⭐")
                    .displayOrder(1)
                    .build());
                actions.add(QuickReplyDTO.builder()
                    .replyText("🛠️ Bảo hành")
                    .replyValue("WARRANTY")
                    .replyType("INTENT")
                    .icon("🛠️")
                    .displayOrder(2)
                    .build());
                break;
        }
        
        // Always add these
        actions.add(QuickReplyDTO.builder()
            .replyText("💬 Hỗ trợ")
            .replyValue("ESCALATE")
            .replyType("ACTION")
            .icon("💬")
            .displayOrder(99)
            .build());
        
        return actions;
    }
    
    private String getStatusLabel(TrangThaiHoaDon status) {
        if (status == null) return "Không xác định";
        
        switch (status) {
            case CHO_THANH_TOAN: return "Chờ thanh toán";
            case DA_THANH_TOAN: return "Đã thanh toán";
            case DANG_GIAO: return "Đang giao hàng";
            case HOAN_THANH: return "Hoàn thành";
            case DA_HUY: return "Đã hủy";
            default: return status.toString();
        }
    }
    
    private String getStatusEmoji(TrangThaiHoaDon status) {
        if (status == null) return "❓";
        
        switch (status) {
            case CHO_THANH_TOAN: return "⏳";
            case DA_THANH_TOAN: return "✅";
            case DANG_GIAO: return "🚚";
            case HOAN_THANH: return "🎉";
            case DA_HUY: return "❌";
            default: return "📦";
        }
    }
    
    private String getStatusMessage(TrangThaiHoaDon status) {
        if (status == null) return null;
        
        switch (status) {
            case CHO_THANH_TOAN:
                return "💡 Bạn vui lònghoàn tất thanh toán để chúng mình xử lý đơn hàng nhé!";
            case DA_THANH_TOAN:
                return "✨ Đơn hàng đang được chuẩn bị. Mình sẽ thông báo khi bắt đầu giao!";
            case DANG_GIAO:
                return "🚚 Đơn hàng đang trên đường đến bạn!";
            case HOAN_THANH:
                return "🎉 Cảm ơn bạn đã mua hàng! Đừng quên đánh giá sản phẩm nhé!";
            case DA_HUY:
                return "Đơn hàng đã bị hủy. Nếu có thắc mắc, vui lòng liên hệ hỗ trợ!";
            default:
                return null;
        }
    }
    
    private String formatDate(Instant instant) {
        if (instant == null) return "N/A";
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            .withZone(ZoneId.of("Asia/Ho_Chi_Minh"));
        
        return formatter.format(instant);
    }
    
    private String formatPrice(BigDecimal price) {
        if (price == null) return "0";
        
        return String.format("%,d", price.longValue()).replace(",", ".");
    }
}
