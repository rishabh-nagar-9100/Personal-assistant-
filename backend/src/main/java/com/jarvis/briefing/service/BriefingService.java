package com.jarvis.briefing.service;

import com.jarvis.auth.model.User;
import com.jarvis.briefing.client.LlmClient;
import com.jarvis.briefing.dto.DailyBriefingResponse;
import com.jarvis.briefing.model.DailyBriefing;
import com.jarvis.briefing.repository.DailyBriefingRepository;
import com.jarvis.scheduler.dto.DailyScheduleResponse;
import com.jarvis.scheduler.service.SchedulerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

@Service
public class BriefingService {

    private static final Logger log = LoggerFactory.getLogger(BriefingService.class);

    private static final String SYSTEM_PROMPT = """
            You are Jarvis, an elite personal AI assistant and executive study coach.
            Your job is to provide an inspiring, highly concise, structured daily briefing for a high-performing student/engineer.
            Keep the tone confident, direct, and actionable. Limit your response to 3 short paragraphs.
            """;

    private final DailyBriefingRepository briefingRepository;
    private final com.jarvis.auth.repository.UserRepository userRepository;
    private final SchedulerService schedulerService;
    private final LlmClient llmClient;

    public BriefingService(DailyBriefingRepository briefingRepository,
                           com.jarvis.auth.repository.UserRepository userRepository,
                           SchedulerService schedulerService,
                           LlmClient llmClient) {
        this.briefingRepository = briefingRepository;
        this.userRepository = userRepository;
        this.schedulerService = schedulerService;
        this.llmClient = llmClient;
    }

    @Transactional
    public DailyBriefingResponse getTodayBriefing(User user) {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        Optional<DailyBriefing> existing = briefingRepository.findByUserIdAndDate(user.getId(), today);

        if (existing.isPresent()) {
            DailyBriefing briefing = existing.get();
            return new DailyBriefingResponse(
                    briefing.getDate(),
                    briefing.getBriefingText(),
                    true, // isCached = true
                    briefing.getGeneratedAt()
            );
        }

        DailyBriefing generated = generateAndSaveBriefing(user, today);
        return new DailyBriefingResponse(
                generated.getDate(),
                generated.getBriefingText(),
                false, // isCached = false
                generated.getGeneratedAt()
        );
    }

    @Transactional
    public DailyBriefingResponse regenerateTodayBriefing(User user) {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        DailyBriefing regenerated = generateAndSaveBriefing(user, today);
        return new DailyBriefingResponse(
                regenerated.getDate(),
                regenerated.getBriefingText(),
                false,
                regenerated.getGeneratedAt()
        );
    }

    private DailyBriefing generateAndSaveBriefing(User user, LocalDate today) {
        DailyScheduleResponse schedule = schedulerService.generateScheduleForDate(user, today);
        String briefingText;

        try {
            String userPrompt = buildUserPrompt(schedule);
            briefingText = llmClient.generateBriefingText(SYSTEM_PROMPT, userPrompt);
        } catch (Exception e) {
            log.warn("LLM generation failed or key missing. Falling back to deterministic template briefing. Cause: {}", e.getMessage());
            briefingText = buildFallbackBriefing(schedule);
        }

        final String finalText = briefingText;
        User managedUser = userRepository.findById(user.getId()).orElse(user);
        DailyBriefing briefing = briefingRepository.findByUserIdAndDate(managedUser.getId(), today)
                .orElseGet(() -> new DailyBriefing(managedUser, today, finalText));

        briefing.setBriefingText(finalText);
        return briefingRepository.save(briefing);
    }

    private String buildUserPrompt(DailyScheduleResponse schedule) {
        StringBuilder sb = new StringBuilder();
        sb.append("Date: ").append(schedule.date()).append(" (").append(schedule.dayOfWeek()).append(")\n");
        sb.append("Priority Events Present: ").append(schedule.hasPriorityEvents()).append("\n");
        sb.append("Scheduled Items Count: ").append(schedule.scheduledItems().size()).append("\n");
        sb.append("Carry-over Overflow Items Count: ").append(schedule.overflowItems().size()).append("\n\n");

        sb.append("Scheduled Time Blocks:\n");
        schedule.scheduledItems().forEach(item ->
                sb.append("- [").append(item.startTime()).append(" - ").append(item.endTime()).append("] ")
                        .append(item.title()).append(" (").append(item.details()).append(")\n")
        );

        if (!schedule.overflowItems().isEmpty()) {
            sb.append("\nCarry-over Overflow Items:\n");
            schedule.overflowItems().forEach(item ->
                    sb.append("- ").append(item.title()).append(" -> Carried over to ").append(item.suggestedCarryOverDate()).append("\n")
            );
        }

        return sb.toString();
    }

    private String buildFallbackBriefing(DailyScheduleResponse schedule) {
        StringBuilder sb = new StringBuilder();
        sb.append("Good morning! Here is your daily briefing for ").append(schedule.date()).append(" (").append(schedule.dayOfWeek()).append(").\n\n");

        if (schedule.hasPriorityEvents()) {
            sb.append("🚨 Priority Notice: You have high-importance priority events coming up. Your schedule has been optimized to reserve prep time for these goals.\n\n");
        }

        sb.append("Today's Plan: You have ").append(schedule.scheduledItems().size())
                .append(" scheduled study/practice sessions time-blocked throughout your available free windows. ");

        if (!schedule.overflowItems().isEmpty()) {
            sb.append("Additionally, ").append(schedule.overflowItems().size())
                    .append(" items exceeded today's free capacity and have been safely queued for carry-over tomorrow. ");
        }

        sb.append("\n\nFocus on executing your scheduled blocks with high intensity. Maintain your momentum and conquer today's goals step by step!");
        return sb.toString();
    }
}
