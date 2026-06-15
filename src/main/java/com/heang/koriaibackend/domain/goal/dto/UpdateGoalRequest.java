package com.heang.koriaibackend.domain.goal.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.Size;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record UpdateGoalRequest(
        @Size(max = 500) String title,
        String description,
        String targetDate,
//        Boolean noDuration,
//        String status,
        Boolean isPublic,
        JsonNode metadata
) {
}
