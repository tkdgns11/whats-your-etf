package com.whatsyouretf.userservice.domain.ai.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.whatsyouretf.userservice.common.exception.BusinessException;
import com.whatsyouretf.userservice.common.exception.ErrorCode;
import com.whatsyouretf.userservice.domain.ai.dto.GmsRequest;
import com.whatsyouretf.userservice.domain.ai.dto.GmsResponse;
import com.whatsyouretf.userservice.domain.ai.dto.PortfolioReviewRequest;
import com.whatsyouretf.userservice.domain.ai.entity.PortfolioAiFeedback;
import com.whatsyouretf.userservice.domain.ai.entity.RiskLevel;
import com.whatsyouretf.userservice.domain.ai.repository.PortfolioAiFeedbackRepository;
import com.whatsyouretf.userservice.domain.ai.service.LlmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * LLM 서비스 구현체 (GMS API 연동)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LlmServiceImpl implements LlmService {

    private final WebClient gmsWebClient;
    private final PortfolioAiFeedbackRepository feedbackRepository;
    private final ObjectMapper objectMapper;

    @Value("${gms.model.name}")
    private String modelName;

    @Value("${gms.model.max-tokens}")
    private int maxTokens;

    @Value("${gms.model.temperature}")
    private double temperature;

    private static final String SYSTEM_PROMPT = """
        당신은 ETF 포트폴리오 분석 전문가입니다. 사용자의 포트폴리오를 분석하여 투자 성향과 특징을 진단해주세요.

        반드시 아래 JSON 형식으로만 응답해주세요. 다른 텍스트 없이 JSON만 반환하세요.

        ```json
        {
          "headline": "포트폴리오 특성 한 문장 (15자 내외)",
          "sub_headline": "부제목 구체적 설명 (25자 내외)",
          "keywords": ["키워드1", "키워드2", "키워드3"],
          "analysis": "종합 분석 200~300자",
          "bull_review": {
            "summary": "긍정적 관점 요약",
            "points": [
              {"title": "포인트 제목", "description": "설명"}
            ]
          },
          "bear_review": {
            "summary": "부정적 관점 요약",
            "points": [
              {"title": "포인트 제목", "description": "설명"}
            ]
          },
          "overall_score": 7.5,
          "risk_level": "MEDIUM",
          "recommendation": "개선 제안"
        }
        ```

        - overall_score: 0.0 ~ 10.0 (분산, 안정성, 성장성 기준)
        - risk_level: LOW / MEDIUM / HIGH
        - bull_review.points: 긍정적 포인트 2~3개
        - bear_review.points: 부정적 포인트 2~3개
        """;

    @Override
    public String analyzePortfolio(String promptTemplate, PortfolioReviewRequest.PortfolioInfo portfolio) {
        String userMessage = buildUserMessage(portfolio);

        GmsRequest request = GmsRequest.forPortfolioAnalysis(
                modelName,
                promptTemplate != null ? promptTemplate : SYSTEM_PROMPT,
                userMessage,
                maxTokens,
                temperature
        );

        try {
            GmsResponse response = gmsWebClient.post()
                    .uri("/v1/chat/completions")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(GmsResponse.class)
                    .block();

            if (response == null || response.getContent() == null) {
                throw new BusinessException(ErrorCode.AI_SERVICE_UNAVAILABLE);
            }

            return response.getContent();
        } catch (WebClientResponseException e) {
            log.error("GMS API 호출 실패: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new BusinessException(ErrorCode.AI_SERVICE_UNAVAILABLE);
        } catch (Exception e) {
            log.error("LLM 분석 실패", e);
            throw new BusinessException(ErrorCode.REVIEW_GENERATION_FAILED);
        }
    }

    @Override
    @Async
    @Transactional
    public void analyzePortfolioAsync(Long feedbackId, String promptTemplate, PortfolioReviewRequest.PortfolioInfo portfolio) {
        PortfolioAiFeedback feedback = feedbackRepository.findById(feedbackId).orElse(null);
        if (feedback == null) {
            log.error("피드백을 찾을 수 없음: feedbackId={}", feedbackId);
            return;
        }

        try {
            String llmResponse = analyzePortfolio(promptTemplate, portfolio);
            log.debug("LLM 응답: {}", llmResponse);

            // JSON 파싱 및 저장
            parseLlmResponseAndUpdate(feedback, llmResponse);

        } catch (Exception e) {
            log.error("비동기 포트폴리오 분석 실패: feedbackId={}", feedbackId, e);
            feedback.fail();
            feedbackRepository.save(feedback);
        }
    }

    /**
     * 사용자 메시지 생성
     */
    private String buildUserMessage(PortfolioReviewRequest.PortfolioInfo portfolio) {
        StringBuilder sb = new StringBuilder();
        sb.append("[포트폴리오 정보]\n");
        sb.append("투자금액: ").append(String.format("%,d", portfolio.getTotalAmount())).append("원\n");
        sb.append("투자유형: ").append(portfolio.getInvestmentType()).append("\n\n");

        sb.append("[ETF 구성]\n");
        for (PortfolioReviewRequest.EtfInfo etf : portfolio.getEtfs()) {
            sb.append("- ").append(etf.getName())
                    .append(" (").append(etf.getTicker()).append(")")
                    .append(": ").append(etf.getWeight()).append("%\n");
        }

        return sb.toString();
    }

    /**
     * LLM 응답 파싱 및 피드백 엔티티 업데이트
     */
    private void parseLlmResponseAndUpdate(PortfolioAiFeedback feedback, String llmResponse) {
        try {
            // JSON 블록 추출 (```json ... ``` 형식 처리)
            String jsonContent = extractJsonFromResponse(llmResponse);
            JsonNode root = objectMapper.readTree(jsonContent);

            // 헤드라인, 서브헤드라인
            String headline = getTextOrNull(root, "headline");
            String subHeadline = getTextOrNull(root, "sub_headline");

            // 키워드
            String keywords = null;
            if (root.has("keywords")) {
                keywords = objectMapper.writeValueAsString(root.get("keywords"));
            }

            // 분석
            String analysis = getTextOrNull(root, "analysis");

            // Bull/Bear 리뷰
            String bullReview = null;
            String bearReview = null;
            if (root.has("bull_review")) {
                bullReview = objectMapper.writeValueAsString(root.get("bull_review"));
            }
            if (root.has("bear_review")) {
                bearReview = objectMapper.writeValueAsString(root.get("bear_review"));
            }

            // 점수 및 리스크 레벨
            BigDecimal overallScore = BigDecimal.valueOf(7.0);
            if (root.has("overall_score")) {
                overallScore = BigDecimal.valueOf(root.get("overall_score").asDouble());
            }

            RiskLevel riskLevel = RiskLevel.MEDIUM;
            if (root.has("risk_level")) {
                String level = root.get("risk_level").asText();
                try {
                    riskLevel = RiskLevel.valueOf(level);
                } catch (IllegalArgumentException e) {
                    riskLevel = RiskLevel.MEDIUM;
                }
            }

            // 추천
            String recommendation = getTextOrNull(root, "recommendation");

            // 엔티티 업데이트
            feedback.setHeadline(headline);
            feedback.setSubHeadline(subHeadline);
            feedback.setKeywords(keywords);
            feedback.setAnalysis(analysis);
            feedback.complete(bullReview, bearReview, overallScore, riskLevel, recommendation, modelName);

            feedbackRepository.save(feedback);
            log.info("포트폴리오 AI 분석 완료: feedbackId={}", feedback.getId());

        } catch (JsonProcessingException e) {
            log.error("LLM 응답 JSON 파싱 실패: {}", llmResponse, e);
            feedback.fail();
            feedbackRepository.save(feedback);
        }
    }

    /**
     * JSON 블록 추출
     */
    private String extractJsonFromResponse(String response) {
        if (response == null) return "{}";

        // ```json ... ``` 형식 처리
        if (response.contains("```json")) {
            int start = response.indexOf("```json") + 7;
            int end = response.indexOf("```", start);
            if (end > start) {
                return response.substring(start, end).trim();
            }
        }

        // ``` ... ``` 형식 처리
        if (response.contains("```")) {
            int start = response.indexOf("```") + 3;
            int end = response.indexOf("```", start);
            if (end > start) {
                return response.substring(start, end).trim();
            }
        }

        // 그 외는 그대로 반환
        return response.trim();
    }

    private String getTextOrNull(JsonNode node, String fieldName) {
        if (node.has(fieldName) && !node.get(fieldName).isNull()) {
            return node.get(fieldName).asText();
        }
        return null;
    }
}
