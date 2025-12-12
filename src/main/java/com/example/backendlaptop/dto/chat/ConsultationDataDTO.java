package com.example.backendlaptop.dto.chat;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO cho consultation data từ frontend
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationDataDTO {
    
    /**
     * Mục đích sử dụng (có thể là single hoặc multiple)
     * Ví dụ: "Học tập", "Gaming", "Làm việc văn phòng", "Đồ họa/Video", "Lập trình"
     */
    private List<String> purposes;
    
    /**
     * Ngân sách (VNĐ)
     */
    @Min(value = 0, message = "Ngân sách phải >= 0")
    private Long budget;
    
    /**
     * Tính năng quan trọng (multi-select)
     * Ví dụ: "CPU mạnh", "RAM lớn", "Ổ cứng SSD lớn", "Màn hình tốt", "Pin lâu", "Nhẹ", "Giá rẻ"
     */
    private List<String> features;
    
    /**
     * Message từ user (optional)
     */
    private String userMessage;
}

