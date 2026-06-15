package com.heang.koriaibackend.domain.goal.service;

import com.heang.koriaibackend.common.api.Code;
import com.heang.koriaibackend.common.exception.BusinessException;
import com.heang.koriaibackend.domain.goal.dto.CreateGoalRequest;
import com.heang.koriaibackend.domain.goal.dto.GoalResponse;
import com.heang.koriaibackend.domain.goal.dto.UpdateGoalRequest;
import com.heang.koriaibackend.domain.goal.mapper.GoalMapper;
import com.heang.koriaibackend.domain.goal.mapper.GoalMemberMapper;
import com.heang.koriaibackend.domain.goal.model.Goal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Goal CRUD with service-layer authorization that replaces the original Supabase RLS:
 *   view   = owner OR member OR public
 *   create = the caller (owner)
 *   update = owner only
 *   delete = owner only
 * Responses follow the Orbit/INTEGRATION.md contract (snake_case + metadata + enrichment).
 */
@Service
@RequiredArgsConstructor
public class GoalService {

    private final GoalMapper goalMapper;
    private final GoalMemberMapper goalMemberMapper;
    private final GoalResponseAssembler assembler;

    public List<GoalResponse> listGoals(Long userId) {
        return assembler.toResponses(goalMapper.findAccessibleByUser(userId));
    }

    public GoalResponse getGoal(Long userId, UUID goalId) {
        requireAccess(userId, goalId);
        return assembler.toResponse(goalMapper.findByIdEnriched(goalId, userId));
    }

    @Transactional
    public GoalResponse createGoal(Long userId, CreateGoalRequest req) {
        boolean noDuration = Boolean.TRUE.equals(req.noDuration());
        Goal goal = Goal.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .title(req.title().trim())
                .description(req.description())
                .targetDate(noDuration ? null : parseTimestamp(req.targetDate()))
                .status(req.status() != null && !req.status().isBlank() ? req.status() : "active")
                .metadata(req.metadata() != null ? req.metadata().toString() : "{}")
                .shareCode(UUID.randomUUID())
                .publicGoal(false)
                .preferences("{}")
                .build();
        goalMapper.insert(goal);
        // Mirror the original add_creator_as_member trigger.
        goalMemberMapper.insertMember(goal.getId(), userId, "creator");
        return assembler.toResponse(goalMapper.findByIdEnriched(goal.getId(), userId));
    }

    @Transactional
    public GoalResponse updateGoal(Long userId, UUID goalId, UpdateGoalRequest req) {
        Goal goal = requireOwner(userId, goalId);
        if (req.title() != null) goal.setTitle(req.title().trim());
        if (req.description() != null) goal.setDescription(req.description());
        if (req.targetDate() != null) goal.setTargetDate(parseTimestamp(req.targetDate()));
        if (req.isPublic() != null) goal.setPublicGoal(req.isPublic());
        if (req.metadata() != null) goal.setMetadata(req.metadata().toString());
        goalMapper.update(goal);
        return assembler.toResponse(goalMapper.findByIdEnriched(goalId, userId));
    }

    @Transactional
    public void deleteGoal(Long userId, UUID goalId) {
        if (goalMapper.deleteByIdAndOwner(goalId, userId) == 0) {
            throw new BusinessException(Code.INSUFFICIENT_PERMISSIONS);
        }
    }

    /** Throws if the goal does not exist or the user has no read access. */
    void requireAccess(Long userId, UUID goalId) {
        if (goalMapper.findById(goalId) == null) {
            throw new BusinessException(Code.NOT_FOUND);
        }
        if (goalMapper.countAccess(goalId, userId) == 0) {
            throw new BusinessException(Code.INSUFFICIENT_PERMISSIONS);
        }
    }

    private Goal requireOwner(Long userId, UUID goalId) {
        Goal goal = goalMapper.findById(goalId);
        if (goal == null) {
            throw new BusinessException(Code.NOT_FOUND);
        }
        if (!goal.getUserId().equals(userId)) {
            throw new BusinessException(Code.INSUFFICIENT_PERMISSIONS);
        }
        return goal;
    }

    private OffsetDateTime parseTimestamp(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(value);
        } catch (Exception e) {
            // Accept a bare date (yyyy-MM-dd) too, treating it as start-of-day UTC.
            try {
                return OffsetDateTime.parse(value + "T00:00:00Z");
            } catch (Exception ignored) {
                throw new BusinessException(Code.BAD_REQUEST, "Invalid timestamp: " + value);
            }
        }
    }
}
