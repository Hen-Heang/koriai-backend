package com.heang.koriaibackend.domain.messages.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateMessageRequest(
        @NotNull Long conversationId,
        @NotBlank @Size(max = 20) String role,
        @NotBlank String content,
        String corrections,
        @NotNull Integer tokensUsed
) {
}
