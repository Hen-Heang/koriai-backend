package com.heang.koriaibackend.domain.analyzer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heang.koriaibackend.ai.OpenAiService;
import com.heang.koriaibackend.ai.dto.OpenAiResult;
import com.heang.koriaibackend.common.util.PromptTemplates;
import com.heang.koriaibackend.domain.analyzer.dto.AnalysisBreakdownItem;
import com.heang.koriaibackend.domain.analyzer.dto.MessageAnalysisResponse;
import com.heang.koriaibackend.domain.analyzer.dto.SuggestedReply;
import com.heang.koriaibackend.domain.analyzer.mapper.MessageAnalysisMapper;
import com.heang.koriaibackend.domain.analyzer.model.MessageAnalysis;
import com.heang.koriaibackend.domain.usage.service.ApiUsageLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyzerService {

    private final MessageAnalysisMapper messageAnalysisMapper;
    private final OpenAiService openAiService;
    private final ApiUsageLogService apiUsageLogService;
    private final ObjectMapper objectMapper;

    @Value("${openai.model:gpt-5-mini}")
    private String model;

    @Transactional
    public MessageAnalysisResponse analyze(Long userId, String text, String source) {
        String prompt = PromptTemplates.analyzerPrompt(text, source);
        OpenAiResult result = openAiService.generate(prompt, model);

        AnalysisPayload payload = parsePayload(result.content());
        String breakdownJson = toJson(payload.breakdown());
        String repliesJson = toJson(payload.suggestedReplies());

        MessageAnalysis analysis = MessageAnalysis.builder()
                .userId(userId)
                .source(source)
                .originalText(text)
                .literalMeaning(payload.literalMeaning())
                .naturalMeaning(payload.naturalMeaning())
                .businessContext(payload.businessContext())
                .politenessLevel(payload.politenessLevel())
                .tone(payload.tone())
                .breakdown(breakdownJson)
                .suggestedReplies(repliesJson)
                .modelUsed(result.model())
                .build();
        messageAnalysisMapper.insert(analysis);
        apiUsageLogService.log(userId, "ANALYZER", result);

        return MessageAnalysisResponse.from(analysis, payload.breakdown(), payload.suggestedReplies());
    }

    public List<MessageAnalysisResponse> history(Long userId, int limit) {
        return messageAnalysisMapper.findByUserId(userId, limit).stream()
                .map(it -> MessageAnalysisResponse.from(it, parseBreakdown(it.getBreakdown()), parseReplies(it.getSuggestedReplies())))
                .toList();
    }

    private AnalysisPayload parsePayload(String json) {
        try {
            return objectMapper.readValue(json, AnalysisPayload.class);
        } catch (JsonProcessingException e) {
            return new AnalysisPayload(
                    "Could not parse analysis payload",
                    "Could not parse analysis payload",
                    null, null, null,
                    Collections.emptyList(),
                    Collections.emptyList()
            );
        }
    }

    private List<AnalysisBreakdownItem> parseBreakdown(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionType(List.class, AnalysisBreakdownItem.class));
        } catch (JsonProcessingException e) {
            return Collections.emptyList();
        }
    }

    private List<SuggestedReply> parseReplies(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionType(List.class, SuggestedReply.class));
        } catch (JsonProcessingException e) {
            return Collections.emptyList();
        }
    }

    private <T> String toJson(List<T> values) {
        try {
            return objectMapper.writeValueAsString(values == null ? Collections.emptyList() : values);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    private record AnalysisPayload(
            String literalMeaning,
            String naturalMeaning,
            String businessContext,
            String politenessLevel,
            String tone,
            List<AnalysisBreakdownItem> breakdown,
            List<SuggestedReply> suggestedReplies
    ) {
    }
}
