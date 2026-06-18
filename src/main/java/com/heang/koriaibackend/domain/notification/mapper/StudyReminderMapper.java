package com.heang.koriaibackend.domain.notification.mapper;

import com.heang.koriaibackend.domain.notification.model.StudyReminderRecipient;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StudyReminderMapper {

    /**
     * Users whose study hour (Seoul) has arrived, who haven't been reminded today,
     * and who actually have SRS cards due. Drives the per-minute "reviews due" nudge.
     */
    List<StudyReminderRecipient> findReviewsDueRecipients();

    /** Fire-once-per-Seoul-day stamp for the reviews-due nudge. */
    int markReviewsDuePushed(@Param("userId") Long userId);

    /**
     * Users in the evening (Seoul) window who have an active streak (activity
     * yesterday) but no activity yet today, and haven't been reminded today.
     * Drives the per-minute "streak saver" nudge.
     */
    List<StudyReminderRecipient> findStreakSaverRecipients();

    /** Fire-once-per-Seoul-day stamp for the streak-saver nudge. */
    int markStreakSaverPushed(@Param("userId") Long userId);
}
