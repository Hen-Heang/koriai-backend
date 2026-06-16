package com.heang.koriaibackend.domain.goal.service;

import com.heang.koriaibackend.domain.goal.mapper.TaskMapper;
import com.heang.koriaibackend.domain.goal.model.DueTaskReminder;
import com.heang.koriaibackend.domain.push.service.PushDispatcher;
import com.heang.koriaibackend.domain.push.service.PushMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Per-minute scheduled task reminders — the backend port of Orbit/goalmap's
 * {@code scheduled-reminders} edge function + pg_cron.
 *
 * Each run finds timed, upcoming, not-yet-reminded tasks whose start is within
 * the owner's reminder offset (via {@link TaskMapper#findDueReminders()}) and
 * fans a "⏰ Upcoming task" message out through the existing {@link PushDispatcher}
 * (Telegram / web push / FCM). Each task is stamped {@code reminder_sent_at} so it
 * fires exactly once. The DB-level dedupe means a missed minute self-heals on the
 * next tick rather than double-sending.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReminderScheduler {

    private final TaskMapper taskMapper;
    private final PushDispatcher pushDispatcher;

    /** Runs at the top of every minute (server time). */
    @Scheduled(cron = "0 * * * * *")
    public void sendDueReminders() {
        List<DueTaskReminder> due;
        try {
            due = taskMapper.findDueReminders();
        } catch (Exception e) {
            log.warn("Reminder scan failed: {}", e.getMessage());
            return;
        }
        if (due.isEmpty()) {
            return;
        }
        for (DueTaskReminder r : due) {
            try {
                String url = r.getGoalId() != null ? "/goals/" + r.getGoalId() : "/goals/calendar";
                pushDispatcher.dispatch(
                        r.getUserId(),
                        new PushMessage("⏰ Upcoming task", "\"" + r.getTitle() + "\" is starting soon.", url));
                // Stamp regardless of delivery outcome — dispatch is best-effort and
                // we must not remind for the same task again.
                taskMapper.markReminderSent(r.getTaskId());
            } catch (Exception e) {
                log.warn("Failed to send reminder for task {}: {}", r.getTaskId(), e.getMessage());
            }
        }
        log.info("Sent {} task reminder(s)", due.size());
    }
}
