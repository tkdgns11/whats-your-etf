package com.whatsyouretf.userservice.domain.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * GMS API 요청 DTO (OpenAI-compatible format)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GmsRequest {

    private String model;

    private List<Message> messages;

    @JsonProperty("max_tokens")
    private Integer maxTokens;

    private Double temperature;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Message {
        private String role;  // "system", "user", "assistant"
        private String content;
    }

    /**
     * 포트폴리오 분석용 요청 생성
     */
    public static GmsRequest forPortfolioAnalysis(String model, String systemPrompt, String userMessage,
                                                   int maxTokens, double temperature) {
        return GmsRequest.builder()
                .model(model)
                .messages(List.of(
                        Message.builder().role("system").content(systemPrompt).build(),
                        Message.builder().role("user").content(userMessage).build()
                ))
                .maxTokens(maxTokens)
                .temperature(temperature)
                .build();
    }
}
