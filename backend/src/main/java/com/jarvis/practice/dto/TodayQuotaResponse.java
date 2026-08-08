package com.jarvis.practice.dto;

import java.time.LocalDate;

public record TodayQuotaResponse(
        LocalDate date,
        int dsaTarget,
        int dsaDone,
        int dsaRemaining,
        int sqlTarget,
        int sqlDone,
        int sqlRemaining,
        int aptitudeTarget,
        int aptitudeDone,
        int aptitudeRemaining
) {
}
