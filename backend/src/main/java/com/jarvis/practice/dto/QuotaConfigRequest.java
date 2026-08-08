package com.jarvis.practice.dto;

public record QuotaConfigRequest(
        int dsaTarget,
        int sqlTarget,
        int aptitudeTarget
) {
}
