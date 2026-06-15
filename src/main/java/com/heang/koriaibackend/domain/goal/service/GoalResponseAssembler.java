package com.heang.koriaibackend.domain.goal.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heang.koriaibackend.domain.goal.dto.GoalResponse;
import com.heang.koriaibackend.domain.goal.model.Goal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/** Builds GoalResponse, parsing the stored jsonb metadata text into a JSON object. */
@Component
@RequiredArgsConstructor
public class GoalResponseAssembler {

    private final ObjectMapper objectMapper;

    public GoalResponse toResponse(Goal goal) {
        return GoalResponse.of(goal, parseMetadata(goal.getMetadata()));
    }

    public List<GoalResponse> toResponses(List<Goal> goals) {
        return goals.stream().map(this::toResponse).toList();
    }

    private JsonNode parseMetadata(String text) {
        if (text == null || text.isBlank()) {
            return objectMapper.createObjectNode();
        }
        try {
            return objectMapper.readTree(text);
        } catch (Exception e) {
            return objectMapper.createObjectNode();
        }
    }
}
