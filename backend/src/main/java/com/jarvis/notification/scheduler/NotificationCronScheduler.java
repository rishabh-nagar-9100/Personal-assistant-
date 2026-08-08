package com.jarvis.notification.scheduler;

import com.jarvis.auth.model.User;
import com.jarvis.auth.repository.UserRepository;
import com.jarvis.notification.model.NotificationType;
import com.jarvis.notification.service.NotificationService;
import com.jarvis.scheduler.dto.DailyScheduleResponse;
import com.jarvis.scheduler.dto.ScheduledSlotItem;
import com.jarvis.scheduler.model.ScheduledItemType;
import com.jarvis.scheduler.service.SchedulerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalTime;
import java.util.List;

@Component
public class NotificationCronScheduler {

    private static final Logger log = LoggerFactory.getLogger(NotificationCronScheduler.class);

    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final SchedulerService schedulerService;

    public NotificationCronScheduler(UserRepository userRepository,
                                     NotificationService notificationService,
                                     SchedulerService schedulerService) {
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.schedulerService = schedulerService;
    }

    // 08:00 AM UTC Daily Briefing Cron Trigger
    @Scheduled(cron = "0 0 8 * * *")
    public void triggerMorningBriefingNotifications() {
        log.info("Cron Trigger [08:00 AM]: Generating Morning Briefing push alerts");
        List<User> users = userRepository.findAll();
        Instant now = Instant.now();

        for (User user : users) {
            notificationService.createNotification(
                    user,
                    NotificationType.MORNING_BRIEFING,
                    "🌅 Morning Briefing Ready",
                    "Your daily study schedule and briefing are ready. Tap to view today's plan!",
                    now
            );
        }
    }

    // 20:00 PM UTC Evening Revision Reminder Cron Trigger
    @Scheduled(cron = "0 0 20 * * *")
    public void triggerEveningRevisionReminders() {
        log.info("Cron Trigger [20:00 PM]: Generating Evening Revision reminders");
        List<User> users = userRepository.findAll();
        Instant now = Instant.now();

        for (User user : users) {
            notificationService.createNotification(
                    user,
                    NotificationType.EVENING_REVISION,
                    "🌙 Evening Revision Check-in",
                    "Time for your evening revision check-in! Review your pending practice quotas before concluding today.",
                    now
            );
        }
    }

    // Live Task Start Reminder Trigger (runs every 60 seconds)
    @Scheduled(fixedRate = 60000)
    public void triggerTaskStartReminders() {
        LocalTime now = LocalTime.now();
        List<User> users = userRepository.findAll();

        for (User user : users) {
            try {
                DailyScheduleResponse schedule = schedulerService.generateTodaySchedule(user);
                for (ScheduledSlotItem item : schedule.scheduledItems()) {
                    // Check if item starts within the next 2 minutes
                    long diffMinutes = java.time.Duration.between(now, item.startTime()).toMinutes();
                    if (diffMinutes >= 0 && diffMinutes <= 2) {
                        NotificationType type = item.itemType() == ScheduledItemType.COLLEGE_CLASS
                                ? NotificationType.CLASS_REMINDER
                                : NotificationType.TASK_START_REMINDER;

                        String icon = item.itemType() == ScheduledItemType.COLLEGE_CLASS ? "🎓" : "⏰";
                        notificationService.createNotification(
                                user,
                                type,
                                icon + " " + item.title(),
                                "Starting at " + item.startTime() + " - " + item.details(),
                                Instant.now()
                        );
                    }
                }
            } catch (Exception e) {
                // Log and continue to prevent cron failure
                log.debug("Skip task reminder check for user: {}", e.getMessage());
            }
        }
    }
}
