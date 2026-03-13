package com.heang.koriaibackend.domain.diary.dto;

import com.heang.koriaibackend.domain.diary.model.DiaryEntry;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record DiaryEntryResponse(
        Long id,
        LocalDate entryDate,
        String originalText,
        String correctedText,
        String feedback,
        Integer wordCount,
        String mood,
        OffsetDateTime createdAt
) {
    public static DiaryEntryResponse from(DiaryEntry entry) {
        return new DiaryEntryResponse(
                entry.getId(),
                entry.getEntryDate(),
                entry.getOriginalText(),
                entry.getCorrectedText(),
                entry.getFeedback(),
                entry.getWordCount(),
                entry.getMood(),
                entry.getCreatedAt()
        );
    }
}
