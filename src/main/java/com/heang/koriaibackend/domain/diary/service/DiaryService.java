package com.heang.koriaibackend.domain.diary.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heang.koriaibackend.ai.OpenAiService;
import com.heang.koriaibackend.ai.dto.OpenAiResult;
import com.heang.koriaibackend.common.api.Code;
import com.heang.koriaibackend.common.exception.BusinessException;
import com.heang.koriaibackend.common.util.PromptTemplates;
import com.heang.koriaibackend.domain.diary.dto.DiaryEntryResponse;
import com.heang.koriaibackend.domain.diary.mapper.DiaryEntryMapper;
import com.heang.koriaibackend.domain.diary.model.DiaryEntry;
import com.heang.koriaibackend.domain.usage.service.ApiUsageLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DiaryService {

    private final DiaryEntryMapper diaryEntryMapper;
    private final OpenAiService openAiService;
    private final ApiUsageLogService apiUsageLogService;
    private final ObjectMapper objectMapper;

    @Value("${openai.model:gpt-5-mini}")
    private String model;

    @Transactional
    public DiaryEntryResponse createOrUpdate(Long userId, LocalDate entryDate, String originalText) {
        LocalDate date = entryDate == null ? LocalDate.now() : entryDate;
        String prompt = PromptTemplates.diaryFeedbackPrompt(originalText);
        OpenAiResult result = openAiService.generate(prompt, model);
        DiaryPayload payload = parsePayload(result.content());

        DiaryEntry entry = DiaryEntry.builder()
                .userId(userId)
                .entryDate(date)
                .originalText(originalText)
                .correctedText(payload.correctedText())
                .feedback(payload.feedback())
                .wordCount(countWords(originalText))
                .mood(payload.mood())
                .build();
        diaryEntryMapper.upsert(entry);
        apiUsageLogService.log(userId, "DIARY_FEEDBACK", result);

        return diaryEntryMapper.findByUserIdAndMonth(userId, date.withDayOfMonth(1), date.plusDays(1))
                .stream()
                .filter(it -> it.getEntryDate().equals(date))
                .findFirst()
                .map(DiaryEntryResponse::from)
                .orElseThrow(() -> new BusinessException(Code.SYSTEM_ERROR, "Failed to load diary entry"));
    }

    public List<DiaryEntryResponse> getByMonth(Long userId, String month) {
        YearMonth yearMonth;
        try {
            yearMonth = YearMonth.parse(month);
        } catch (Exception e) {
            throw new BusinessException(Code.BAD_REQUEST, "month must be YYYY-MM");
        }
        LocalDate start = yearMonth.atDay(1);
        LocalDate endExclusive = yearMonth.plusMonths(1).atDay(1);
        return diaryEntryMapper.findByUserIdAndMonth(userId, start, endExclusive).stream()
                .map(DiaryEntryResponse::from)
                .toList();
    }

    private DiaryPayload parsePayload(String json) {
        try {
            return objectMapper.readValue(json, DiaryPayload.class);
        } catch (JsonProcessingException e) {
            return new DiaryPayload(json, "Could not parse diary feedback payload", null);
        }
    }

    private int countWords(String text) {
        String trimmed = text == null ? "" : text.trim();
        if (trimmed.isEmpty()) {
            return 0;
        }
        return trimmed.split("\\s+").length;
    }

    private record DiaryPayload(String correctedText, String feedback, String mood) {
    }
}
