package com.whatsyouretf.userservice.domain.news.service;

import com.whatsyouretf.userservice.domain.news.dto.*;

/**
 * 뉴스 서비스 인터페이스
 */
public interface NewsService {

    /**
     * 최신 뉴스 목록 조회
     *
     * @param page         페이지 번호 (0부터 시작)
     * @param size         페이지 크기
     * @param categoryCode 카테고리 코드 필터 (nullable)
     * @return 뉴스 페이지 응답
     */
    NewsPageResponse getLatestNews(int page, int size, String categoryCode);

    /**
     * 뉴스 상세 조회
     *
     * @param newsId 뉴스 ID
     * @return 뉴스 상세 응답
     */
    NewsDetailResponse getNewsDetail(Long newsId);

    /**
     * 뉴스 검색
     *
     * @param keyword 검색 키워드
     * @param page    페이지 번호
     * @param size    페이지 크기
     * @return 뉴스 페이지 응답 (keyword 포함)
     */
    NewsPageResponse searchNews(String keyword, int page, int size);

    /**
     * ETF 관련 뉴스 조회
     *
     * @param etfId ETF ID
     * @param size  조회 개수
     * @return ETF 뉴스 응답
     */
    EtfNewsResponse getEtfNews(Long etfId, int size);

    /**
     * 종목 관련 뉴스 조회
     *
     * @param ticker 종목 코드 (티커)
     * @param size   조회 개수
     * @return 종목 뉴스 응답
     */
    StockNewsResponse getStockNews(String ticker, int size);
}
