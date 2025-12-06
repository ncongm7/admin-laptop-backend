package com.example.backendlaptop.exception;

import java.util.UUID;

/**
 * Exception thrown when conversation is not found
 */
public class ChatConversationNotFoundException extends ChatException {
    private final UUID conversationId;

    public ChatConversationNotFoundException(UUID conversationId) {
        super(
            String.format("Conversation not found: %s", conversationId),
            "CONVERSATION_NOT_FOUND",
            "Không tìm thấy cuộc trò chuyện. Vui lòng thử lại."
        );
        this.conversationId = conversationId;
    }

    public UUID getConversationId() {
        return conversationId;
    }
}

