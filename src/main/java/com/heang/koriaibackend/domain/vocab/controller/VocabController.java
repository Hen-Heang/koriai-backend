package com.heang.koriaibackend.domain.vocab.controller;

import com.heang.koriaibackend.common.api.ApiResponse;
import com.heang.koriaibackend.domain.vocab.dto.SaveVocabRequest;
import com.heang.koriaibackend.domain.vocab.dto.VocabItemResponse;
import com.heang.koriaibackend.domain.vocab.service.VocabService;
import com.heang.koriaibackend.security.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/vocab")
@RequiredArgsConstructor
public class VocabController {

    private final VocabService vocabService;

    @GetMapping
    public ApiResponse<List<VocabItemResponse>> getSavedWords() {
        return ApiResponse.success(vocabService.getSavedWords(SecurityUtils.currentUserId()));
    }

    @PostMapping("/save")
    public ApiResponse<VocabItemResponse> save(@Valid @RequestBody SaveVocabRequest request) {
        return ApiResponse.success(vocabService.saveManual(SecurityUtils.currentUserId(), request));
    }

    @GetMapping("/review/due")
    public ApiResponse<List<VocabItemResponse>> getDueWords() {
        return ApiResponse.success(vocabService.getDueWords(SecurityUtils.currentUserId()));
    }

    @PostMapping("/{id}/review")
    public ApiResponse<Void> markReviewed(@PathVariable Long id,
                                          @RequestParam(defaultValue = "true") boolean correct) {
        vocabService.markReviewed(SecurityUtils.currentUserId(), id, correct);
        return ApiResponse.success(null);
    }

    @PostMapping("/generate")
    public ApiResponse<List<VocabItemResponse>> generate(
            @RequestParam String category,
            @RequestParam(defaultValue = "10") int count) {
        return ApiResponse.success(vocabService.generateByCategory(SecurityUtils.currentUserId(), category, Math.min(count, 20)));
    }
}
