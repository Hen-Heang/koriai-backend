package com.heang.koriaibackend.domain.diary.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record CreateDiaryEntryRequest(
        LocalDate entryDate,
        @NotBlank String originalText
) {
}
