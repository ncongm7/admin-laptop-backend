package com.example.backendlaptop.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuickReplyDTO {
    
    private Integer id;
    private String intentCode;
    private String replyText;
    private String replyValue;
    private String replyType; // text, url, intent_trigger
    private Integer displayOrder;
    private String icon;
}
