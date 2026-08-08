package com.jarvis.chat.dto;

import java.util.List;

public record ChatResponse(
        String reply,
        String intent,
        boolean actionExecuted,
        List<String> actionsPerformed
) {
    public ChatResponse(String reply) {
        this(reply, "GENERAL_CHAT", false, List.of());
    }

    public ChatResponse(String reply, String intent, List<String> actionsPerformed) {
        this(reply, intent, !actionsPerformed.isEmpty(), actionsPerformed);
    }
}
