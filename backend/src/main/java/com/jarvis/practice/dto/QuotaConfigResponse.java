package com.jarvis.practice.dto;

import com.jarvis.practice.model.DailyQuotaConfig;

public record QuotaConfigResponse(
        int dsaTarget,
        int sqlTarget,
        int aptitudeTarget
) {
    public static QuotaConfigResponse fromEntity(DailyQuotaConfig config) {
        return new QuotaConfigResponse(
                config.getDsaTarget(),
                config.getSqlTarget(),
                config.getAptitudeTarget()
        );
    }
}
