package com.heang.koriaibackend.domain.conversations.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateConversationRequest(
        @NotNull Long userId,
        Long scenarioId,
        @NotBlank @Size(max = 200) String title,
        @NotBlank @Size(max = 50) String conversationType,
        @NotBlank @Size(max = 100) String modelUsed
) {
}
