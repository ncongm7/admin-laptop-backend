package com.example.backendlaptop.service.chat;

import com.example.backendlaptop.entity.ChatIntent;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Intent Match Result
 * Kết quả phân tích intent với confidence score
 */
@Data
@AllArgsConstructor
public class IntentMatch {
    private String intentCode;
    private BigDecimal confidence;
    private ChatIntent intent;
    
    public IntentMatch(String intentCode, double confidence, ChatIntent intent) {
        this.intentCode = intentCode;
        this.confidence = BigDecimal.valueOf(confidence);
        this.intent = intent;
    }
}
