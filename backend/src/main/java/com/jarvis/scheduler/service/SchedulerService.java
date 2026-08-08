package com.jarvis.scheduler.service;

import com.jarvis.auth.model.User;
import com.jarvis.dsa.dto.DsaQuestionResponse;
import com.jarvis.dsa.service.DsaQuestionService;
import com.jarvis.practice.dto.PracticeQuestionResponse;
import com.jarvis.practice.dto.TodayQuotaResponse;
import com.jarvis.practice.model.PracticeCategoryType;
import com.jarvis.practice.service.PracticeService;
import com.jarvis.scheduler.dto.DailyScheduleResponse;
import com.jarvis.scheduler.dto.OverflowItem;
import com.jarvis.scheduler.dto.ScheduledSlotItem;
import com.jarvis.scheduler.model.ScheduledItemType;
import com.jarvis.task.dto.PriorityEventResponse;
import com.jarvis.task.dto.TaskResponse;
import com.jarvis.task.model.TaskStatus;
import com.jarvis.task.service.PriorityEventService;
import com.jarvis.task.service.TaskService;
import com.jarvis.timetable.dto.FreeSlotResponse;
import com.jarvis.timetable.dto.SlotResponse;
import com.jarvis.timetable.service.TimetableService;
import com.jarvis.topic.dto.TopicResponse;
import com.jarvis.topic.service.TopicService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class SchedulerService {

    private static final int DEFAULT_ITEM_DURATION_MINUTES = 30;

    private final TimetableService timetableService;
    private final PriorityEventService priorityEventService;
    private final TopicService topicService;
    private final DsaQuestionService dsaQuestionService;
    private final PracticeService practiceService;
    private final TaskService taskService;

    public SchedulerService(TimetableService timetableService,
                            PriorityEventService priorityEventService,
                            TopicService topicService,
                            DsaQuestionService dsaQuestionService,
                            PracticeService practiceService,
                            TaskService taskService) {
        this.timetableService = timetableService;
        this.priorityEventService = priorityEventService;
        this.topicService = topicService;
        this.dsaQuestionService = dsaQuestionService;
        this.practiceService = practiceService;
        this.taskService = taskService;
    }

    @Transactional
    public DailyScheduleResponse generateTodaySchedule(User user) {
        return generateScheduleForDate(user, LocalDate.now(ZoneOffset.UTC));
    }

    @Transactional
    public DailyScheduleResponse generateScheduleForDate(User user, LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();

        // 1. Get Day Order and Free Time Blocks
        String activeDayOrder = timetableService.getActiveDayOrder(user, date);
        List<FreeSlotResponse> freeBlocks = timetableService.getFreeSlotsForDayOrder(user, activeDayOrder, dayOfWeek);
        List<SlotResponse> collegeClasses = timetableService.getSlotsForDayOrder(user, activeDayOrder);

        // Copy free blocks for mutable allocation tracking
        List<TimeWindow> mutableFreeWindows = new ArrayList<>(
                freeBlocks.stream().map(f -> new TimeWindow(f.startTime(), f.endTime())).toList()
        );

        List<CandidateItem> candidateItems = new ArrayList<>();

        // 2. Step 2: Priority Events (Highest Priority)
        List<PriorityEventResponse> priorityEvents = priorityEventService.getUpcomingEvents(user);
        boolean hasPriorityEvents = !priorityEvents.isEmpty();

        for (PriorityEventResponse event : priorityEvents) {
            String details = (event.jdText() != null && event.boostedTopicIds() != null && !event.boostedTopicIds().isEmpty())
                    ? "Boosted topics: " + String.join(", ", event.boostedTopicIds())
                    : "Exam preparation";

            candidateItems.add(new CandidateItem(
                    ScheduledItemType.PRIORITY_EVENT_PREP,
                    "Prep for " + event.name() + " (" + event.type() + ")",
                    details,
                    event.id(),
                    60 // Reserve 60 mins for priority event prep
            ));
        }

        // 3. Step 3: Overdue Topic Revisions
        List<TopicResponse> dueTopics = topicService.getTopicsDueForRevision(user);
        for (TopicResponse topic : dueTopics) {
            candidateItems.add(new CandidateItem(
                    ScheduledItemType.TOPIC_REVISION,
                    "Revise Topic: " + topic.name(),
                    "Subject: " + topic.subjectName() + " (Interval: " + topic.intervalDays() + "d)",
                    topic.id(),
                    DEFAULT_ITEM_DURATION_MINUTES
            ));
        }

        // 4. Step 4: Daily Quotas (DSA, SQL, Aptitude)
        TodayQuotaResponse quotaStatus = practiceService.getTodayQuotaStatus(user);

        if (quotaStatus.dsaRemaining() > 0) {
            List<DsaQuestionResponse> todayDsa = dsaQuestionService.getTodayQuestions(user, quotaStatus.dsaRemaining());
            for (DsaQuestionResponse dsa : todayDsa) {
                candidateItems.add(new CandidateItem(
                        ScheduledItemType.DSA_PRACTICE,
                        "DSA: " + dsa.title(),
                        "Topic: " + dsa.topic() + " (" + dsa.difficulty() + ")",
                        dsa.id(),
                        DEFAULT_ITEM_DURATION_MINUTES
                ));
            }
        }

        if (quotaStatus.sqlRemaining() > 0) {
            List<PracticeQuestionResponse> sqlQuestions = practiceService.getPracticeQuestions(user, PracticeCategoryType.SQL);
            int count = 0;
            for (PracticeQuestionResponse sql : sqlQuestions) {
                if (count >= quotaStatus.sqlRemaining()) break;
                candidateItems.add(new CandidateItem(
                        ScheduledItemType.SQL_PRACTICE,
                        "SQL: " + sql.title(),
                        "Category: " + sql.subCategory() + " (" + sql.difficulty() + ")",
                        sql.id(),
                        DEFAULT_ITEM_DURATION_MINUTES
                ));
                count++;
            }
        }

        if (quotaStatus.aptitudeRemaining() > 0) {
            List<PracticeQuestionResponse> aptQuestions = practiceService.getPracticeQuestions(user, PracticeCategoryType.APTITUDE);
            int count = 0;
            for (PracticeQuestionResponse apt : aptQuestions) {
                if (count >= quotaStatus.aptitudeRemaining()) break;
                candidateItems.add(new CandidateItem(
                        ScheduledItemType.APTITUDE_PRACTICE,
                        "Aptitude: " + apt.title(),
                        "Category: " + apt.subCategory() + " (" + apt.difficulty() + ")",
                        apt.id(),
                        DEFAULT_ITEM_DURATION_MINUTES
                ));
                count++;
            }
        }

        // 5. Ad-hoc Pending Tasks
        List<TaskResponse> pendingTasks = taskService.getUserTasks(user, TaskStatus.PENDING);
        for (TaskResponse task : pendingTasks) {
            candidateItems.add(new CandidateItem(
                    ScheduledItemType.ADHOC_TASK,
                    "Task: " + task.title(),
                    task.description() != null ? task.description() : "Priority: " + task.priority(),
                    task.id(),
                    DEFAULT_ITEM_DURATION_MINUTES
            ));
        }

        // Apply Equal Subject Time Balancing (Round-Robin Interleaving across Subjects)
        List<CandidateItem> balancedCandidateItems = interleaveCandidatesEquallyBySubject(candidateItems);

        // 6. Packing algorithm into free time windows
        List<ScheduledSlotItem> scheduledItems = new ArrayList<>();
        List<OverflowItem> overflowItems = new ArrayList<>();

        // Add College Classes into the schedule first
        for (SlotResponse cc : collegeClasses) {
            int duration = (int) Duration.between(cc.startTime(), cc.endTime()).toMinutes();
            scheduledItems.add(new ScheduledSlotItem(
                    cc.startTime(),
                    cc.endTime(),
                    duration,
                    ScheduledItemType.COLLEGE_CLASS,
                    cc.label(),
                    "College Class (" + activeDayOrder + ")",
                    cc.id()
            ));
        }

        int windowIndex = 0;

        for (CandidateItem item : balancedCandidateItems) {
            boolean scheduled = false;

            while (windowIndex < mutableFreeWindows.size()) {
                TimeWindow window = mutableFreeWindows.get(windowIndex);
                long windowDuration = Duration.between(window.currentStart, window.end).toMinutes();

                if (windowDuration >= item.durationMinutes) {
                    LocalTime itemStart = window.currentStart;
                    LocalTime itemEnd = itemStart.plusMinutes(item.durationMinutes);

                    scheduledItems.add(new ScheduledSlotItem(
                            itemStart,
                            itemEnd,
                            item.durationMinutes,
                            item.itemType,
                            item.title,
                            item.details,
                            item.referenceId
                    ));

                    window.currentStart = itemEnd; // advance window start
                    scheduled = true;
                    break;
                } else {
                    windowIndex++; // current free window is too small, try next
                }
            }

            if (!scheduled) {
                overflowItems.add(new OverflowItem(
                        item.itemType,
                        item.title,
                        "Exceeds available free time slots today",
                        item.referenceId,
                        date.plusDays(1)
                ));
            }
        }

        // Sort all scheduled items (classes + study tasks) chronologically by start time
        scheduledItems.sort(Comparator.comparing(ScheduledSlotItem::startTime));

        String summary = String.format(
                "Daily Plan for %s (%s - %s): %d study items scheduled, %d college classes, %d carry-over items.",
                date, dayOfWeek, activeDayOrder, scheduledItems.size() - collegeClasses.size(), collegeClasses.size(), overflowItems.size()
        );

        return new DailyScheduleResponse(
                date,
                dayOfWeek,
                freeBlocks,
                scheduledItems,
                overflowItems,
                hasPriorityEvents,
                summary
        );
    }

    private static class TimeWindow {
        LocalTime currentStart;
        final LocalTime end;

        TimeWindow(LocalTime start, LocalTime end) {
            this.currentStart = start;
            this.end = end;
        }
    }

    private List<CandidateItem> interleaveCandidatesEquallyBySubject(List<CandidateItem> items) {
        List<CandidateItem> priorityItems = new ArrayList<>();
        List<CandidateItem> otherItems = new ArrayList<>();

        for (CandidateItem item : items) {
            if (item.itemType == ScheduledItemType.PRIORITY_EVENT_PREP) {
                priorityItems.add(item);
            } else {
                otherItems.add(item);
            }
        }

        // Group by subject / topic domain
        java.util.Map<String, List<CandidateItem>> subjectGroups = new java.util.LinkedHashMap<>();
        for (CandidateItem item : otherItems) {
            String subjectKey = extractSubjectKey(item);
            subjectGroups.computeIfAbsent(subjectKey, k -> new ArrayList<>()).add(item);
        }

        List<CandidateItem> balanced = new ArrayList<>(priorityItems);
        boolean itemsRemaining = true;
        int round = 0;

        while (itemsRemaining) {
            itemsRemaining = false;
            for (List<CandidateItem> group : subjectGroups.values()) {
                if (round < group.size()) {
                    balanced.add(group.get(round));
                    itemsRemaining = true;
                }
            }
            round++;
        }

        return balanced;
    }

    private String extractSubjectKey(CandidateItem item) {
        if (item.details != null && item.details.contains("Subject: ")) {
            int idx = item.details.indexOf("Subject: ") + 9;
            int endIdx = item.details.indexOf(" (", idx);
            if (endIdx > idx) return item.details.substring(idx, endIdx).trim();
            return item.details.substring(idx).trim();
        }
        return item.itemType.name();
    }

    private record CandidateItem(
            ScheduledItemType itemType,
            String title,
            String details,
            UUID referenceId,
            int durationMinutes
    ) {
    }
}
