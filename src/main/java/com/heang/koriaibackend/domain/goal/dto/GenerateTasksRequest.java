package com.heang.koriaibackend.domain.goal.dto;

/**
 * Request to AI-generate tasks for a goal.
 *
 * @param count optional number of tasks to generate (clamped server-side)
 * @param note  optional extra context/instructions from the user
 */
public record GenerateTasksRequest(Integer count, String note) {
}
