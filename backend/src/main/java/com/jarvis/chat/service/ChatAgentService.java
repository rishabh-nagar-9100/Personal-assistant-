package com.jarvis.chat.service;

import com.jarvis.auth.model.User;
import com.jarvis.briefing.client.LlmClient;
import com.jarvis.chat.dto.ChatResponse;
import com.jarvis.practice.service.PracticeService;
import com.jarvis.scheduler.dto.DailyScheduleResponse;
import com.jarvis.scheduler.dto.ScheduledSlotItem;
import com.jarvis.scheduler.service.SchedulerService;
import com.jarvis.task.dto.TaskResponse;
import com.jarvis.task.service.TaskService;
import com.jarvis.timetable.service.TimetableService;
import com.jarvis.topic.dto.TopicResponse;
import com.jarvis.topic.service.TopicService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import com.jarvis.task.model.TaskStatus;

@Service
public class ChatAgentService {

    private static final Logger log = LoggerFactory.getLogger(ChatAgentService.class);

    private final LlmClient llmClient;
    private final SchedulerService schedulerService;
    private final TopicService topicService;
    private final TaskService taskService;
    private final PracticeService practiceService;
    private final com.jarvis.topic.service.SubjectService subjectService;
    private final TimetableService timetableService;

    public ChatAgentService(LlmClient llmClient,
                            SchedulerService schedulerService,
                            TopicService topicService,
                            TaskService taskService,
                            PracticeService practiceService,
                            com.jarvis.topic.service.SubjectService subjectService,
                            TimetableService timetableService) {
        this.llmClient = llmClient;
        this.schedulerService = schedulerService;
        this.topicService = topicService;
        this.taskService = taskService;
        this.practiceService = practiceService;
        this.subjectService = subjectService;
        this.timetableService = timetableService;
    }

    @Transactional
    public ChatResponse processMessage(User user, String userMessage) {
        // Build rich context about the user's current state
        String context = buildUserContext(user);

        String systemPrompt = """
                You are JARVIS, an AI personal study & productivity assistant.
                You have access to the user's current local time, college Day Order, daily schedule, topics, tasks, practice quotas, and revision queue.
                
                When responding to user requests:
                1. ALWAYS check the CURRENT LOCAL TIME provided in the context.
                2. When asked about their schedule or "what's next", prioritize showing what is CURRENTLY ACTIVE NOW and what is REMAINING FOR THE REST OF THE DAY. Do not list past morning slots unless asked for a full day history.
                3. Highlight upcoming sessions and suggest clear action steps for the remaining hours.
                4. Reference actual context data (Day Order, remaining quotas, due topics) without hallucinating.
                5. Be concise, structured, and encouraging. Use emoji tastefully. Format responses clearly.
                """;

        String userPrompt = String.format("""
                USER CONTEXT (current state):
                %s
                
                USER MESSAGE:
                %s
                """, context, userMessage);

        try {
            String reply = llmClient.generateBriefingText(systemPrompt, userPrompt);
            String intent = classifyIntent(userMessage);
            List<String> actions = new ArrayList<>();

            if (intent.equals("SCHEDULE_QUERY") || intent.equals("STUDY_RECOMMENDATION")) {
                actions.add("Fetched today's live schedule, current time, and active Day Order");
            }

            return new ChatResponse(reply, intent, actions);
        } catch (Exception e) {
            log.warn("LLM chat failed, falling back to deterministic response. Cause: {}", e.getMessage());
            return buildDeterministicResponse(user, userMessage, context);
        }
    }

    private String buildUserContext(User user) {
        StringBuilder ctx = new StringBuilder();
        LocalTime nowTime = LocalTime.now();
        LocalDate todayDate = LocalDate.now();

        ctx.append("CURRENT LOCAL TIME: ").append(nowTime.toString().substring(0, 5)).append("\n");
        ctx.append("TODAY'S DATE: ").append(todayDate).append(" (").append(todayDate.getDayOfWeek()).append(")\n");

        try {
            String activeDayOrder = timetableService.getActiveDayOrder(user, todayDate);
            ctx.append("ACTIVE COLLEGE DAY ORDER: ").append(activeDayOrder).append("\n");
        } catch (Exception e) {
            ctx.append("ACTIVE COLLEGE DAY ORDER: Unknown\n");
        }

        // Today's schedule categorized by time status
        try {
            DailyScheduleResponse schedule = schedulerService.generateTodaySchedule(user);
            ctx.append("\nTODAY'S SCHEDULE (Total ").append(schedule.scheduledItems() != null ? schedule.scheduledItems().size() : 0).append(" items):\n");
            
            List<ScheduledSlotItem> activeNow = new ArrayList<>();
            List<ScheduledSlotItem> remaining = new ArrayList<>();
            List<ScheduledSlotItem> past = new ArrayList<>();

            if (schedule.scheduledItems() != null && !schedule.scheduledItems().isEmpty()) {
                for (ScheduledSlotItem item : schedule.scheduledItems()) {
                    LocalTime start = item.startTime();
                    LocalTime end = item.endTime();
                    
                    if (nowTime.isBefore(start)) {
                        remaining.add(item);
                        ctx.append("  [UPCOMING ⏳] ").append(start).append("-").append(end)
                           .append(": ").append(item.title()).append(" (").append(item.itemType()).append(")\n");
                    } else if (!nowTime.isBefore(start) && !nowTime.isAfter(end)) {
                        activeNow.add(item);
                        ctx.append("  [CURRENTLY ACTIVE 🟢] ").append(start).append("-").append(end)
                           .append(": ").append(item.title()).append(" (").append(item.itemType()).append(")\n");
                    } else {
                        past.add(item);
                        ctx.append("  [COMPLETED ✓] ").append(start).append("-").append(end)
                           .append(": ").append(item.title()).append(" (").append(item.itemType()).append(")\n");
                    }
                }
            } else {
                ctx.append("  No scheduled items today.\n");
            }

            ctx.append("\nSCHEDULE STATUS OVERVIEW:\n");
            ctx.append("  • Active Now: ").append(activeNow.isEmpty() ? "None" : activeNow.get(0).title()).append("\n");
            ctx.append("  • Remaining Sessions Today: ").append(remaining.size()).append("\n");
            ctx.append("  • Completed Sessions Today: ").append(past.size()).append("\n");

        } catch (Exception e) {
            ctx.append("SCHEDULE: Unable to load.\n");
        }

        // Due topics (revision queue)
        try {
            List<TopicResponse> dueTopics = topicService.getTopicsDueForRevision(user);
            ctx.append("\nREVISION QUEUE (").append(dueTopics.size()).append(" topics due):\n");
            for (TopicResponse t : dueTopics.stream().limit(8).toList()) {
                ctx.append("  • ").append(t.name()).append(" (Subject: ").append(t.subjectName())
                   .append(", Ease: ").append(String.format("%.1f", t.easeFactor()))
                   .append(", Interval: ").append(t.intervalDays()).append("d)\n");
            }
        } catch (Exception e) {
            ctx.append("REVISION QUEUE: Unable to load.\n");
        }

        // Practice quotas
        try {
            var quotas = practiceService.getTodayQuotaStatus(user);
            ctx.append("\nPRACTICE QUOTAS:\n");
            ctx.append("  DSA: ").append(quotas.dsaDone()).append("/").append(quotas.dsaTarget()).append("\n");
            ctx.append("  SQL: ").append(quotas.sqlDone()).append("/").append(quotas.sqlTarget()).append("\n");
            ctx.append("  Aptitude: ").append(quotas.aptitudeDone()).append("/").append(quotas.aptitudeTarget()).append("\n");
        } catch (Exception e) {
            ctx.append("PRACTICE QUOTAS: Unable to load.\n");
        }

        // Tasks
        try {
            List<TaskResponse> tasks = taskService.getUserTasks(user, null);
            long pending = tasks.stream().filter(t -> TaskStatus.PENDING == t.status()).count();
            long done = tasks.stream().filter(t -> TaskStatus.DONE == t.status()).count();
            ctx.append("\nTASKS: ").append(done).append(" completed, ").append(pending).append(" pending\n");
            tasks.stream().filter(t -> TaskStatus.PENDING == t.status()).limit(5).forEach(t ->
                ctx.append("  • ").append(t.title())
                   .append(t.dueDate() != null ? " (due: " + t.dueDate() + ")" : "").append("\n")
            );
        } catch (Exception e) {
            ctx.append("TASKS: Unable to load.\n");
        }

        // Subjects & Questions Breakdown
        try {
            var summaries = subjectService.getSubjectSummaries(user);
            if (!summaries.isEmpty()) {
                ctx.append("\nSUBJECTS & QUESTIONS BREAKDOWN:\n");
                for (var s : summaries) {
                    ctx.append("  • ").append(s.name()).append(": ")
                       .append(s.totalQuestions()).append(" questions (")
                       .append(s.solvedQuestions()).append(" solved, ")
                       .append(s.inProgressQuestions()).append(" in-progress, ")
                       .append(s.needsRevisionQuestions()).append(" needs revision, ")
                       .append(s.notStartedQuestions()).append(" not started, ")
                       .append(s.topicCount()).append(" topics)\n");
                }
            }
        } catch (Exception e) {
            ctx.append("SUBJECTS: Unable to load.\n");
        }

        return ctx.toString();
    }

    private String classifyIntent(String message) {
        String lower = message.toLowerCase();
        if (lower.contains("class") || lower.contains("college") || lower.contains("lecture")
                || lower.contains("professor") || lower.contains("day order")) {
            return "COLLEGE_CLASS_QUERY";
        }
        if (lower.contains("weak") || lower.contains("revision queue") || lower.contains("hard topic")
                || lower.contains("ease factor") || lower.contains("sm-2") || lower.contains("struggling")) {
            return "WEAK_TOPICS";
        }
        if (lower.contains("subject") || lower.contains("breakdown") || lower.contains("overall progress")
                || lower.contains("question count") || lower.contains("total questions")) {
            return "SUBJECT_BREAKDOWN";
        }
        if (lower.contains("dsa") || lower.contains("sql") || lower.contains("aptitude")
                || lower.contains("quota") || lower.contains("how much left") || lower.contains("target")) {
            return "PRACTICE_QUOTA_QUERY";
        }
        if (lower.contains("schedule") || lower.contains("reschedule") || lower.contains("routine")
                || lower.contains("timetable") || lower.contains("plan my day")) {
            return "SCHEDULE_QUERY";
        }
        if (lower.contains("study") || lower.contains("focus") || lower.contains("revise")
                || lower.contains("topic") || lower.contains("what should i")) {
            return "STUDY_RECOMMENDATION";
        }
        if (lower.contains("progress") || lower.contains("how many")
                || lower.contains("completed") || lower.contains("done")) {
            return "PROGRESS_CHECK";
        }
        if (lower.contains("exam") || lower.contains("test") || lower.contains("deadline")
                || lower.contains("priority") || lower.contains("placement")) {
            return "PRIORITY_EVENT";
        }
        if (lower.contains("task") || lower.contains("todo") || lower.contains("remind")) {
            return "TASK_MANAGEMENT";
        }
        return "GENERAL_CHAT";
    }

    /**
     * Fallback when LLM is not available — provides useful deterministic responses
     * based on the user's actual data and current local time.
     */
    private ChatResponse buildDeterministicResponse(User user, String userMessage, String context) {
        String intent = classifyIntent(userMessage);
        String reply;

        switch (intent) {
            case "COLLEGE_CLASS_QUERY" -> {
                reply = generateDeterministicCollegeClassReply(user, context);
            }
            case "WEAK_TOPICS" -> {
                reply = "🧠 **Top Topics Requiring Revision (SM-2 Spaced Repetition):**\n\n" + extractRevisionSection(context)
                        + "\n\nTopics are ranked by SM-2 Ease Factor. Lower Ease Factor = higher priority for revision!";
            }
            case "SUBJECT_BREAKDOWN" -> {
                reply = "🗂️ **Overall Subject & Questions Breakdown:**\n\n" + extractSubjectSection(context)
                        + "\n\nTip: You can upload 7-column Excel files under any subject tab in your Study Plan to automatically track question statuses!";
            }
            case "PRACTICE_QUOTA_QUERY" -> {
                reply = "🎯 **Daily Practice Quota Status:**\n\n" + extractQuotaSection(context)
                        + "\n\nComplete your practice questions directly in the Study Plan or check off tasks in the Live Schedule timeline!";
            }
            case "SCHEDULE_QUERY" -> {
                reply = generateDeterministicScheduleReply(user, context);
            }
            case "STUDY_RECOMMENDATION" -> {
                reply = "📚 Based on your revision queue and study plan:\n\n" + extractRevisionSection(context)
                        + "\n\nI recommend prioritizing topics with the lowest ease factor — those need the most attention!";
            }
            case "PROGRESS_CHECK" -> {
                reply = "📊 Your progress today:\n\n" + extractQuotaSection(context)
                        + "\n" + extractTaskSection(context)
                        + "\n\nKeep going! You're making steady progress. 💪";
            }
            case "PRIORITY_EVENT" -> {
                reply = "🎯 I see you might have an important event coming up. Here's what I suggest:\n\n"
                        + "1. Review your revision queue and prioritize weak topics\n"
                        + "2. Check your practice quotas and fill any gaps\n"
                        + "3. Look at your schedule for available study blocks\n\n"
                        + extractRevisionSection(context);
            }
            case "TASK_MANAGEMENT" -> {
                reply = "✅ Here's your task overview:\n\n" + extractTaskSection(context)
                        + "\n\nYou can manage tasks from the Tasks section in your dashboard.";
            }
            default -> {
                reply = "👋 Hey! I'm JARVIS, your personal study assistant. Here's what you can ask me:\n\n"
                        + "• **\"What should I study right now?\"** — Recommend topics from revision queue\n"
                        + "• **\"What's my schedule for today?\"** — View active & remaining study slots\n"
                        + "• **\"What college classes do I have today?\"** — View today's college timetable\n"
                        + "• **\"Which topics need revision the most?\"** — Check SM-2 revision queue\n"
                        + "• **\"How much DSA and SQL is left?\"** — Check daily quota progress\n"
                        + "• **\"Summarize my progress across subjects\"** — View subject breakdown\n"
                        + "• **\"What are my pending tasks?\"** — View active tasks & deadlines\n\n"
                        + "Just type naturally or click any chip above! 🚀";
            }
        }

        List<String> actions = List.of("Analyzed user data & current time (deterministic mode — LLM key not configured)");
        return new ChatResponse(reply, intent, actions);
    }

    private String generateDeterministicCollegeClassReply(User user, String context) {
        StringBuilder sb = new StringBuilder();
        LocalDate today = LocalDate.now();

        try {
            String activeDayOrder = timetableService.getActiveDayOrder(user, today);
            var slots = timetableService.getSlotsForDayOrder(user, activeDayOrder);

            sb.append("🎓 **Today's College Timetable (Active Order: ").append(activeDayOrder).append("):**\n\n");

            if (slots == null || slots.isEmpty()) {
                sb.append("🎉 **No college classes scheduled for ").append(activeDayOrder).append("!** Today is a 100% Free Study Day.");
            } else {
                sb.append("You have **").append(slots.size()).append(" college classes** scheduled today:\n");
                for (var slot : slots) {
                    sb.append("• `").append(slot.startTime()).append(" - ").append(slot.endTime()).append("`: **")
                      .append(slot.label()).append("**\n");
                }
                sb.append("\nYour free study blocks have been automatically calculated outside these class hours.");
            }
        } catch (Exception e) {
            sb.append("🎓 Unable to load college timetable slots.");
        }

        return sb.toString();
    }

    private String generateDeterministicScheduleReply(User user, String context) {
        StringBuilder sb = new StringBuilder();
        LocalTime now = LocalTime.now();
        String timeStr = now.toString().substring(0, 5);

        sb.append("📅 **Schedule Overview (Current Local Time: ").append(timeStr).append(")**\n\n");

        try {
            DailyScheduleResponse schedule = schedulerService.generateTodaySchedule(user);
            List<ScheduledSlotItem> items = schedule.scheduledItems();

            if (items == null || items.isEmpty()) {
                return "📅 No study sessions or college classes scheduled for today! Select a Day Order or add timetable slots to populate your day.";
            }

            ScheduledSlotItem currentActive = null;
            List<ScheduledSlotItem> upcoming = new ArrayList<>();
            List<ScheduledSlotItem> past = new ArrayList<>();

            for (ScheduledSlotItem item : items) {
                if (now.isBefore(item.startTime())) {
                    upcoming.add(item);
                } else if (!now.isBefore(item.startTime()) && !now.isAfter(item.endTime())) {
                    currentActive = item;
                } else {
                    past.add(item);
                }
            }

            // 1. Current Active Slot
            if (currentActive != null) {
                sb.append("🟢 **CURRENTLY ACTIVE NOW (").append(currentActive.startTime()).append(" - ").append(currentActive.endTime()).append("):**\n");
                sb.append("• **").append(currentActive.title()).append("** (").append(currentActive.itemType()).append(")\n");
                if (currentActive.details() != null && !currentActive.details().isEmpty()) {
                    sb.append("  *Details: ").append(currentActive.details()).append("*\n");
                }
                sb.append("\n");
            } else {
                sb.append("☕ **CURRENT STATUS:** No active session running right now at ").append(timeStr).append(".\n\n");
            }

            // 2. Remaining Schedule for Today
            if (!upcoming.isEmpty()) {
                sb.append("⏳ **REMAINING SCHEDULE FOR TODAY (").append(upcoming.size()).append(" sessions left):**\n");
                for (ScheduledSlotItem item : upcoming) {
                    sb.append("• `").append(item.startTime()).append("-").append(item.endTime()).append("`: **")
                      .append(item.title()).append("** (").append(item.itemType()).append(")\n");
                }
                sb.append("\n");
            } else {
                sb.append("🎉 **ALL REMAINING SCHEDULED SESSIONS FOR TODAY ARE COMPLETED!** Great work today!\n\n");
            }

            // 3. Completed / Past Slots Summary
            if (!past.isEmpty()) {
                sb.append("✓ **EARLIER / COMPLETED SLOTS TODAY (").append(past.size()).append(" slots):**\n");
                for (ScheduledSlotItem item : past) {
                    sb.append("  • ").append(item.startTime()).append("-").append(item.endTime()).append(": ").append(item.title()).append("\n");
                }
            }

            // 4. Practice Quotas Summary
            try {
                var quotas = practiceService.getTodayQuotaStatus(user);
                sb.append("\n\n📊 **TODAY'S PRACTICE QUOTAS:**\n");
                sb.append("• DSA: ").append(quotas.dsaDone()).append("/").append(quotas.dsaTarget())
                  .append(" | SQL: ").append(quotas.sqlDone()).append("/").append(quotas.sqlTarget())
                  .append(" | Aptitude: ").append(quotas.aptitudeDone()).append("/").append(quotas.aptitudeTarget());
            } catch (Exception ignored) {}

        } catch (Exception e) {
            sb.append(extractScheduleSection(context));
        }

        return sb.toString().trim();
    }

    private String extractSubjectSection(String context) {
        int idx = context.indexOf("SUBJECTS & QUESTIONS BREAKDOWN:");
        if (idx >= 0) {
            return context.substring(idx).trim();
        }
        return "No subject breakdown data available.";
    }

    private String extractScheduleSection(String context) {
        return extractSection(context, "TODAY'S SCHEDULE", "REVISION QUEUE");
    }

    private String extractRevisionSection(String context) {
        return extractSection(context, "REVISION QUEUE", "PRACTICE QUOTAS");
    }

    private String extractQuotaSection(String context) {
        return extractSection(context, "PRACTICE QUOTAS", "TASKS");
    }

    private String extractTaskSection(String context) {
        int idx = context.indexOf("TASKS:");
        if (idx >= 0) {
            return context.substring(idx).trim();
        }
        return "No task data available.";
    }

    private String extractSection(String context, String startMarker, String endMarker) {
        int start = context.indexOf(startMarker);
        int end = context.indexOf(endMarker);
        if (start >= 0) {
            if (end > start) {
                return context.substring(start, end).trim();
            }
            return context.substring(start).trim();
        }
        return "No data available.";
    }
}

