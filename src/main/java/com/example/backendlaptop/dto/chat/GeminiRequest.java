package com.example.backendlaptop.dto.chat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO cho request body gửi đến Gemini API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeminiRequest {
    
    @JsonProperty("contents")
    private List<Content> contents;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Content {
        @JsonProperty("parts")
        private List<Part> parts;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Part {
        @JsonProperty("text")
        private String text;
    }
    
    /**
     * Helper method để tạo request từ prompt text
     */
    public static GeminiRequest fromPrompt(String prompt) {
        Part part = Part.builder().text(prompt).build();
        Content content = Content.builder()
                .parts(List.of(part))
                .build();
        return GeminiRequest.builder()
                .contents(List.of(content))
                .build();
    }
}

