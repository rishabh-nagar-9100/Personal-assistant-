package com.jarvis.briefing.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class LlmClient {

    private final RestClient restClient;
    private final String apiKey;
    private final String model;

    public LlmClient(@Value("${llm.api-key:}") String apiKey,
                     @Value("${llm.base-url:https://api.groq.com/openai/v1}") String baseUrl,
                     @Value("${llm.model:llama-3.1-8b-instant}") String model) {
        this.apiKey = apiKey;
        this.model = model;
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public String generateBriefingText(String systemPrompt, String userContextPrompt) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new LlmClientException("LLM_API_KEY is not configured");
        }

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userContextPrompt)
                ),
                "max_tokens", 400,
                "temperature", 0.7
        );

        try {
            Map<?, ?> response = restClient.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            if (response != null && response.containsKey("choices")) {
                List<?> choices = (List<?>) response.get("choices");
                if (!choices.isEmpty() && choices.get(0) instanceof Map<?, ?> choice) {
                    if (choice.get("message") instanceof Map<?, ?> message) {
                        Object content = message.get("content");
                        if (content != null) {
                            return content.toString().trim();
                        }
                    }
                }
            }
            throw new LlmClientException("Invalid response structure from LLM endpoint");
        } catch (Exception e) {
            throw new LlmClientException("Failed to invoke LLM API: " + e.getMessage(), e);
        }
    }

    public static class LlmClientException extends RuntimeException {
        public LlmClientException(String message) {
            super(message);
        }

        public LlmClientException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
