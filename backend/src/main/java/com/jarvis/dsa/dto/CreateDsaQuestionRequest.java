package com.jarvis.dsa.dto;

import com.jarvis.dsa.model.DsaDifficulty;
import com.jarvis.dsa.model.DsaStatus;

public record CreateDsaQuestionRequest(
        String title,
        String topic,
        DsaDifficulty difficulty,
        String sourceLink,
        DsaStatus status
) {
}
