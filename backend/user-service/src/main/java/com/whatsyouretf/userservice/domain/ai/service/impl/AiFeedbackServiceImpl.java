package com.whatsyouretf.userservice.domain.ai.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.whatsyouretf.userservice.common.exception.BusinessException;
import com.whatsyouretf.userservice.common.exception.ErrorCode;
import com.whatsyouretf.userservice.domain.ai.dto.*;
import com.whatsyouretf.userservice.domain.ai.entity.*;
import com.whatsyouretf.userservice.domain.ai.repository.AiPromptRepository;
import com.whatsyouretf.userservice.domain.ai.repository.PortfolioAiFeedbackRepository;
import com.whatsyouretf.userservice.domain.ai.service.AiFeedbackService;
import com.whatsyouretf.userservice.domain.ai.service.LlmService;
import com.whatsyouretf.userservice.domain.user.entity.User;
import com.whatsyouretf.userservice.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * AI 피드백 서비스 구현체
 * <p>
 * LLM 연동을 통해 포트폴리오 분석을 수행합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AiFeedbackServiceImpl implements AiFeedbackService {

    private final PortfolioAiFeedbackRepository feedbackRepository;
    private final AiPromptRepository promptRepository;
    private final UserRepository userRepository;
    private final LlmService llmService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public PortfolioReviewResponse requestReview(Long userId, PortfolioReviewRequest request) {
        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 포트폴리오 비중 합계 검증
        int totalWeight = request.getPortfolio().getEtfs().stream()
                .mapToInt(PortfolioReviewRequest.EtfInfo::getWeight)
                .sum();
        if (totalWeight != 100) {
            throw new BusinessException(ErrorCode.INVALID_WEIGHT_SUM);
        }

        // 활성 프롬프트 조회
        AiPrompt prompt = promptRepository.findByNameAndIsActiveTrue("portfolio_feedback")
                .orElse(null);

        // 포트폴리오 데이터를 JSON으로 변환
        String portfolioData;
        try {
            portfolioData = objectMapper.writeValueAsString(request.getPortfolio());
        } catch (JsonProcessingException e) {
            log.error("포트폴리오 데이터 변환 실패", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        // 피드백 엔티티 생성
        PortfolioAiFeedback feedback = PortfolioAiFeedback.builder()
                .user(user)
                .prompt(prompt)
                .portfolioSnapshotId(request.getPortfolioSnapshotId())
                .portfolioData(portfolioData)
                .status(ReviewStatus.PROCESSING)
                .build();

        feedbackRepository.save(feedback);

        // 비동기로 LLM 호출 및 결과 저장
        String promptTemplate = prompt != null ? prompt.getPromptTemplate() : null;
        llmService.analyzePortfolioAsync(feedback.getId(), promptTemplate, request.getPortfolio());

        return PortfolioReviewResponse.processing(feedback.getId());
    }

    @Override
    public PortfolioReviewResponse getReview(Long userId, Long reviewId) {
        PortfolioAiFeedback feedback = feedbackRepository.findByIdAndUserId(reviewId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));

        // 처리 중인 경우
        if (feedback.getStatus() == ReviewStatus.PROCESSING) {
            return PortfolioReviewResponse.processing(feedback.getId());
        }

        // 실패한 경우
        if (feedback.getStatus() == ReviewStatus.FAILED) {
            throw new BusinessException(ErrorCode.REVIEW_GENERATION_FAILED);
        }

        // 완료된 경우 - JSON 파싱
        PortfolioReviewResponse.ReviewSection bullReview = parseReviewSection(feedback.getBullReview());
        PortfolioReviewResponse.ReviewSection bearReview = parseReviewSection(feedback.getBearReview());
        List<PortfolioReviewResponse.RelatedNewsItem> relatedNews = parseRelatedNews(feedback.getRelatedNews());

        return PortfolioReviewResponse.from(feedback, bullReview, bearReview, relatedNews);
    }

    @Override
    public ReviewHistoryResponse getReviewHistory(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        Page<PortfolioAiFeedback> feedbackPage = feedbackRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        List<ReviewHistoryResponse.ReviewSummary> reviews = feedbackPage.getContent().stream()
                .map(feedback -> {
                    String bullSummary = extractSummary(feedback.getBullReview());
                    String bearSummary = extractSummary(feedback.getBearReview());
                    return ReviewHistoryResponse.ReviewSummary.from(feedback, bullSummary, bearSummary);
                })
                .toList();

        return ReviewHistoryResponse.builder()
                .reviews(reviews)
                .page(page)
                .totalPages(feedbackPage.getTotalPages())
                .totalElements(feedbackPage.getTotalElements())
                .build();
    }

    @Override
    @Transactional
    public void rateReview(Long userId, Long reviewId, RatingRequest request) {
        PortfolioAiFeedback feedback = feedbackRepository.findByIdAndUserId(reviewId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));

        // 이미 평가된 경우
        if (feedback.isRated()) {
            throw new BusinessException(ErrorCode.ALREADY_RATED);
        }

        // 완료된 리뷰만 평가 가능
        if (feedback.getStatus() != ReviewStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.REVIEW_PROCESSING);
        }

        // XSS 필터링 (간단한 처리)
        String sanitizedComment = request.getComment() != null
                ? request.getComment().replaceAll("<[^>]*>", "")
                : null;

        feedback.rate(request.getRating(), sanitizedComment);
    }

    /**
     * 리뷰 섹션 JSON 파싱
     */
    private PortfolioReviewResponse.ReviewSection parseReviewSection(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            Map<String, Object> map = objectMapper.readValue(json, new TypeReference<>() {});
            String summary = (String) map.get("summary");

            @SuppressWarnings("unchecked")
            List<Map<String, String>> pointsList = (List<Map<String, String>>) map.get("points");
            List<PortfolioReviewResponse.ReviewPoint> points = new ArrayList<>();
            if (pointsList != null) {
                for (Map<String, String> pointMap : pointsList) {
                    points.add(PortfolioReviewResponse.ReviewPoint.builder()
                            .title(pointMap.get("title"))
                            .description(pointMap.get("description"))
                            .build());
                }
            }

            return PortfolioReviewResponse.ReviewSection.builder()
                    .summary(summary)
                    .points(points)
                    .build();
        } catch (JsonProcessingException e) {
            log.error("리뷰 섹션 파싱 실패: {}", json, e);
            return null;
        }
    }

    /**
     * 관련 뉴스 JSON 파싱
     */
    private List<PortfolioReviewResponse.RelatedNewsItem> parseRelatedNews(String json) {
        if (json == null || json.isEmpty()) {
            return List.of();
        }
        try {
            List<Map<String, Object>> newsList = objectMapper.readValue(json, new TypeReference<>() {});
            return newsList.stream()
                    .map(map -> PortfolioReviewResponse.RelatedNewsItem.builder()
                            .newsId(((Number) map.get("newsId")).longValue())
                            .title((String) map.get("title"))
                            .influenceType((String) map.get("influenceType"))
                            .build())
                    .toList();
        } catch (JsonProcessingException e) {
            log.error("관련 뉴스 파싱 실패: {}", json, e);
            return List.of();
        }
    }

    /**
     * 리뷰 섹션에서 요약 추출
     */
    private String extractSummary(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            Map<String, Object> map = objectMapper.readValue(json, new TypeReference<>() {});
            return (String) map.get("summary");
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
