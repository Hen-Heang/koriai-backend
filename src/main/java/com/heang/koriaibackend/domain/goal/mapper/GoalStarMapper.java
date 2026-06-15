package com.heang.koriaibackend.domain.goal.mapper;

import com.heang.koriaibackend.domain.goal.model.Goal;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

@Mapper
public interface GoalStarMapper {

    int insertStar(@Param("userId") Long userId, @Param("goalId") UUID goalId);

    int deleteStar(@Param("userId") Long userId, @Param("goalId") UUID goalId);

    int existsStar(@Param("userId") Long userId, @Param("goalId") UUID goalId);

    /** Goals this user has starred, newest star first. */
    List<Goal> findStarredGoals(@Param("userId") Long userId);
}
