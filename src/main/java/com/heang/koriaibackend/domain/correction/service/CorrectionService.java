package com.heang.koriaibackend.domain.correction.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heang.koriaibackend.ai.OpenAiService;
import com.heang.koriaibackend.ai.dto.OpenAiResult;
import com.heang.koriaibackend.common.util.PromptTemplates;
import com.heang.koriaibackend.domain.correction.dto.CorrectionResponse;
import com.heang.koriaibackend.domain.correction.mapper.SentenceCorrectionMapper;
import com.heang.koriaibackend.domain.correction.model.SentenceCorrection;
import com.heang.koriaibackend.domain.usage.service.ApiUsageLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CorrectionService {

    private final SentenceCorrectionMapper sentenceCorrectionMapper;
    private final OpenAiService openAiService;
    private final ApiUsageLogService apiUsageLogService;
    private final ObjectMapper objectMapper;

    @Value("${openai.model:gpt-5-mini}")
    private String model;

    @Transactional
    public CorrectionResponse check(Long userId, String text) {
        String prompt = PromptTemplates.correctionPrompt(text);
        OpenAiResult result = openAiService.generate(prompt, model);

        CorrectionPayload payload = parseCorrectionPayload(result.content(), text);
        String grammarPointsJson = toJson(payload.grammarPoints());

        SentenceCorrection correction = SentenceCorrection.builder()
                .userId(userId)
                .originalText(text)
                .correctedText(payload.correctedText())
                .explanation(payload.explanation())
                .grammarPoints(grammarPointsJson)
                .modelUsed(result.model())
                .build();
        sentenceCorrectionMapper.insert(correction);
        apiUsageLogService.log(userId, "CORRECTION", result);

        return CorrectionResponse.from(correction, payload.grammarPoints());
    }

    public List<CorrectionResponse> history(Long userId, int limit) {
        return sentenceCorrectionMapper.findByUserId(userId, limit).stream()
                .map(it -> CorrectionResponse.from(it, parseStringList(it.getGrammarPoints())))
                .toList();
    }

    private CorrectionPayload parseCorrectionPayload(String json, String originalText) {
        try {
            return objectMapper.readValue(json, CorrectionPayload.class);
        } catch (JsonProcessingException e) {
            return new CorrectionPayload(originalText, "Could not parse correction payload", Collections.emptyList());
        }
    }

    private List<String> parseStringList(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
        } catch (JsonProcessingException e) {
            return Collections.emptyList();
        }
    }

    private String toJson(List<String> values) {
        try {
            return objectMapper.writeValueAsString(values);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    private record CorrectionPayload(String correctedText, String explanation, List<String> grammarPoints) {
    }
}
