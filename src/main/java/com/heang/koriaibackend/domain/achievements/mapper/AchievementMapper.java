package com.heang.koriaibackend.domain.achievements.mapper;

import com.heang.koriaibackend.domain.achievements.model.Achievement;
import com.heang.koriaibackend.domain.achievements.model.UserAchievement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AchievementMapper {
    List<Achievement> findCatalog();

    List<UserAchievement> findUnlockedByUser(@Param("userId") Long userId);

    int insertUnlocked(@Param("userId") Long userId, @Param("code") String code);

    int countVocab(@Param("userId") Long userId);

    int countCorrections(@Param("userId") Long userId);

    int countDiary(@Param("userId") Long userId);

    int countChatMessages(@Param("userId") Long userId);
}
