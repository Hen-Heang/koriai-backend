package com.heang.koriaibackend.domain.users.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserProfileRequest(
        @NotBlank @Size(max = 100) String displayName,
        @NotBlank @Size(max = 20) String koreanLevel
) {
}
