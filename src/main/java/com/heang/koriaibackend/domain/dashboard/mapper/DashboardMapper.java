package com.heang.koriaibackend.domain.dashboard.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface DashboardMapper {
    int countCorrectionsThisWeek(@Param("userId") Long userId);
    int countMessagesThisWeek(@Param("userId") Long userId);
    int countDiaryThisWeek(@Param("userId") Long userId);
    int countStreakDays(@Param("userId") Long userId);
    int countTotalCorrections(@Param("userId") Long userId);
    List<Map<String, Object>> getDailyActivity(@Param("userId") Long userId);
}