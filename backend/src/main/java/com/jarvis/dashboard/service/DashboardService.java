package com.jarvis.dashboard.service;

import com.jarvis.auth.model.User;
import com.jarvis.dashboard.dto.DashboardMetricsResponse;
import com.jarvis.practice.dto.TodayQuotaResponse;
import com.jarvis.practice.service.PracticeService;
import com.jarvis.scheduler.dto.DailyScheduleResponse;
import com.jarvis.scheduler.service.SchedulerService;
import com.jarvis.task.dto.TaskResponse;
import com.jarvis.task.service.TaskService;
import com.jarvis.topic.dto.TopicResponse;
import com.jarvis.topic.service.TopicService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class DashboardService {

    private static final Logger log = LoggerFactory.getLogger(DashboardService.class);

    private final SchedulerService schedulerService;
    private final TopicService topicService;
    private final TaskService taskService;
    private final PracticeService practiceService;

    public DashboardService(SchedulerService schedulerService,
                            TopicService topicService,
                            TaskService taskService,
                            PracticeService practiceService) {
        this.schedulerService = schedulerService;
        this.topicService = topicService;
        this.taskService = taskService;
        this.practiceService = practiceService;
    }

    @Transactional(readOnly = true)
    public DashboardMetricsResponse getMetrics(User user) {
        // Practice quotas
        int dsaDone = 0, dsaTarget = 5, sqlDone = 0, sqlTarget = 5, aptDone = 0, aptTarget = 5;
        try {
            TodayQuotaResponse q = practiceService.getTodayQuotaStatus(user);
            dsaDone = q.dsaDone();
            dsaTarget = q.dsaTarget();
            sqlDone = q.sqlDone();
            sqlTarget = q.sqlTarget();
            aptDone = q.aptitudeDone();
            aptTarget = q.aptitudeTarget();
        } catch (Exception e) {
            log.warn("Could not load practice quotas: {}", e.getMessage());
        }

        // Tasks
        int tasksDone = 0, tasksTotal = 0;
        try {
            List<TaskResponse> tasks = taskService.getUserTasks(user, null);
            tasksTotal = tasks.size();
            tasksDone = (int) tasks.stream().filter(t -> com.jarvis.task.model.TaskStatus.DONE == t.status()).count();
        } catch (Exception e) {
            log.warn("Could not load tasks: {}", e.getMessage());
        }

        // Topics & revision queue
        int topicsTotal = 0, revisionQueueSize = 0;
        try {
            List<TopicResponse> dueTopics = topicService.getTopicsDueForRevision(user);
            revisionQueueSize = dueTopics.size();
        } catch (Exception e) {
            log.warn("Could not load revision queue: {}", e.getMessage());
        }

        // Schedule items
        int scheduledItemsCount = 0;
        int studyTimeMinutes = 0;
        try {
            DailyScheduleResponse schedule = schedulerService.generateTodaySchedule(user);
            if (schedule.scheduledItems() != null) {
                scheduledItemsCount = schedule.scheduledItems().size();
                studyTimeMinutes = (int) schedule.scheduledItems().stream()
                        .mapToLong(item -> item.durationMinutes())
                        .sum();
            }
        } catch (Exception e) {
            log.warn("Could not load schedule: {}", e.getMessage());
        }

        int topicsStudied = dsaDone + sqlDone + aptDone; // approximation of studied items

        return new DashboardMetricsResponse(
                studyTimeMinutes,
                topicsStudied,
                topicsTotal,
                tasksDone,
                tasksTotal,
                dsaDone, dsaTarget,
                sqlDone, sqlTarget,
                aptDone, aptTarget,
                revisionQueueSize,
                scheduledItemsCount
        );
    }
}
