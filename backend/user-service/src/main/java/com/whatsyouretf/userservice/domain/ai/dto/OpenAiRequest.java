package com.whatsyouretf.userservice.domain.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * OpenAI Chat Completions API 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpenAiRequest {

    private String model;
    private List<Message> messages;

    @JsonProperty("max_tokens")
    private int maxTokens;

    private double temperature;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        private String role;
        private String content;
    }

    /**
     * 포트폴리오 분석용 요청 생성
     */
    public static OpenAiRequest forPortfolioAnalysis(String model, String systemPrompt, String userMessage, int maxTokens) {
        return OpenAiRequest.builder()
                .model(model)
                .messages(List.of(
                        Message.builder().role("system").content(systemPrompt).build(),
                        Message.builder().role("user").content(userMessage).build()
                ))
                .maxTokens(maxTokens)
                .temperature(0.7)
                .build();
    }
}
