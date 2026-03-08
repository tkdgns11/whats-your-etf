package com.whatsyouretf.userservice.domain.etf.service;

import com.whatsyouretf.userservice.domain.etf.dto.*;

import java.time.LocalDate;

/**
 * ETF 서비스 인터페이스
 */
public interface EtfService {

    /**
     * ETF 목록 조회
     */
    EtfListResponse getEtfList(int page, int size, String strategyType,
                               String sector, String riskGrade, String sortBy);

    /**
     * ETF 상세 조회
     */
    EtfDetailResponse getEtfDetail(Long etfId);

    /**
     * ETF 가격 이력 조회
     */
    EtfPriceHistoryResponse getEtfPriceHistory(Long etfId, LocalDate startDate, LocalDate endDate);

    /**
     * ETF 구성종목 조회
     */
    EtfCompositionResponse getEtfCompositions(Long etfId, LocalDate baseDate);

    /**
     * ETF 검색
     */
    EtfSearchResponse searchEtf(String keyword, String strategyType, String issuer);

    /**
     * ETF 섹터 분포 조회
     */
    EtfSectorClusterResponse getEtfSectorCluster(Long etfId);

    /**
     * ETF 추천 (사용자 기반)
     */
    EtfRecommendResponse getEtfRecommendations(Long userId);
}
