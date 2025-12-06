package com.example.backendlaptop.exception;

/**
 * Exception thrown when message is too long
 */
public class ChatMessageTooLongException extends ChatException {
    private final int maxLength;
    private final int actualLength;

    public ChatMessageTooLongException(int maxLength, int actualLength) {
        super(
            String.format("Message too long: %d characters (max: %d)", actualLength, maxLength),
            "MESSAGE_TOO_LONG",
            String.format("Tin nhắn quá dài (%d ký tự). Vui lòng rút ngắn xuống tối đa %d ký tự.", actualLength, maxLength)
        );
        this.maxLength = maxLength;
        this.actualLength = actualLength;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public int getActualLength() {
        return actualLength;
    }
}

