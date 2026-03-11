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

import java.util.List;

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

        // 피드백 엔티티 생성
        PortfolioAiFeedback feedback = PortfolioAiFeedback.builder()
                .user(user)
                .prompt(prompt)
                .build();

        feedbackRepository.save(feedback);

        // LLM 호출하여 분석 수행
        String promptTemplate = prompt != null ? prompt.getPromptTemplate() : null;
        llmService.analyzePortfolio(feedback.getId(), promptTemplate, request.getPortfolio());

        // 분석 결과 조회
        PortfolioAiFeedback result = feedbackRepository.findById(feedback.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));

        return PortfolioReviewResponse.from(result, parseKeywords(result.getKeywords()));
    }

    @Override
    public PortfolioReviewResponse getReview(Long userId, Long reviewId) {
        PortfolioAiFeedback feedback = feedbackRepository.findByIdAndUserId(reviewId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));

        return PortfolioReviewResponse.from(feedback, parseKeywords(feedback.getKeywords()));
    }

    @Override
    public ReviewHistoryResponse getReviewHistory(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        Page<PortfolioAiFeedback> feedbackPage = feedbackRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        List<ReviewHistoryResponse.ReviewSummary> reviews = feedbackPage.getContent().stream()
                .map(feedback -> ReviewHistoryResponse.ReviewSummary.from(
                        feedback,
                        parseKeywords(feedback.getKeywords())))
                .toList();

        return ReviewHistoryResponse.builder()
                .reviews(reviews)
                .page(page)
                .totalPages(feedbackPage.getTotalPages())
                .totalElements(feedbackPage.getTotalElements())
                .build();
    }

    /**
     * 키워드 JSON 파싱
     */
    private List<String> parseKeywords(String json) {
        if (json == null || json.isEmpty()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            log.error("키워드 파싱 실패: {}", json, e);
            return List.of();
        }
    }
}
