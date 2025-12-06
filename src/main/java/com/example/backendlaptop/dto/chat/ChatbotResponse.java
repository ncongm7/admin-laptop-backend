package com.example.backendlaptop.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotResponse {
    
    private String responseText;
    private String intentCode;
    private BigDecimal confidence;
    private List<QuickReplyDTO> quickReplies;
    private Boolean shouldSave;
    private Boolean shouldEscalate;
    private String escalationReason;
    
    // Helper để tạo response nhanh
    public static ChatbotResponse simpleResponse(String text, String intentCode, BigDecimal confidence) {
        return ChatbotResponse.builder()
                .responseText(text)
                .intentCode(intentCode)
                .confidence(confidence)
                .shouldSave(true)
                .shouldEscalate(false)
                .build();
    }
    
    public static ChatbotResponse escalateResponse(String text, String reason) {
        return ChatbotResponse.builder()
                .responseText(text)
                .shouldEscalate(true)
                .escalationReason(reason)
                .shouldSave(true)
                .build();
    }
}
