package com.heang.koriaibackend.domain.dashboard.controller;

import com.heang.koriaibackend.common.api.ApiResponse;
import com.heang.koriaibackend.domain.dashboard.dto.DashboardResponse;
import com.heang.koriaibackend.domain.dashboard.dto.DashboardStats;
import com.heang.koriaibackend.domain.dashboard.dto.ProgressPoint;
import com.heang.koriaibackend.domain.dashboard.dto.StreakResponse;
import com.heang.koriaibackend.domain.dashboard.mapper.DashboardMapper;
import com.heang.koriaibackend.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardMapper dashboardMapper;

    @GetMapping("/progress")
    public ApiResponse<DashboardResponse> getProgress() {
        Long userId = SecurityUtils.currentUserId();

        int correctionsThisWeek = dashboardMapper.countCorrectionsThisWeek(userId);
        int messagesThisWeek = dashboardMapper.countMessagesThisWeek(userId);
        int diaryThisWeek = dashboardMapper.countDiaryThisWeek(userId);
        int streakDays = dashboardMapper.countStreakDays(userId);
        int wordsSaved = dashboardMapper.countTotalCorrections(userId);

        int weeklyMinutes = messagesThisWeek + correctionsThisWeek * 2 + diaryThisWeek * 5;
        int dailyGoalProgress = Math.min(100, (messagesThisWeek / 7 + correctionsThisWeek / 7) * 20);

        DashboardStats stats = new DashboardStats(
                streakDays,
                weeklyMinutes,
                wordsSaved,
                correctionsThisWeek,
                dailyGoalProgress
        );

        List<Map<String, Object>> raw = dashboardMapper.getDailyActivity(userId);
        List<ProgressPoint> chartData = raw.stream()
                .map(row -> new ProgressPoint(
                        String.valueOf(row.get("day")),
                        ((Number) row.getOrDefault("minutes", 0)).intValue(),
                        ((Number) row.getOrDefault("accuracy", 0)).doubleValue()
                ))
                .toList();

        return ApiResponse.success(new DashboardResponse(stats, chartData));
    }

    @GetMapping("/streak")
    public ApiResponse<StreakResponse> getStreak() {
        Long userId = SecurityUtils.currentUserId();
        int streakDays = dashboardMapper.countStreakDays(userId);
        boolean activityToday = dashboardMapper.hasActivityToday(userId);
        return ApiResponse.success(new StreakResponse(streakDays, activityToday));
    }
}