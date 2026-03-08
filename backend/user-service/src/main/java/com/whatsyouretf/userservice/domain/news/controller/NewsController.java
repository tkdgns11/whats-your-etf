package com.whatsyouretf.userservice.domain.news.controller;

import com.whatsyouretf.userservice.common.response.ApiResponse;
import com.whatsyouretf.userservice.domain.news.dto.*;
import com.whatsyouretf.userservice.domain.news.service.NewsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 뉴스 API 컨트롤러
 * <p>
 * 뉴스 조회 관련 API를 제공합니다.
 * 인증 없이 접근 가능합니다.
 */
@Tag(name = "News", description = "뉴스 API")
@RestController
@RequestMapping("/api/v1/news")
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;

    /**
     * 최신 뉴스 목록 조회
     */
    @Operation(summary = "최신 뉴스 목록 조회", description = "최신 뉴스 목록을 페이징하여 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<NewsPageResponse>> getLatestNews(
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기 (최대 100)") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "카테고리 코드 필터") @RequestParam(required = false) String category
    ) {
        NewsPageResponse response = newsService.getLatestNews(page, size, category);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 뉴스 상세 조회
     */
    @Operation(summary = "뉴스 상세 조회", description = "뉴스 상세 정보를 조회합니다. 조회수가 증가합니다.")
    @GetMapping("/{newsId}")
    public ResponseEntity<ApiResponse<NewsDetailResponse>> getNewsDetail(
            @Parameter(description = "뉴스 ID") @PathVariable Long newsId
    ) {
        NewsDetailResponse response = newsService.getNewsDetail(newsId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 뉴스 검색
     */
    @Operation(summary = "뉴스 검색", description = "키워드로 뉴스를 검색합니다.")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<NewsPageResponse>> searchNews(
            @Parameter(description = "검색 키워드 (2~50자)") @RequestParam String keyword,
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size
    ) {
        NewsPageResponse response = newsService.searchNews(keyword, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * ETF 관련 뉴스 조회
     */
    @Operation(summary = "ETF 관련 뉴스 조회", description = "ETF 구성종목들의 뉴스를 조회합니다.")
    @GetMapping("/etf/{etfId}")
    public ResponseEntity<ApiResponse<EtfNewsResponse>> getEtfNews(
            @Parameter(description = "ETF ID") @PathVariable Long etfId,
            @Parameter(description = "조회 개수 (최대 50)") @RequestParam(defaultValue = "10") int size
    ) {
        EtfNewsResponse response = newsService.getEtfNews(etfId, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 종목 관련 뉴스 조회
     */
    @Operation(summary = "종목 뉴스 조회", description = "특정 종목의 뉴스를 조회합니다.")
    @GetMapping("/stock/{stockCode}")
    public ResponseEntity<ApiResponse<StockNewsResponse>> getStockNews(
            @Parameter(description = "종목 코드 (6자리)") @PathVariable String stockCode,
            @Parameter(description = "조회 개수 (최대 50)") @RequestParam(defaultValue = "10") int size
    ) {
        StockNewsResponse response = newsService.getStockNews(stockCode, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
