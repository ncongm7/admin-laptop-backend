package com.example.backendlaptop.dto.review;

import lombok.Data;
import java.util.UUID;

@Data
public class HelpfulVoteRequest {
    private Boolean isHelpful; // true = helpful, false = not helpful
}
