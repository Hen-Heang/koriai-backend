package com.heang.koriaibackend.domain.goal.mapper;

import com.heang.koriaibackend.domain.goal.model.GoalMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

@Mapper
public interface GoalMemberMapper {

    int insertMember(@Param("goalId") UUID goalId,
                     @Param("userId") Long userId,
                     @Param("role") String role);

    int countMembership(@Param("goalId") UUID goalId, @Param("userId") Long userId);

    /** Members of a goal, joined with user display info. */
    List<GoalMember> findByGoal(@Param("goalId") UUID goalId);

    String findRole(@Param("goalId") UUID goalId, @Param("userId") Long userId);

    int deleteMember(@Param("goalId") UUID goalId, @Param("userId") Long userId);

    int touchLastSeen(@Param("goalId") UUID goalId, @Param("userId") Long userId);
}
