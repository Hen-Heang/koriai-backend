package com.heang.koriaibackend.domain.goal.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Snake_case input per the Orbit contract. metadata is a free-form JSON blob. */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record CreateGoalRequest(
        @NotBlank @Size(max = 500) String title,
        String description,
        String targetDate,
        Boolean noDuration,
        String status,
        JsonNode metadata
) {
}
