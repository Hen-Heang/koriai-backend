package com.heang.koriaibackend.domain.goal.service;

import com.heang.koriaibackend.common.api.Code;
import com.heang.koriaibackend.common.exception.BusinessException;
import com.heang.koriaibackend.domain.goal.dto.GoalResponse;
import com.heang.koriaibackend.domain.goal.mapper.GoalMapper;
import com.heang.koriaibackend.domain.goal.mapper.GoalMemberMapper;
import com.heang.koriaibackend.domain.goal.mapper.GoalStarMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/** Per-user goal pinning (goal_stars). A user may only star a goal they own or are a member of. */
@Service
@RequiredArgsConstructor
public class GoalStarService {

    private final GoalStarMapper goalStarMapper;
    private final GoalMapper goalMapper;
    private final GoalMemberMapper goalMemberMapper;
    private final GoalResponseAssembler assembler;

    public List<GoalResponse> listStarred(Long userId) {
        return assembler.toResponses(goalStarMapper.findStarredGoals(userId));
    }

    /** Toggle the star for a goal; returns the new starred state. */
    @Transactional
    public boolean toggle(Long userId, UUID goalId) {
        if (goalStarMapper.existsStar(userId, goalId) > 0) {
            goalStarMapper.deleteStar(userId, goalId);
            return false;
        }
        boolean accessible = goalMapper.countOwner(goalId, userId) > 0
                || goalMemberMapper.countMembership(goalId, userId) > 0;
        if (!accessible) {
            throw new BusinessException(Code.INSUFFICIENT_PERMISSIONS);
        }
        goalStarMapper.insertStar(userId, goalId);
        return true;
    }
}
