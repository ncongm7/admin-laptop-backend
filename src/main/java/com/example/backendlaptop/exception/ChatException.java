package com.example.backendlaptop.exception;

import lombok.Getter;

/**
 * Base exception for chat-related errors
 */
@Getter
public class ChatException extends RuntimeException {
    private final String errorCode;
    private final String userMessage;

    public ChatException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.userMessage = message;
    }

    public ChatException(String message, String errorCode, String userMessage) {
        super(message);
        this.errorCode = errorCode;
        this.userMessage = userMessage;
    }
}

