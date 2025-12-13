package com.example.backendlaptop.service.chat.flow;

import com.example.backendlaptop.dto.chat.ChatbotResponse;
import com.example.backendlaptop.dto.chat.QuickReplyDTO;
import com.example.backendlaptop.entity.ChatSession;
import com.example.backendlaptop.entity.Serial;
import com.example.backendlaptop.model.ConversationState;
import com.example.backendlaptop.service.SerialService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class WarrantyFlowHandler {
    
    private final SerialService serialService;
    private final ObjectMapper objectMapper;
    
    public ChatbotResponse handle(ChatSession session, ConversationState state, String userMessage, UUID khachHangId) {
        switch (state) {
            case WARRANTY_CHECK_LOGIN:
                return handleCheckLogin(session, khachHangId);
            case WARRANTY_SELECT_PRODUCT:
                return handleSelectProduct(session, khachHangId);
            case WARRANTY_CHECK_STATUS:
                return handleCheckStatus(session, userMessage);
            case WARRANTY_CREATE_REQUEST:
                return handleCreateRequest(session);
            default:
                return ChatbotResponse.simpleResponse("Bạn cần hỗ trợ bảo hành?", state.name(), BigDecimal.ONE);
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
                    .replyText("💬 Tư vấn nhân viên")
                    .replyValue("ESCALATE")
                    .replyType("ACTION")
                    .icon("💬")
                    .displayOrder(2)
                    .build()
            );
            
            return ChatbotResponse.builder()
                    .responseText(
                        "Để kiểm tra bảo hành, bạn cần **đăng nhập** trước nhé! 🔐\n\n" +
                        "Sau khi đăng nhập, mình sẽ hiển thị:\n" +
                        "• Danh sách sản phẩm đang bảo hành\n" +
                        "• Thời hạn bảo hành còn lại\n" +
                        "• Lịch sử bảo hành"
                    )
                    .quickReplies(loginOptions)
                    .intentCode("WARRANTY_REQUIRE_LOGIN")
                    .confidence(BigDecimal.ONE)
                    .shouldSave(false)
                    .shouldEscalate(false)
                    .build();
        }
        
        // User is logged in, proceed to select product
        return handleSelectProduct(session, khachHangId);
    }
    
    private ChatbotResponse handleSelectProduct(ChatSession session, UUID khachHangId) {
        if (khachHangId == null) {
            return handleCheckLogin(session, null);
        }
        
        // Get user's purchased products (serials)
        List<Serial> userSerials;
        try {
            userSerials = serialService.getSerialsByUserId(khachHangId).stream()
                .map(serialResponse -> {
                    // Convert SerialResponse back to Serial entity
                    // In production, better to have a dedicated method
                    Serial s = new Serial();
                    s.setId(serialResponse.getId());
                    s.setSerialNo(serialResponse.getSerialNo());
                    s.setNgayNhap(serialResponse.getNgayNhap());
                    // Note: We need product info, might need to enhance SerialResponse
                    return s;
                })
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting user serials", e);
            return ChatbotResponse.simpleResponse(
                "Xin lỗi, mình không thể tải danh sách sản phẩm của bạn. Vui lòng thử lại sau!",
                "WARRANTY_LOAD_ERROR",
                BigDecimal.valueOf(0.5)
            );
        }
        
        if (userSerials.isEmpty()) {
            return ChatbotResponse.builder()
                    .responseText(
                        "❌ Mình không tìm thấy sản phẩm nào trong tài khoản của bạn.\n\n" +
                        "Bạn cần mua sản phẩm trước để được hỗ trợ bảo hành nhé!"
                    )
                    .quickReplies(Arrays.asList(
                        QuickReplyDTO.builder()
                            .replyText("🛒 Xem sản phẩm")
                            .replyValue("SALES")
                            .replyType("INTENT")
                            .icon("🛒")
                            .build(),
                        QuickReplyDTO.builder()
                            .replyText("💬 Liên hệ hỗ trợ")
                            .replyValue("ESCALATE")
                            .replyType("ACTION")
                            .icon("💬")
                            .build()
                    ))
                    .intentCode("NO_WARRANTY_PRODUCTS")
                    .confidence(BigDecimal.ONE)
                    .shouldSave(false)
                    .shouldEscalate(false)
                    .build();
        }
        
        // Filter active warranties (within warranty period)
        List<Serial> activeWarranties = userSerials.stream()
            .filter(this::isWithinWarrantyPeriod)
            .limit(5)
            .collect(Collectors.toList());
        
        StringBuilder response = new StringBuilder();
        response.append(String.format("Bạn có **%d sản phẩm** đang trong thời gian bảo hành:\n\n", activeWarranties.size()));
        
        List<QuickReplyDTO> productOptions = new ArrayList<>();
        int index = 1;
        
        for (Serial serial : activeWarranties) {
            String productName = getProductName(serial);
            String warrantyInfo = getWarrantyInfo(serial);
            
            response.append(String.format("%d. **%s**\n", index, productName));
            response.append(String.format("   📋 Serial: %s\n", serial.getSerialNo()));
            response.append(String.format("   ⏰ %s\n\n", warrantyInfo));
            
            productOptions.add(QuickReplyDTO.builder()
                .replyText(String.format("Chọn %s", productName))
                .replyValue(serial.getId().toString())
                .replyType("SERIAL_ID")
                .displayOrder(index)
                .build());
            
            index++;
        }
        
        if (activeWarranties.isEmpty()) {
            response = new StringBuilder("❌ Tất cả sản phẩm của bạn đã hết hạn bảo hành. 😔\n\n");
            response.append("Bạn có thể liên hệ nhân viên để được tư vấn dịch vụ sửa chữa!");
            
            productOptions.add(QuickReplyDTO.builder()
                .replyText("💬 Liên hệ nhân viên")
                .replyValue("ESCALATE")
                .replyType("ACTION")
                .icon("💬")
                .build());
        } else {
            productOptions.add(QuickReplyDTO.builder()
                .replyText("❓ Không có trong danh sách")
                .replyValue("ESCALATE")
                .replyType("ACTION")
                .icon("❓")
                .displayOrder(99)
                .build());
            
            response.append("Bạn muốn kiểm tra bảo hành sản phẩm nào? 👆");
        }
        
        return ChatbotResponse.builder()
                .responseText(response.toString())
                .quickReplies(productOptions)
                .intentCode("WARRANTY_SELECT_PRODUCT")
                .confidence(BigDecimal.ONE)
                .shouldSave(true)
                .shouldEscalate(false)
                .build();
    }
    
    private ChatbotResponse handleCheckStatus(ChatSession session, String userMessage) {
        UUID serialId = extractSerialId(session, userMessage);
        
        if (serialId == null) {
            return ChatbotResponse.simpleResponse(
                "Vui lòng chọn sản phẩm từ danh sách trên nhé!",
                "NO_SERIAL_SELECTED",
                BigDecimal.valueOf(0.5)
            );
        }
        
        // Save selected serial
        saveSelectedSerial(session, serialId);
        
        // Get warranty status (simplified - in production, check actual warranty records)
        String productName = "Sản phẩm"; // Get from serial
        String warrantyStatus = "Còn hiệu lực";
        String expiryDate = "31/12/2024"; // Calculate from purchase date + warranty period
        
        StringBuilder response = new StringBuilder();
        response.append(String.format("🛠️ **Thông tin bảo hành - %s**\n\n", productName));
        response.append(String.format("✅ **Trạng thái:** %s\n", warrantyStatus));
        response.append(String.format("📅 **Hạn bảo hành:** %s\n\n", expiryDate));
        response.append("Bạn muốn:\n");
        
        List<QuickReplyDTO> options = Arrays.asList(
            QuickReplyDTO.builder()
                .replyText("🔧 Tạo yêu cầu bảo hành")
                .replyValue(String.format("create_warranty|%s", serialId))
                .replyType("ACTION")
                .icon("🔧")
                .displayOrder(1)
                .build(),
            QuickReplyDTO.builder()
                .replyText("📜 Xem lịch sử bảo hành")
                .replyValue(String.format("warranty_history|%s", serialId))
                .replyType("ACTION")
                .icon("📜")
                .displayOrder(2)
                .build(),
            QuickReplyDTO.builder()
                .replyText("💬 Tư vấn nhân viên")
                .replyValue("ESCALATE")
                .replyType("ACTION")
                .icon("💬")
                .displayOrder(3)
                .build()
        );
        
        return ChatbotResponse.builder()
                .responseText(response.toString())
                .quickReplies(options)
                .intentCode("WARRANTY_CHECK_STATUS")
                .confidence(BigDecimal.ONE)
                .shouldSave(true)
                .shouldEscalate(false)
                .build();
    }
    
    private ChatbotResponse handleCreateRequest(ChatSession session) {
        UUID serialId = getSelectedSerial(session);
        
        if (serialId == null) {
            return ChatbotResponse.simpleResponse(
                "Không tìm thấy thông tin sản phẩm. Vui lòng thử lại!",
                "NO_SERIAL",
                BigDecimal.valueOf(0.5)
            );
        }
        
        String productName = "Sản phẩm"; // Get from serial
        
        List<QuickReplyDTO> issueTypes = Arrays.asList(
            QuickReplyDTO.builder()
                .replyText("⚡ Lỗi phần cứng")
                .replyValue(String.format("create_warranty|%s|HARDWARE", serialId))
                .replyType("ACTION")
                .icon("⚡")
                .displayOrder(1)
                .build(),
            QuickReplyDTO.builder()
                .replyText("💻 Lỗi phần mềm")
                .replyValue(String.format("create_warranty|%s|SOFTWARE", serialId))
                .replyType("ACTION")
                .icon("💻")
                .displayOrder(2)
                .build(),
            QuickReplyDTO.builder()
                .replyText("🔋 Lỗi pin")
                .replyValue(String.format("create_warranty|%s|BATTERY", serialId))
                .replyType("ACTION")
                .icon("🔋")
                .displayOrder(3)
                .build(),
            QuickReplyDTO.builder()
                .replyText("🖥️ Lỗi màn hình")
                .replyValue(String.format("create_warranty|%s|SCREEN", serialId))
                .replyType("ACTION")
                .icon("🖥️")
                .displayOrder(4)
                .build(),
            QuickReplyDTO.builder()
                .replyText("❓ Khác")
                .replyValue(String.format("create_warranty|%s|OTHER", serialId))
                .replyType("ACTION")
                .icon("❓")
                .displayOrder(5)
                .build()
        );
        
        return ChatbotResponse.builder()
                .responseText(
                    String.format("Bạn muốn tạo yêu cầu bảo hành cho **%s**?\n\n" +
                                 "Vui lòng chọn loại sự cố: 👇", productName)
                )
                .quickReplies(issueTypes)
                .intentCode("WARRANTY_CREATE_REQUEST")
                .confidence(BigDecimal.ONE)
                .shouldSave(true)
                .shouldEscalate(false)
                .build();
    }
    
    // Helper Methods
    
    private boolean isWithinWarrantyPeriod(Serial serial) {
        if (serial.getNgayNhap() == null) return false;
        
        // Assume 12 months warranty
        Instant warrantyExpiry = serial.getNgayNhap().plus(365, ChronoUnit.DAYS);
        return Instant.now().isBefore(warrantyExpiry);
    }
    
    private String getProductName(Serial serial) {
        // In production, get from serial.ctsp.sanPham.tenSanPham
        return "Laptop " + serial.getSerialNo().substring(0, Math.min(6, serial.getSerialNo().length()));
    }
    
    private String getWarrantyInfo(Serial serial) {
        if (serial.getNgayNhap() == null) return "Chưa rõ hạn BH";
        
        Instant warrantyExpiry = serial.getNgayNhap().plus(365, ChronoUnit.DAYS);
        long daysRemaining = ChronoUnit.DAYS.between(Instant.now(), warrantyExpiry);
        
        if (daysRemaining > 30) {
            return String.format("Còn %d ngày", daysRemaining);
        } else if (daysRemaining > 0) {
            return String.format("⚠️ Sắp hết hạn (%d ngày)", daysRemaining);
        } else {
            return "❌ Đã hết hạn";
        }
    }
    
    private UUID extractSerialId(ChatSession session, String userMessage) {
        // Try to parse from message
        try {
            return UUID.fromString(userMessage.trim());
        } catch (Exception e) {
            // Not a UUID
        }
        
        // Try from progress
        return getSelectedSerial(session);
    }
    
    private void saveSelectedSerial(ChatSession session, UUID serialId) {
        try {
            Map<String, Object> progress;
            if (session.getProgressData() != null) {
                progress = objectMapper.readValue(
                    session.getProgressData(),
                    new TypeReference<Map<String, Object>>() {}
                );
            } else {
                progress = new HashMap<>();
            }
            
            progress.put("selected_serial_id", serialId.toString());
            session.setProgressData(objectMapper.writeValueAsString(progress));
        } catch (Exception e) {
            log.error("Error saving selected serial", e);
        }
    }
    
    private UUID getSelectedSerial(ChatSession session) {
        try {
            if (session.getProgressData() != null) {
                Map<String, Object> progress = objectMapper.readValue(
                    session.getProgressData(),
                    new TypeReference<Map<String, Object>>() {}
                );
                
                String serialId = (String) progress.get("selected_serial_id");
                if (serialId != null) {
                    return UUID.fromString(serialId);
                }
            }
        } catch (Exception e) {
            log.debug("Could not get selected serial", e);
        }
        
        return null;
    }
}
