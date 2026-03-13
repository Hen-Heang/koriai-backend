package com.heang.koriaibackend.domain.conversations.dto;

import com.heang.koriaibackend.domain.conversations.model.Conversation;

import java.time.OffsetDateTime;

public record ConversationResponse(
        Long id,
        Long userId,
        Long scenarioId,
        String title,
        String conversationType,
        String modelUsed,
        Integer messageCount,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static ConversationResponse from(Conversation conversation) {
        return new ConversationResponse(
                conversation.getId(),
                conversation.getUserId(),
                conversation.getScenarioId(),
                conversation.getTitle(),
                conversation.getConversationType(),
                conversation.getModelUsed(),
                conversation.getMessageCount(),
                conversation.getCreatedAt(),
                conversation.getUpdatedAt()
        );
    }
}
