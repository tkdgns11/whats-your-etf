package com.whatsyouretf.userservice.domain.portfolio.service;

import com.whatsyouretf.userservice.domain.portfolio.dto.*;

import java.time.LocalDate;

/**
 * 포트폴리오 서비스 인터페이스
 */
public interface PortfolioService {

    /**
     * 내 포트폴리오 목록 조회
     */
    PortfolioListResponse getPortfolios(Long userId, int page, int size);

    /**
     * 포트폴리오 생성
     */
    PortfolioCreateResponse createPortfolio(Long userId, PortfolioCreateRequest request);

    /**
     * 포트폴리오 상세 조회
     */
    PortfolioDetailResponse getPortfolioDetail(Long userId, Long portfolioId);

    /**
     * 포트폴리오 수정
     */
    void updatePortfolio(Long userId, Long portfolioId, PortfolioUpdateRequest request);

    /**
     * 포트폴리오 삭제
     */
    void deletePortfolio(Long userId, Long portfolioId);

    /**
     * 포트폴리오 수익률 조회
     */
    PortfolioPerformanceResponse getPortfolioPerformance(Long userId, Long portfolioId,
                                                          LocalDate startDate, LocalDate endDate);

    /**
     * 포트폴리오 ETF 추가
     */
    void addEtfToPortfolio(Long userId, Long portfolioId, PortfolioEtfAddRequest request);

    /**
     * 포트폴리오 ETF 비중 수정
     */
    void updateEtfWeights(Long userId, Long portfolioId, PortfolioEtfUpdateRequest request);

    /**
     * 포트폴리오 ETF 삭제
     */
    void removeEtfFromPortfolio(Long userId, Long portfolioId, Long etfId);
}
