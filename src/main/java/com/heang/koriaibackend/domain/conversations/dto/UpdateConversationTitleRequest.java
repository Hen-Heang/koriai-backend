package com.heang.koriaibackend.domain.conversations.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateConversationTitleRequest(
        @NotBlank @Size(max = 200) String title
) {
}
