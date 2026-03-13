package com.heang.koriaibackend.domain.diary.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiaryEntry {
    private Long id;
    private Long userId;
    private LocalDate entryDate;
    private String originalText;
    private String correctedText;
    private String feedback;
    private Integer wordCount;
    private String mood;
    private OffsetDateTime createdAt;
}
