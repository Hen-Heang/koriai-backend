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
import org.springframework.web.bind.annotation.RequestParam;
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
        int streakDays = dashboardMapper.countStreakDays(userId);
        int wordsSaved = dashboardMapper.countTotalWordsSaved(userId);
        int reviewsToday = dashboardMapper.countReviewsToday(userId);
        int correctionsToday = dashboardMapper.countCorrectionsToday(userId);
        int dueReviews = dashboardMapper.countDueReviews(userId);

        // Daily goal: 5 flashcard reviews (20% each) OR 1 written sentence (100%).
        int dailyGoalProgress = Math.min(100, reviewsToday * 20 + correctionsToday * 100);

        List<Map<String, Object>> raw = dashboardMapper.getDailyActivity(userId);
        List<ProgressPoint> chartData = raw.stream()
                .map(row -> new ProgressPoint(
                        String.valueOf(row.get("day")),
                        ((Number) row.getOrDefault("minutes", 0)).intValue(),
                        ((Number) row.getOrDefault("accuracy", 0)).doubleValue()
                ))
                .toList();

        // Derived from the same chart data shown to the user, rather than a
        // separately computed (and previously inconsistent — it omitted vocab
        // review and listening activity) formula.
        int weeklyMinutes = chartData.stream().mapToInt(ProgressPoint::minutes).sum();

        DashboardStats stats = new DashboardStats(
                streakDays,
                weeklyMinutes,
                wordsSaved,
                correctionsThisWeek,
                dailyGoalProgress,
                reviewsToday,
                correctionsToday,
                dueReviews
        );

        return ApiResponse.success(new DashboardResponse(stats, chartData));
    }

    @GetMapping("/streak")
    public ApiResponse<StreakResponse> getStreak() {
        Long userId = SecurityUtils.currentUserId();
        int streakDays = dashboardMapper.countStreakDays(userId);
        boolean activityToday = dashboardMapper.hasActivityToday(userId);
        return ApiResponse.success(new StreakResponse(streakDays, activityToday));
    }

    @GetMapping("/activity")
    public ApiResponse<List<String>> getActivityDays(@RequestParam String month) {
        Long userId = SecurityUtils.currentUserId();
        if (!month.matches("\\d{4}-\\d{2}")) {
            return ApiResponse.success(List.of());
        }
        return ApiResponse.success(dashboardMapper.getActivityDays(userId, month));
    }
}