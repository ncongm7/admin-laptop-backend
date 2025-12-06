package com.example.backendlaptop.exception;

/**
 * Exception thrown when rate limit is exceeded
 */
public class ChatRateLimitExceededException extends ChatException {
    private final int remainingMessages;

    public ChatRateLimitExceededException(int remainingMessages) {
        super(
            "Rate limit exceeded",
            "RATE_LIMIT_EXCEEDED",
            String.format("Bạn đã gửi quá nhiều tin nhắn. Vui lòng đợi một chút. Còn lại %d tin nhắn trong phút này.", remainingMessages)
        );
        this.remainingMessages = remainingMessages;
    }

    public int getRemainingMessages() {
        return remainingMessages;
    }
}

