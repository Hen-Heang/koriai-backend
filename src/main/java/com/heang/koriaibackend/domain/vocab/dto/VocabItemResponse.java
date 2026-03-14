package com.heang.koriaibackend.domain.vocab.dto;

import com.heang.koriaibackend.domain.vocab.model.VocabCard;

import java.util.List;

public record VocabItemResponse(
        String id,
        String category,
        String term,
        String meaning,
        String example,
        int mastery,
        String nextReview,
        List<String> tags
) {
    public static VocabItemResponse from(VocabCard card) {
        return new VocabItemResponse(
                String.valueOf(card.getId()),
                card.getCategory(),
                card.getTerm(),
                card.getMeaning(),
                card.getExample(),
                card.getMastery(),
                card.getNextReviewDate() != null ? card.getNextReviewDate().toString() : "-",
                List.of()
        );
    }
}
