package com.heang.koriaibackend.domain.diary.dto;

public record DiaryChange(
        String original,
        String corrected,
        String englishMeaning,
        String reason
) {
}
