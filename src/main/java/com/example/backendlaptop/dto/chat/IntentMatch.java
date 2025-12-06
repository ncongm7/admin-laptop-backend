package com.example.backendlaptop.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntentMatch {
    
    private String intentCode;
    private BigDecimal confidence;
    private com.example.backendlaptop.entity.ChatIntent intent;
    
    public IntentMatch(String intentCode, double confidence, com.example.backendlaptop.entity.ChatIntent intent) {
        this.intentCode = intentCode;
        this.confidence = BigDecimal.valueOf(confidence);
        this.intent = intent;
    }
}
