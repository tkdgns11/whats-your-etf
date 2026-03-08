package com.whatsyouretf.userservice.domain.ai.dto;

import com.whatsyouretf.userservice.domain.ai.entity.PortfolioAiFeedback;
import com.whatsyouretf.userservice.domain.ai.entity.RiskLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 리뷰 히스토리 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewHistoryResponse {

    /** 리뷰 목록 */
    private List<ReviewSummary> reviews;

    /** 현재 페이지 */
    private int page;

    /** 전체 페이지 수 */
    private int totalPages;

    /** 전체 리뷰 수 */
    private long totalElements;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReviewSummary {
        /** 리뷰 ID */
        private Long reviewId;

        /** 포트폴리오 스냅샷 ID */
        private Long portfolioSnapshotId;

        /** 종합 점수 */
        private BigDecimal overallScore;

        /** 리스크 레벨 */
        private RiskLevel riskLevel;

        /** Bull 리뷰 요약 */
        private String bullSummary;

        /** Bear 리뷰 요약 */
        private String bearSummary;

        /** 생성일시 */
        private LocalDateTime createdAt;

        /**
         * Entity -> DTO 변환
         */
        public static ReviewSummary from(PortfolioAiFeedback feedback, String bullSummary, String bearSummary) {
            return ReviewSummary.builder()
                    .reviewId(feedback.getId())
                    .portfolioSnapshotId(feedback.getPortfolioSnapshotId())
                    .overallScore(feedback.getOverallScore())
                    .riskLevel(feedback.getRiskLevel())
                    .bullSummary(bullSummary)
                    .bearSummary(bearSummary)
                    .createdAt(feedback.getCreatedAt())
                    .build();
        }
    }
}
