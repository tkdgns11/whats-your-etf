package com.whatsyouretf.userservice.domain.ai.service;

import com.whatsyouretf.userservice.domain.ai.dto.*;

/**
 * AI 피드백 서비스 인터페이스
 */
public interface AiFeedbackService {

    /**
     * 포트폴리오 AI 리뷰 요청
     *
     * @param userId  사용자 ID
     * @param request 리뷰 요청 정보
     * @return 리뷰 응답
     */
    PortfolioReviewResponse requestReview(Long userId, PortfolioReviewRequest request);

    /**
     * 리뷰 결과 조회
     *
     * @param userId   사용자 ID
     * @param reviewId 리뷰 ID
     * @return 리뷰 응답
     */
    PortfolioReviewResponse getReview(Long userId, Long reviewId);

    /**
     * 리뷰 히스토리 조회
     *
     * @param userId 사용자 ID
     * @param page   페이지 번호
     * @param size   페이지 크기
     * @return 리뷰 히스토리 응답
     */
    ReviewHistoryResponse getReviewHistory(Long userId, int page, int size);
}
