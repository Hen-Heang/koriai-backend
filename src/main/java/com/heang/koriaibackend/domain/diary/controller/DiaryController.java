package com.heang.koriaibackend.domain.diary.controller;

import com.heang.koriaibackend.common.api.ApiResponse;
import com.heang.koriaibackend.domain.diary.dto.CreateDiaryEntryRequest;
import com.heang.koriaibackend.domain.diary.dto.DiaryEntryResponse;
import com.heang.koriaibackend.domain.diary.service.DiaryService;
import com.heang.koriaibackend.security.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/diary")
@RequiredArgsConstructor
@Validated
public class DiaryController {

    private final DiaryService diaryService;

    @PostMapping
    public ApiResponse<DiaryEntryResponse> createOrUpdate(@Valid @RequestBody CreateDiaryEntryRequest req) {
        Long userId = SecurityUtils.currentUserId();
        return ApiResponse.success(diaryService.createOrUpdate(userId, req.entryDate(), req.originalText()));
    }

    @GetMapping
    public ApiResponse<List<DiaryEntryResponse>> getByMonth(@RequestParam String month) {
        Long userId = SecurityUtils.currentUserId();
        return ApiResponse.success(diaryService.getByMonth(userId, month));
    }
}
