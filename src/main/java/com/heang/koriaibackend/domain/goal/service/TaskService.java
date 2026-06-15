package com.heang.koriaibackend.domain.goal.service;

import com.heang.koriaibackend.common.api.Code;
import com.heang.koriaibackend.common.exception.BusinessException;
import com.heang.koriaibackend.domain.goal.dto.CreateTaskRequest;
import com.heang.koriaibackend.domain.goal.dto.TaskResponse;
import com.heang.koriaibackend.domain.goal.dto.UpdateTaskRequest;
import com.heang.koriaibackend.domain.goal.mapper.GoalMapper;
import com.heang.koriaibackend.domain.goal.mapper.GoalMemberMapper;
import com.heang.koriaibackend.domain.goal.mapper.TaskMapper;
import com.heang.koriaibackend.domain.goal.model.Task;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Task CRUD with service-layer authorization (replacing Supabase RLS):
 *   a task is writable by its owner, or by the owner/members of its goal.
 *   a standalone task (null goal) is private to its owner.
 */
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskMapper taskMapper;
    private final GoalMapper goalMapper;
    private final GoalMemberMapper goalMemberMapper;
    private final GoalNotificationService notificationService;

    private static String taskUrl(UUID goalId) {
        return goalId != null ? "/goals/" + goalId : "/goals/calendar";
    }

    /** Tasks for a goal (used by GET /goals/{id}/tasks). */
    public List<TaskResponse> listByGoal(Long userId, UUID goalId) {
        requireGoalAccess(userId, goalId);
        return taskMapper.findByGoal(goalId).stream().map(TaskResponse::of).toList();
    }

    /** Calendar range query: goalId null = all the user's tasks; otherwise that goal's tasks. */
    public List<TaskResponse> listRange(Long userId, UUID goalId, String from, String to) {
        if (goalId != null) {
            requireGoalAccess(userId, goalId);
        }
        return taskMapper.findRange(userId, goalId, parseTimestamp(from), parseTimestamp(to))
                .stream().map(TaskResponse::of).toList();
    }

    @Transactional
    public TaskResponse createTask(Long userId, CreateTaskRequest req) {
        if (req.goalId() != null) {
            requireGoalWrite(userId, req.goalId());
        }
        String title = (req.title() != null && !req.title().isBlank()) ? req.title().trim() : "Untitled task";
        Task task = Task.builder()
                .id(UUID.randomUUID())
                .goalId(req.goalId())
                .userId(userId)
                .title(title)
                .description(req.description())
                .completed(Boolean.TRUE.equals(req.completed()))
                .startDate(parseTimestamp(req.startDate()))
                .endDate(parseTimestamp(req.endDate()))
                .dailyStartTime(parseTime(req.dailyStartTime()))
                .dailyEndTime(parseTime(req.dailyEndTime()))
                .anytime(Boolean.TRUE.equals(req.isAnytime()))
                .durationMinutes(req.durationMinutes())
                .color(req.color())
                .tags(req.tags() == null ? Collections.emptyList() : req.tags())
                .updatedBy(userId)
                .build();
        taskMapper.insert(task);
        notificationService.notifySelf(userId, "task_created", task.getGoalId(), taskUrl(task.getGoalId()));
        return TaskResponse.of(taskMapper.findById(task.getId()));
    }

    @Transactional
    public TaskResponse updateTask(Long userId, UUID taskId, UpdateTaskRequest req) {
        Task task = requireTaskWrite(userId, taskId);
        boolean wasCompleted = task.isCompleted();
        if (req.title() != null) task.setTitle(req.title().trim());
        if (req.description() != null) task.setDescription(req.description());
        if (req.completed() != null) task.setCompleted(req.completed());
        if (req.startDate() != null) task.setStartDate(parseTimestamp(req.startDate()));
        if (req.endDate() != null) task.setEndDate(parseTimestamp(req.endDate()));
        if (req.dailyStartTime() != null) task.setDailyStartTime(parseTime(req.dailyStartTime()));
        if (req.dailyEndTime() != null) task.setDailyEndTime(parseTime(req.dailyEndTime()));
        if (req.isAnytime() != null) task.setAnytime(req.isAnytime());
        if (req.durationMinutes() != null) task.setDurationMinutes(req.durationMinutes());
        if (req.color() != null) task.setColor(req.color());
        if (req.tags() != null) task.setTags(req.tags());
        task.setUpdatedBy(userId);
        taskMapper.update(task);
        // Self-notify on the incomplete → complete transition only (a milestone,
        // not on every edit) so the bell stays meaningful and low-noise.
        if (!wasCompleted && task.isCompleted()) {
            notificationService.notifySelf(userId, "task_updated", task.getGoalId(), taskUrl(task.getGoalId()));
        }
        return TaskResponse.of(taskMapper.findById(taskId));
    }

    @Transactional
    public void deleteTask(Long userId, UUID taskId) {
        requireTaskWrite(userId, taskId);
        taskMapper.deleteById(taskId);
    }

    private Task requireTaskWrite(Long userId, UUID taskId) {
        Task task = taskMapper.findById(taskId);
        if (task == null) {
            throw new BusinessException(Code.NOT_FOUND);
        }
        boolean writable = task.getUserId().equals(userId)
                || (task.getGoalId() != null && canWriteGoal(userId, task.getGoalId()));
        if (!writable) {
            throw new BusinessException(Code.INSUFFICIENT_PERMISSIONS);
        }
        return task;
    }

    private void requireGoalAccess(Long userId, UUID goalId) {
        if (goalMapper.findById(goalId) == null) {
            throw new BusinessException(Code.NOT_FOUND);
        }
        if (goalMapper.countAccess(goalId, userId) == 0) {
            throw new BusinessException(Code.INSUFFICIENT_PERMISSIONS);
        }
    }

    private void requireGoalWrite(Long userId, UUID goalId) {
        if (goalMapper.findById(goalId) == null) {
            throw new BusinessException(Code.NOT_FOUND);
        }
        if (!canWriteGoal(userId, goalId)) {
            throw new BusinessException(Code.INSUFFICIENT_PERMISSIONS);
        }
    }

    /** Owner or member may write tasks in a goal (public visibility alone is not enough). */
    private boolean canWriteGoal(Long userId, UUID goalId) {
        return goalMapper.countOwner(goalId, userId) > 0
                || goalMemberMapper.countMembership(goalId, userId) > 0;
    }

    private OffsetDateTime parseTimestamp(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(value);
        } catch (Exception e) {
            try {
                return OffsetDateTime.parse(value + "T00:00:00Z");
            } catch (Exception ignored) {
                throw new BusinessException(Code.BAD_REQUEST, "Invalid timestamp: " + value);
            }
        }
    }

    private LocalTime parseTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalTime.parse(value);
        } catch (Exception e) {
            throw new BusinessException(Code.BAD_REQUEST, "Invalid time: " + value);
        }
    }
}
