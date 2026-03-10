package com.whatsyouretf.userservice.domain.news.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.whatsyouretf.userservice.common.exception.BusinessException;
import com.whatsyouretf.userservice.common.exception.ErrorCode;
import com.whatsyouretf.userservice.domain.etf.entity.Etf;
import com.whatsyouretf.userservice.domain.etf.entity.EtfPrice;
import com.whatsyouretf.userservice.domain.etf.repository.EtfPriceRepository;
import com.whatsyouretf.userservice.domain.etf.repository.EtfRepository;
import com.whatsyouretf.userservice.domain.news.dto.*;
import com.whatsyouretf.userservice.domain.news.entity.NewsArticle;
import com.whatsyouretf.userservice.domain.news.repository.NewsArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 뉴스 서비스 구현체
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class NewsServiceImpl implements com.whatsyouretf.userservice.domain.news.service.NewsService {

    private final NewsArticleRepository newsArticleRepository;
    private final EtfRepository etfRepository;
    private final EtfPriceRepository etfPriceRepository;
    private final ObjectMapper objectMapper;

    private static final int MAX_PAGE_SIZE = 100;
    private static final int MAX_ETF_NEWS_SIZE = 50;
    private static final int MAX_RELATED_ETFS = 5;

    @Override
    public NewsPageResponse getLatestNews(int page, int size, String categoryCode) {
        // 페이지 크기 제한
        size = Math.min(size, MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, size);

        Page<NewsArticle> newsPage;
        if (categoryCode != null && !categoryCode.isBlank()) {
            newsPage = newsArticleRepository.findByCategory_CodeAndIsActiveTrueOrderByPublishedAtDesc(categoryCode, pageable);
        } else {
            newsPage = newsArticleRepository.findByIsActiveTrueOrderByPublishedAtDesc(pageable);
        }

        List<NewsListResponse> newsList = newsPage.getContent().stream()
                .map(NewsListResponse::from)
                .toList();

        return NewsPageResponse.builder()
                .news(newsList)
                .page(page)
                .totalPages(newsPage.getTotalPages())
                .totalElements(newsPage.getTotalElements())
                .build();
    }

    @Override
    @Transactional
    public NewsDetailResponse getNewsDetail(Long newsId) {
        NewsArticle article = newsArticleRepository.findById(newsId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NEWS_NOT_FOUND));

        // 조회수 증가
        article.incrementViewCount();

        // AI 요약 파싱 (JSON: {"bullets": ["요약1", "요약2", ...]} 형식)
        List<String> aiSummary = parseAiSummary(article.getContentSummary());

        // 키워드 파싱 (JSON 배열)
        List<String> keywords = parseKeywords(article.getKeywords());

        // 관련 종목 목록 조회 (news_stock_mapping 기반)
        List<RelatedStockResponse> relatedStocks = article.getStockMappings().stream()
                .map(mapping -> RelatedStockResponse.from(mapping.getCompanyInfo()))
                .toList();

        // 관련 ETF 목록 조회 (news_stock_mapping → stock → etf_stock_composition → etf)
        List<RelatedEtfResponse> relatedEtfs = getRelatedEtfs(newsId);

        return NewsDetailResponse.from(article, aiSummary, keywords, relatedStocks, relatedEtfs);
    }

    /**
     * 뉴스와 관련된 ETF 목록 조회
     * <p>
     * news_stock_mapping의 종목이 포함된 ETF를 비중 높은 순으로 조회
     */
    private List<RelatedEtfResponse> getRelatedEtfs(Long newsId) {
        // 관련 ETF 조회
        List<Etf> etfs = etfRepository.findRelatedEtfsByNewsId(newsId, MAX_RELATED_ETFS);

        if (etfs.isEmpty()) {
            return List.of();
        }

        // ETF ID 목록
        List<Long> etfIds = etfs.stream().map(Etf::getId).toList();

        // 최신 시세 조회 (한번에 조회하여 N+1 문제 방지)
        Map<Long, EtfPrice> priceMap = etfPriceRepository.findLatestByEtfIds(etfIds).stream()
                .collect(Collectors.toMap(
                        price -> price.getEtf().getId(),
                        Function.identity()
                ));

        // DTO 변환
        return etfs.stream()
                .map(etf -> RelatedEtfResponse.from(etf, priceMap.get(etf.getId())))
                .toList();
    }

    /**
     * AI 요약 JSON 파싱
     * 형식: {"bullets": ["요약1", "요약2", "요약3"]}
     */
    private List<String> parseAiSummary(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            JsonNode root = objectMapper.readTree(json);
            if (root.has("bullets") && root.get("bullets").isArray()) {
                return objectMapper.convertValue(
                        root.get("bullets"),
                        new TypeReference<List<String>>() {}
                );
            }
            return List.of();
        } catch (JsonProcessingException e) {
            log.error("AI 요약 파싱 실패: {}", json, e);
            return List.of();
        }
    }

    /**
     * 키워드 JSON 파싱
     * 형식: ["키워드1", "키워드2", ...]
     */
    private List<String> parseKeywords(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            log.error("키워드 파싱 실패: {}", json, e);
            return List.of();
        }
    }

    @Override
    public NewsPageResponse searchNews(String keyword, int page, int size) {
        // 키워드 검증
        if (keyword == null || keyword.trim().length() < 2 || keyword.trim().length() > 50) {
            throw new BusinessException(ErrorCode.INVALID_KEYWORD);
        }

        size = Math.min(size, MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, size);

        Page<NewsArticle> newsPage = newsArticleRepository.searchByKeyword(keyword.trim(), pageable);

        List<NewsListResponse> newsList = newsPage.getContent().stream()
                .map(NewsListResponse::from)
                .toList();

        return NewsPageResponse.builder()
                .news(newsList)
                .page(page)
                .totalPages(newsPage.getTotalPages())
                .totalElements(newsPage.getTotalElements())
                .keyword(keyword)
                .build();
    }

    // TODO: 팀원이 etf/company repository 구현 후 활성화
    @Override
    public EtfNewsResponse getEtfNews(Long etfId, int size) {
        throw new BusinessException(ErrorCode.AI_SERVICE_UNAVAILABLE);
    }

    // TODO: 팀원이 etf/company repository 구현 후 활성화
    @Override
    public StockNewsResponse getStockNews(String ticker, int size) {
        throw new BusinessException(ErrorCode.AI_SERVICE_UNAVAILABLE);
    }
}
