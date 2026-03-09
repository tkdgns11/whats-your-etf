package com.whatsyouretf.userservice.domain.news.service.impl;

import com.whatsyouretf.userservice.common.exception.BusinessException;
import com.whatsyouretf.userservice.common.exception.ErrorCode;
import com.whatsyouretf.userservice.domain.company.entity.Stock;
import com.whatsyouretf.userservice.domain.company.repository.StockRepository;
import com.whatsyouretf.userservice.domain.etf.entity.Etf;
import com.whatsyouretf.userservice.domain.etf.entity.EtfStockComposition;
import com.whatsyouretf.userservice.domain.etf.repository.EtfStockCompositionRepository;
import com.whatsyouretf.userservice.domain.etf.repository.EtfRepository;
import com.whatsyouretf.userservice.domain.news.dto.*;
import com.whatsyouretf.userservice.domain.news.entity.NewsArticle;
import com.whatsyouretf.userservice.domain.news.entity.NewsStockMapping;
import com.whatsyouretf.userservice.domain.news.repository.NewsArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 뉴스 서비스 구현체
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NewsServiceImpl implements com.whatsyouretf.userservice.domain.news.service.NewsService {

    private final NewsArticleRepository newsArticleRepository;
    private final StockRepository stockRepository;
    private final EtfRepository etfRepository;
    private final EtfStockCompositionRepository etfStockCompositionRepository;

    private static final int MAX_PAGE_SIZE = 100;
    private static final int MAX_ETF_NEWS_SIZE = 50;

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

        // 관련 종목 목록 조회
        List<RelatedStockResponse> relatedStocks = article.getStockMappings().stream()
                .map(mapping -> RelatedStockResponse.from(mapping.getCompanyInfo()))
                .toList();

        // 관련 ETF 목록 조회 (관련 종목을 구성종목으로 포함하는 ETF)
        List<RelatedEtfResponse> relatedEtfs = new ArrayList<>();
        Map<Long, BigDecimal> etfWeightMap = new HashMap<>();

        for (NewsStockMapping mapping : article.getStockMappings()) {
            // Stock을 통해 ETF 구성종목 조회
            Stock stock = stockRepository.findByCompanyId(mapping.getCompanyInfo().getId()).orElse(null);
            if (stock != null) {
                List<EtfStockComposition> compositions = etfStockCompositionRepository.findLatestByStockId(stock.getId());
                for (EtfStockComposition composition : compositions) {
                    Etf etf = composition.getEtf();
                    // 중복 제거 (같은 ETF가 여러 종목으로 연결될 수 있음)
                    if (!etfWeightMap.containsKey(etf.getId())) {
                        etfWeightMap.put(etf.getId(), composition.getWeightPct());
                        relatedEtfs.add(RelatedEtfResponse.builder()
                                .etfId(etf.getId())
                                .ticker(etf.getStockCode())
                                .name(etf.getName())
                                .weightPct(composition.getWeightPct())
                                .build());
                    }
                }
            }
        }

        // 비중 순 정렬
        relatedEtfs.sort((a, b) -> b.getWeightPct().compareTo(a.getWeightPct()));

        return NewsDetailResponse.from(article, relatedStocks, relatedEtfs);
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

    @Override
    public EtfNewsResponse getEtfNews(Long etfId, int size) {
        // ETF 조회
        Etf etf = etfRepository.findById(etfId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ETF_NOT_FOUND));

        size = Math.min(size, MAX_ETF_NEWS_SIZE);
        Pageable pageable = PageRequest.of(0, size);

        // ETF 구성종목 비중 조회 (companyId -> weightPct 맵 생성)
        List<EtfStockComposition> compositions = etfStockCompositionRepository.findLatestByEtfId(etfId);
        Map<Long, BigDecimal> companyWeightMap = new HashMap<>();
        Map<Long, String> companyTickerMap = new HashMap<>();
        for (EtfStockComposition comp : compositions) {
            if (comp.getStock() != null && comp.getStock().getCompany() != null) {
                Long companyId = comp.getStock().getCompany().getId();
                companyWeightMap.put(companyId, comp.getWeightPct());
                companyTickerMap.put(companyId, comp.getStock().getTicker());
            }
        }

        // ETF 관련 뉴스 조회
        List<NewsArticle> newsArticles = newsArticleRepository.findByEtfId(etfId, pageable);
        long totalCount = newsArticleRepository.countByEtfId(etfId);

        List<EtfNewsResponse.EtfNewsItem> newsItems = newsArticles.stream()
                .map(article -> {
                    // 가장 높은 비중의 관련 종목 찾기
                    EtfNewsResponse.RelatedStockInfo relatedStock = null;
                    BigDecimal maxWeight = BigDecimal.ZERO;

                    for (NewsStockMapping mapping : article.getStockMappings()) {
                        Long companyId = mapping.getCompanyInfo().getId();
                        BigDecimal weight = companyWeightMap.get(companyId);
                        if (weight != null && weight.compareTo(maxWeight) > 0) {
                            maxWeight = weight;
                            relatedStock = EtfNewsResponse.RelatedStockInfo.builder()
                                    .stockCode(companyTickerMap.get(companyId))
                                    .companyName(mapping.getCompanyInfo().getStockName())
                                    .weightPct(weight)
                                    .build();
                        }
                    }

                    return EtfNewsResponse.EtfNewsItem.builder()
                            .id(article.getId())
                            .title(article.getTitle())
                            .source(article.getSource())
                            .publishedAt(article.getPublishedAt())
                            .relatedStock(relatedStock)
                            .build();
                })
                .toList();

        return EtfNewsResponse.builder()
                .etf(EtfNewsResponse.EtfInfo.builder()
                        .id(etf.getId())
                        .ticker(etf.getStockCode())
                        .name(etf.getName())
                        .build())
                .news(newsItems)
                .totalCount(totalCount)
                .build();
    }

    @Override
    public StockNewsResponse getStockNews(String ticker, int size) {
        // 종목 조회 (ticker로 Stock 조회)
        Stock stock = stockRepository.findByTicker(ticker)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        size = Math.min(size, MAX_ETF_NEWS_SIZE);
        Pageable pageable = PageRequest.of(0, size);

        // 종목 뉴스 조회 (companyId 기준)
        List<NewsArticle> newsArticles = newsArticleRepository.findByCompanyId(stock.getCompany().getId(), pageable);
        long totalCount = newsArticleRepository.countByCompanyId(stock.getCompany().getId());

        List<StockNewsResponse.StockNewsItem> newsItems = newsArticles.stream()
                .map(article -> StockNewsResponse.StockNewsItem.builder()
                        .id(article.getId())
                        .title(article.getTitle())
                        .source(article.getSource())
                        .publishedAt(article.getPublishedAt())
                        .build())
                .toList();

        return StockNewsResponse.builder()
                .stock(StockNewsResponse.StockInfo.builder()
                        .stockCode(stock.getTicker())
                        .companyName(stock.getCompany().getStockName())
                        .build())
                .news(newsItems)
                .totalCount(totalCount)
                .build();
    }
}
