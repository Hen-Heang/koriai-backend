package com.heang.koriaibackend.domain.goal.service;

import com.heang.koriaibackend.common.api.Code;
import com.heang.koriaibackend.common.exception.BusinessException;
import com.heang.koriaibackend.domain.goal.dto.CreateInviteRequest;
import com.heang.koriaibackend.domain.goal.dto.GoalNotificationResponse;
import com.heang.koriaibackend.domain.goal.mapper.GoalMemberMapper;
import com.heang.koriaibackend.domain.goal.mapper.GoalNotificationMapper;
import com.heang.koriaibackend.domain.goal.model.GoalNotification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Goal notifications / invitations. A member of a goal can invite another user;
 * the receiver lists, reads, and accepts/declines. Accepting adds membership.
 */
@Service
@RequiredArgsConstructor
public class GoalNotificationService {

    private static final String TYPE_INVITATION = "invitation";
    private static final String STATUS_PENDING = "pending";
    private static final String STATUS_ACCEPTED = "accepted";
    private static final String STATUS_DECLINED = "declined";
    private static final String ROLE_MEMBER = "member";

    private final GoalNotificationMapper notificationMapper;
    private final GoalMemberMapper goalMemberMapper;

    public List<GoalNotificationResponse> list(Long userId, boolean onlyUnread) {
        return notificationMapper.findEnrichedByReceiver(userId, onlyUnread).stream()
                .map(GoalNotificationResponse::of).toList();
    }

    /** Invite a user to a goal. Only an existing member of the goal may invite. */
    @Transactional
    public GoalNotificationResponse invite(Long senderId, CreateInviteRequest req) {
        if (goalMemberMapper.countMembership(req.goalId(), senderId) == 0) {
            throw new BusinessException(Code.INSUFFICIENT_PERMISSIONS);
        }
        if (senderId.equals(req.receiverUserId())) {
            throw new BusinessException(Code.BAD_REQUEST, "You cannot invite yourself");
        }
        if (goalMemberMapper.countMembership(req.goalId(), req.receiverUserId()) > 0) {
            throw new BusinessException(Code.BAD_REQUEST, "User is already a member");
        }
        GoalNotification n = GoalNotification.builder()
                .id(UUID.randomUUID())
                .type(TYPE_INVITATION)
                .goalId(req.goalId())
                .senderId(senderId)
                .receiverId(req.receiverUserId())
                .payload("{}")
                .invitationStatus(STATUS_PENDING)
                .build();
        notificationMapper.insert(n);
        return GoalNotificationResponse.of(notificationMapper.findById(n.getId()));
    }

    @Transactional
    public void markRead(Long userId, UUID notificationId) {
        if (notificationMapper.markRead(notificationId, userId) == 0) {
            throw new BusinessException(Code.NOT_FOUND);
        }
    }

    /** Accept or decline an invitation. Accepting joins the goal. */
    @Transactional
    public void respond(Long userId, UUID notificationId, boolean accept) {
        GoalNotification n = notificationMapper.findById(notificationId);
        if (n == null || !userId.equals(n.getReceiverId())) {
            throw new BusinessException(Code.NOT_FOUND);
        }
        if (!TYPE_INVITATION.equals(n.getType())) {
            throw new BusinessException(Code.BAD_REQUEST, "Not an invitation");
        }
        if (!STATUS_PENDING.equals(n.getInvitationStatus())) {
            throw new BusinessException(Code.BAD_REQUEST, "Invitation already resolved");
        }
        String status = accept ? STATUS_ACCEPTED : STATUS_DECLINED;
        notificationMapper.updateInvitationStatus(notificationId, userId, status);
        if (accept && n.getGoalId() != null) {
            goalMemberMapper.insertMember(n.getGoalId(), userId, ROLE_MEMBER);
        }
    }
}
