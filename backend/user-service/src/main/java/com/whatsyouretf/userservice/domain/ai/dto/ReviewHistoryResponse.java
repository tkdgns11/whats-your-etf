package com.whatsyouretf.userservice.domain.ai.dto;

import com.whatsyouretf.userservice.domain.ai.entity.PortfolioAiFeedback;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

        /** 진단 결과 헤드라인 */
        private String headline;

        /** 서브 헤드라인 */
        private String subHeadline;

        /** 분석 키워드 */
        private List<String> keywords;

        /** 생성일시 */
        private LocalDateTime createdAt;

        /**
         * Entity -> DTO 변환
         */
        public static ReviewSummary from(PortfolioAiFeedback feedback, List<String> keywords) {
            return ReviewSummary.builder()
                    .reviewId(feedback.getId())
                    .headline(feedback.getHeadline())
                    .subHeadline(feedback.getSubHeadline())
                    .keywords(keywords)
                    .createdAt(feedback.getCreatedAt())
                    .build();
        }
    }
}
