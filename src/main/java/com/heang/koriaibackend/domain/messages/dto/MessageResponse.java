package com.heang.koriaibackend.domain.messages.dto;

import com.heang.koriaibackend.domain.messages.model.Message;

import java.time.OffsetDateTime;

public record MessageResponse(
        Long id,
        Long conversationId,
        String role,
        String content,
        String corrections,
        Integer tokensUsed,
        OffsetDateTime createdAt
) {
    public static MessageResponse from(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getConversationId(),
                message.getRole(),
                message.getContent(),
                message.getCorrections(),
                message.getTokensUsed(),
                message.getCreatedAt()
        );
    }
}
