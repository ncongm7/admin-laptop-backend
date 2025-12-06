package com.example.backendlaptop.exception;

/**
 * Exception thrown when WebSocket connection fails
 */
public class ChatWebSocketException extends ChatException {
    public ChatWebSocketException(String message) {
        super(
            message,
            "WEBSOCKET_ERROR",
            "Lỗi kết nối. Vui lòng thử lại sau."
        );
    }

    public ChatWebSocketException(String message, Throwable cause) {
        super(
            message,
            "WEBSOCKET_ERROR",
            "Lỗi kết nối. Vui lòng thử lại sau."
        );
        initCause(cause);
    }
}

