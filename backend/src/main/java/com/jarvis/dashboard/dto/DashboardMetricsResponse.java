package com.jarvis.dashboard.dto;

public record DashboardMetricsResponse(
        int studyTimeMinutes,
        int topicsStudiedCount,
        int topicsTotalCount,
        int tasksCompletedCount,
        int tasksTotalCount,
        int dsaDone,
        int dsaTarget,
        int sqlDone,
        int sqlTarget,
        int aptitudeDone,
        int aptitudeTarget,
        int revisionQueueSize,
        int scheduledItemsCount
) {}
