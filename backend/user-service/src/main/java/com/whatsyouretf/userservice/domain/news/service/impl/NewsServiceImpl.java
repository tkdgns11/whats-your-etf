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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
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

    private static final int NEWS_LIST_SIZE = 20;
    private static final int MAX_ETF_NEWS_SIZE = 50;
    private static final int MAX_RELATED_ETFS = 5;
    private static final int MAX_PORTFOLIO_NEWS = 5;

    @Override
    public NewsPageResponse getLatestNews(String categoryCode) {
        Pageable pageable = PageRequest.of(0, NEWS_LIST_SIZE);

        List<NewsArticle> articles;
        if (categoryCode != null && !categoryCode.isBlank()) {
            articles = newsArticleRepository.findByCategory_CodeAndIsActiveTrueOrderByPublishedAtDesc(categoryCode, pageable)
                    .getContent();
        } else {
            articles = newsArticleRepository.findByIsActiveTrueOrderByPublishedAtDesc(pageable)
                    .getContent();
        }

        List<NewsListResponse> newsList = articles.stream()
                .map(NewsListResponse::from)
                .toList();

        return NewsPageResponse.builder()
                .news(newsList)
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

        // 관련 ETF 목록 조회 (news_stock_mapping → stock → etf_stock_composition → etf)
        List<RelatedEtfResponse> relatedEtfs = getRelatedEtfs(newsId);

        return NewsDetailResponse.from(article, aiSummary, keywords, relatedEtfs);
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

    /**
     * 본문 내용 자르기
     */
    private String truncateContent(String content, int maxLength) {
        if (content == null || content.isBlank()) {
            return null;
        }
        if (content.length() <= maxLength) {
            return content;
        }
        return content.substring(0, maxLength) + "...";
    }

    @Override
    public NewsPageResponse searchNews(String keyword) {
        // 키워드 검증
        if (keyword == null || keyword.trim().length() < 2 || keyword.trim().length() > 50) {
            throw new BusinessException(ErrorCode.INVALID_KEYWORD);
        }

        Pageable pageable = PageRequest.of(0, NEWS_LIST_SIZE);

        List<NewsArticle> articles = newsArticleRepository.searchByKeyword(keyword.trim(), pageable)
                .getContent();

        List<NewsListResponse> newsList = articles.stream()
                .map(NewsListResponse::from)
                .toList();

        return NewsPageResponse.builder()
                .news(newsList)
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

    // TODO: 포트폴리오 기능 완성 후 활성화
    @Override
    // @Cacheable(value = "portfolioNews", key = "#portfolioId")
    public PortfolioNewsResponse getPortfolioNews(Long portfolioId) {
        throw new BusinessException(ErrorCode.PORTFOLIO_NOT_FOUND);

        /*
        // 포트폴리오 조회
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PORTFOLIO_NOT_FOUND));

        // 포트폴리오 관련 뉴스 조회 (관련성 높은 순 + 최신 5개)
        List<NewsArticle> articles = newsArticleRepository.findByPortfolioId(portfolioId, MAX_PORTFOLIO_NEWS);

        // DTO 변환
        List<PortfolioNewsResponse.PortfolioNewsItem> newsItems = articles.stream()
                .map(article -> PortfolioNewsResponse.PortfolioNewsItem.builder()
                        .id(article.getId())
                        .title(article.getTitle())
                        .summary(truncateContent(article.getContent(), 100))
                        .source(article.getSource())
                        .thumbnailUrl(article.getThumbnailUrl())
                        .publishedAt(article.getPublishedAt())
                        .build())
                .toList();

        // 오늘 오전 9시 기준 시각 계산
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.LocalDateTime todayNineAm = now.toLocalDate().atTime(9, 0);
        java.time.LocalDateTime updatedAt = now.isBefore(todayNineAm)
                ? todayNineAm.minusDays(1)
                : todayNineAm;

        return PortfolioNewsResponse.builder()
                .portfolioId(portfolio.getId())
                .portfolioName(portfolio.getName())
                .news(newsItems)
                .updatedAt(updatedAt)
                .build();
        */
    }

    /**
     * 매일 오전 9시에 포트폴리오 뉴스 캐시 초기화
     */
    @Scheduled(cron = "0 0 9 * * *")
    @CacheEvict(value = "portfolioNews", allEntries = true)
    public void clearPortfolioNewsCache() {
        log.info("포트폴리오 뉴스 캐시 초기화 완료 (매일 오전 9시)");
    }
}
