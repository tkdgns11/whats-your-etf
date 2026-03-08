package com.whatsyouretf.userservice.domain.ai.dto;

import com.whatsyouretf.userservice.domain.ai.entity.PortfolioAiFeedback;
import com.whatsyouretf.userservice.domain.ai.entity.ReviewStatus;
import com.whatsyouretf.userservice.domain.ai.entity.RiskLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 포트폴리오 AI 리뷰 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioReviewResponse {

    /** 리뷰 ID */
    private Long reviewId;

    /** 처리 상태 */
    private ReviewStatus status;

    /** 처리 중 메시지 */
    private String message;

    /** 예상 완료 시간 (초) */
    private Integer estimatedTime;

    /** Bull 리뷰 (긍정적 분석) */
    private ReviewSection bullReview;

    /** Bear 리뷰 (부정적 분석) */
    private ReviewSection bearReview;

    /** 종합 점수 (0.0 ~ 10.0) */
    private BigDecimal overallScore;

    /** 리스크 레벨 */
    private RiskLevel riskLevel;

    /** 추천 코멘트 */
    private String recommendation;

    /** 관련 뉴스 목록 */
    private List<RelatedNewsItem> relatedNews;

    /** 사용된 LLM 모델 */
    private String llmModel;

    /** 생성일시 */
    private LocalDateTime createdAt;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReviewSection {
        /** 요약 */
        private String summary;
        /** 분석 포인트 목록 */
        private List<ReviewPoint> points;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReviewPoint {
        /** 포인트 제목 */
        private String title;
        /** 포인트 설명 */
        private String description;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RelatedNewsItem {
        /** 뉴스 ID */
        private Long newsId;
        /** 뉴스 제목 */
        private String title;
        /** 영향 유형: POSITIVE / NEGATIVE / NEUTRAL */
        private String influenceType;
    }

    /**
     * 처리 중 응답 생성
     */
    public static PortfolioReviewResponse processing(Long reviewId) {
        return PortfolioReviewResponse.builder()
                .reviewId(reviewId)
                .status(ReviewStatus.PROCESSING)
                .message("AI 분석이 진행 중입니다. 잠시 후 다시 조회해주세요.")
                .estimatedTime(30)
                .build();
    }

    /**
     * Entity -> DTO 변환 (완료된 리뷰)
     */
    public static PortfolioReviewResponse from(PortfolioAiFeedback feedback,
                                                ReviewSection bullReview,
                                                ReviewSection bearReview,
                                                List<RelatedNewsItem> relatedNews) {
        return PortfolioReviewResponse.builder()
                .reviewId(feedback.getId())
                .status(feedback.getStatus())
                .bullReview(bullReview)
                .bearReview(bearReview)
                .overallScore(feedback.getOverallScore())
                .riskLevel(feedback.getRiskLevel())
                .recommendation(feedback.getRecommendation())
                .relatedNews(relatedNews)
                .llmModel(feedback.getLlmModel())
                .createdAt(feedback.getCreatedAt())
                .build();
    }
}
