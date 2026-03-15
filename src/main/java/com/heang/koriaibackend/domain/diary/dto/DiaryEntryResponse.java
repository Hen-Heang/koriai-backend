package com.heang.koriaibackend.domain.diary.dto;

import com.heang.koriaibackend.domain.diary.model.DiaryEntry;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public record DiaryEntryResponse(
        Long id,
        LocalDate entryDate,
        String originalText,
        String correctedText,
        String feedback,
        Integer wordCount,
        String mood,
        List<String> grammarPoints,
        List<DiaryChange> changes,
        OffsetDateTime createdAt
) {
    public static DiaryEntryResponse from(DiaryEntry entry, List<String> grammarPoints, List<DiaryChange> changes) {
        return new DiaryEntryResponse(
                entry.getId(),
                entry.getEntryDate(),
                entry.getOriginalText(),
                entry.getCorrectedText(),
                entry.getFeedback(),
                entry.getWordCount(),
                entry.getMood(),
                grammarPoints,
                changes,
                entry.getCreatedAt()
        );
    }
}
