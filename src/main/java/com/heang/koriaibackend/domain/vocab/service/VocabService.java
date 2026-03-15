package com.heang.koriaibackend.domain.vocab.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heang.koriaibackend.ai.OpenAiService;
import com.heang.koriaibackend.ai.dto.OpenAiResult;
import com.heang.koriaibackend.common.util.PromptTemplates;
import com.heang.koriaibackend.domain.users.mapper.UserMapper;
import com.heang.koriaibackend.domain.users.model.User;
import com.heang.koriaibackend.domain.vocab.dto.SaveVocabRequest;
import com.heang.koriaibackend.domain.vocab.dto.VocabItemResponse;
import com.heang.koriaibackend.domain.vocab.mapper.VocabCardMapper;
import com.heang.koriaibackend.domain.vocab.model.VocabCard;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VocabService {

    private final VocabCardMapper vocabCardMapper;
    private final UserMapper userMapper;
    private final OpenAiService openAiService;
    private final ObjectMapper objectMapper;

    @Value("${openai.model:gpt-4o-mini}")
    private String model;

    public List<VocabItemResponse> getSavedWords(Long userId) {
        return vocabCardMapper.findByUserId(userId).stream()
                .map(VocabItemResponse::from)
                .toList();
    }

    public List<VocabItemResponse> getDueWords(Long userId) {
        return vocabCardMapper.findDueByUserId(userId).stream()
                .map(VocabItemResponse::from)
                .toList();
    }

    @Transactional
    public VocabItemResponse saveManual(Long userId, SaveVocabRequest request) {
        VocabCard card = VocabCard.builder()
                .userId(userId)
                .category(normalizeCategory(request.category()))
                .term(request.term().trim())
                .meaning(request.meaning().trim())
                .example(hasText(request.example()) ? request.example().trim() : null)
                .mastery(0)
                .tags("[]")
                .build();
        vocabCardMapper.insert(card);
        return VocabItemResponse.from(card);
    }

    @Transactional
    public void markReviewed(Long userId, Long cardId, boolean correct) {
        VocabCard card = vocabCardMapper.findByUserId(userId).stream()
                .filter(c -> c.getId().equals(cardId))
                .findFirst()
                .orElse(null);
        if (card == null) return;

        int newMastery = correct ? Math.min(100, card.getMastery() + 20) : Math.max(0, card.getMastery() - 10);
        int daysUntilReview = correct ? Math.max(1, newMastery / 20) : 1;
        String nextReview = LocalDate.now().plusDays(daysUntilReview).toString();
        vocabCardMapper.updateMastery(cardId, userId, newMastery, nextReview);
    }

    @Transactional
    public List<VocabItemResponse> generateByCategory(Long userId, String category, int count) {
        User user = userMapper.findById(userId).orElse(null);
        String level = (user != null) ? user.getKoreanLevel() : "BEGINNER";

        String prompt = PromptTemplates.vocabGenerationPrompt(category, level, count);
        OpenAiResult result = openAiService.generate(prompt, model);

        List<VocabCard> cards = parseVocabCards(userId, category, result.content());
        cards.forEach(vocabCardMapper::insert);
        return cards.stream().map(VocabItemResponse::from).toList();
    }

    private List<VocabCard> parseVocabCards(Long userId, String category, String json) {
        try {
            String cleaned = json.trim();
            int start = cleaned.indexOf('[');
            int end = cleaned.lastIndexOf(']');
            if (start == -1 || end == -1) return Collections.emptyList();
            cleaned = cleaned.substring(start, end + 1);

            JsonNode array = objectMapper.readTree(cleaned);
            List<VocabCard> cards = new ArrayList<>();
            for (JsonNode node : array) {
                String tagsJson = node.has("tags") ? node.get("tags").toString() : "[]";
                cards.add(VocabCard.builder()
                        .userId(userId)
                        .category(category)
                        .term(node.path("term").asText(""))
                        .meaning(node.path("meaning").asText(""))
                        .example(node.path("example").asText(null))
                        .exampleTranslation(node.path("exampleTranslation").asText(null))
                        .mastery(0)
                        .tags(tagsJson)
                        .build());
            }
            return cards;
        } catch (JsonProcessingException e) {
            return Collections.emptyList();
        }
    }

    private String normalizeCategory(String category) {
        return hasText(category) ? category.trim() : "Saved phrases";
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
