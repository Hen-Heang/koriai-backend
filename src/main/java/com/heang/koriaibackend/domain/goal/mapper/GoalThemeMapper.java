package com.heang.koriaibackend.domain.goal.mapper;

import com.heang.koriaibackend.domain.goal.model.GoalTheme;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

@Mapper
public interface GoalThemeMapper {

    int insert(GoalTheme theme);

    GoalTheme findById(@Param("id") UUID id);

    List<GoalTheme> findByUser(@Param("userId") Long userId);

    int update(GoalTheme theme);

    int deleteByIdAndOwner(@Param("id") UUID id, @Param("userId") Long userId);
}
