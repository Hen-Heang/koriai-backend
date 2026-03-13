package com.heang.koriaibackend.domain.users.dto;

import com.heang.koriaibackend.domain.users.model.User;

import java.time.OffsetDateTime;

public record UserResponse(
        Long id,
        String email,
        String displayName,
        String koreanLevel,
        String preferredModel,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getKoreanLevel(),
                user.getPreferredModel(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
