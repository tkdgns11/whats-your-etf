package com.whatsyouretf.userservice.domain.etf.controller;

import com.whatsyouretf.userservice.common.auth.CustomUserDetails;
import com.whatsyouretf.userservice.common.response.ApiResponse;
import com.whatsyouretf.userservice.domain.etf.dto.*;
import com.whatsyouretf.userservice.domain.etf.service.EtfService;
import com.whatsyouretf.userservice.domain.news.dto.EtfNewsResponse;
import com.whatsyouretf.userservice.domain.news.service.NewsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * ETF API 컨트롤러
 */
@Tag(name = "ETF", description = "ETF API")
@RestController
@RequestMapping("/api/v1/etf")
@RequiredArgsConstructor
public class EtfController {

    private final EtfService etfService;
    private final NewsService newsService;

    /**
     * ETF 목록 조회
     */
    @Operation(summary = "ETF 목록 조회", description = "ETF 목록을 페이징하여 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<EtfListResponse>> getEtfList(
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "전략 유형") @RequestParam(required = false) String strategyType,
            @Parameter(description = "섹터 필터") @RequestParam(required = false) String sector,
            @Parameter(description = "위험등급") @RequestParam(required = false) String riskGrade,
            @Parameter(description = "정렬 기준") @RequestParam(required = false) String sortBy
    ) {
        EtfListResponse response = etfService.getEtfList(page, size, strategyType, sector, riskGrade, sortBy);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * ETF 상세 조회
     */
    @Operation(summary = "ETF 상세 조회", description = "ETF 상세 정보를 조회합니다.")
    @GetMapping("/{etfId}")
    public ResponseEntity<ApiResponse<EtfDetailResponse>> getEtfDetail(
            @Parameter(description = "ETF ID") @PathVariable Long etfId
    ) {
        EtfDetailResponse response = etfService.getEtfDetail(etfId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * ETF 가격 이력 조회
     */
    @Operation(summary = "ETF 가격 이력 조회", description = "ETF의 일별 가격 이력을 조회합니다.")
    @GetMapping("/{etfId}/prices")
    public ResponseEntity<ApiResponse<EtfPriceHistoryResponse>> getEtfPriceHistory(
            @Parameter(description = "ETF ID") @PathVariable Long etfId,
            @Parameter(description = "시작일") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "종료일") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        EtfPriceHistoryResponse response = etfService.getEtfPriceHistory(etfId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * ETF 구성종목 조회
     */
    @Operation(summary = "ETF 구성종목 조회", description = "ETF의 구성종목 정보를 조회합니다.")
    @GetMapping("/{etfId}/compositions")
    public ResponseEntity<ApiResponse<EtfCompositionResponse>> getEtfCompositions(
            @Parameter(description = "ETF ID") @PathVariable Long etfId,
            @Parameter(description = "기준일") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate baseDate
    ) {
        EtfCompositionResponse response = etfService.getEtfCompositions(etfId, baseDate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * ETF 검색
     */
    @Operation(summary = "ETF 검색", description = "ETF를 검색합니다.")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<EtfSearchResponse>> searchEtf(
            @Parameter(description = "검색어 (2~100자)") @RequestParam String keyword,
            @Parameter(description = "전략 유형 필터") @RequestParam(required = false) String strategyType,
            @Parameter(description = "운용사 필터") @RequestParam(required = false) String issuer
    ) {
        EtfSearchResponse response = etfService.searchEtf(keyword, strategyType, issuer);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * ETF 섹터 분포 조회
     */
    @Operation(summary = "ETF 섹터 분포 조회", description = "ETF 구성종목의 섹터 분포를 조회합니다.")
    @GetMapping("/{etfId}/sector-cluster")
    public ResponseEntity<ApiResponse<EtfSectorClusterResponse>> getEtfSectorCluster(
            @Parameter(description = "ETF ID") @PathVariable Long etfId
    ) {
        EtfSectorClusterResponse response = etfService.getEtfSectorCluster(etfId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * ETF 뉴스 타임라인 조회
     */
    @Operation(summary = "ETF 뉴스 타임라인 조회", description = "ETF 구성종목 관련 뉴스를 타임라인으로 조회합니다.")
    @GetMapping("/{etfId}/timeline")
    public ResponseEntity<ApiResponse<EtfNewsResponse>> getEtfTimeline(
            @Parameter(description = "ETF ID") @PathVariable Long etfId,
            @Parameter(description = "조회 개수") @RequestParam(defaultValue = "10") int size
    ) {
        EtfNewsResponse response = newsService.getEtfNews(etfId, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * ETF 추천
     */
    @Operation(summary = "ETF 추천", description = "사용자의 관심 ETF 기반 맞춤 추천을 제공합니다.")
    @GetMapping("/recommend")
    public ResponseEntity<ApiResponse<EtfRecommendResponse>> getEtfRecommendations(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        EtfRecommendResponse response = etfService.getEtfRecommendations(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
