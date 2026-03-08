package com.whatsyouretf.userservice.domain.portfolio.controller;

import com.whatsyouretf.userservice.common.auth.CustomUserDetails;
import com.whatsyouretf.userservice.common.response.ApiResponse;
import com.whatsyouretf.userservice.domain.portfolio.dto.*;
import com.whatsyouretf.userservice.domain.portfolio.service.PortfolioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 포트폴리오 API 컨트롤러
 */
@Tag(name = "Portfolio", description = "포트폴리오 API")
@RestController
@RequestMapping("/api/v1/portfolios")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;

    /**
     * 내 포트폴리오 목록 조회
     */
    @Operation(summary = "포트폴리오 목록 조회", description = "내 포트폴리오 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<PortfolioListResponse>> getPortfolios(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size
    ) {
        PortfolioListResponse response = portfolioService.getPortfolios(userDetails.getUserId(), page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 포트폴리오 생성
     */
    @Operation(summary = "포트폴리오 생성", description = "새로운 포트폴리오를 생성합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<PortfolioCreateResponse>> createPortfolio(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PortfolioCreateRequest request
    ) {
        PortfolioCreateResponse response = portfolioService.createPortfolio(userDetails.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 포트폴리오 상세 조회
     */
    @Operation(summary = "포트폴리오 상세 조회", description = "포트폴리오 상세 정보를 조회합니다.")
    @GetMapping("/{portfolioId}")
    public ResponseEntity<ApiResponse<PortfolioDetailResponse>> getPortfolioDetail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "포트폴리오 ID") @PathVariable Long portfolioId
    ) {
        PortfolioDetailResponse response = portfolioService.getPortfolioDetail(userDetails.getUserId(), portfolioId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 포트폴리오 수정
     */
    @Operation(summary = "포트폴리오 수정", description = "포트폴리오 정보를 수정합니다.")
    @PutMapping("/{portfolioId}")
    public ResponseEntity<ApiResponse<Void>> updatePortfolio(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "포트폴리오 ID") @PathVariable Long portfolioId,
            @Valid @RequestBody PortfolioUpdateRequest request
    ) {
        portfolioService.updatePortfolio(userDetails.getUserId(), portfolioId, request);
        return ResponseEntity.ok(ApiResponse.success("포트폴리오가 수정되었습니다."));
    }

    /**
     * 포트폴리오 삭제
     */
    @Operation(summary = "포트폴리오 삭제", description = "포트폴리오를 삭제합니다.")
    @DeleteMapping("/{portfolioId}")
    public ResponseEntity<ApiResponse<Void>> deletePortfolio(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "포트폴리오 ID") @PathVariable Long portfolioId
    ) {
        portfolioService.deletePortfolio(userDetails.getUserId(), portfolioId);
        return ResponseEntity.ok(ApiResponse.success("포트폴리오가 삭제되었습니다."));
    }

    /**
     * 포트폴리오 수익률 조회
     */
    @Operation(summary = "포트폴리오 수익률 조회", description = "포트폴리오의 수익률을 조회합니다.")
    @GetMapping("/{portfolioId}/performance")
    public ResponseEntity<ApiResponse<PortfolioPerformanceResponse>> getPortfolioPerformance(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "포트폴리오 ID") @PathVariable Long portfolioId,
            @Parameter(description = "시작일") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "종료일") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        PortfolioPerformanceResponse response = portfolioService.getPortfolioPerformance(
                userDetails.getUserId(), portfolioId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 포트폴리오 ETF 추가
     */
    @Operation(summary = "포트폴리오 ETF 추가", description = "포트폴리오에 ETF를 추가합니다.")
    @PostMapping("/{portfolioId}/etfs")
    public ResponseEntity<ApiResponse<Void>> addEtfToPortfolio(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "포트폴리오 ID") @PathVariable Long portfolioId,
            @Valid @RequestBody PortfolioEtfAddRequest request
    ) {
        portfolioService.addEtfToPortfolio(userDetails.getUserId(), portfolioId, request);
        return ResponseEntity.ok(ApiResponse.success("ETF가 포트폴리오에 추가되었습니다."));
    }

    /**
     * 포트폴리오 ETF 비중 수정
     */
    @Operation(summary = "포트폴리오 ETF 비중 수정", description = "포트폴리오의 ETF 비중을 수정합니다.")
    @PutMapping("/{portfolioId}/etfs")
    public ResponseEntity<ApiResponse<Void>> updateEtfWeights(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "포트폴리오 ID") @PathVariable Long portfolioId,
            @Valid @RequestBody PortfolioEtfUpdateRequest request
    ) {
        portfolioService.updateEtfWeights(userDetails.getUserId(), portfolioId, request);
        return ResponseEntity.ok(ApiResponse.success("ETF 비중이 수정되었습니다."));
    }

    /**
     * 포트폴리오 ETF 삭제
     */
    @Operation(summary = "포트폴리오 ETF 삭제", description = "포트폴리오에서 ETF를 삭제합니다.")
    @DeleteMapping("/{portfolioId}/etfs/{etfId}")
    public ResponseEntity<ApiResponse<Void>> removeEtfFromPortfolio(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "포트폴리오 ID") @PathVariable Long portfolioId,
            @Parameter(description = "ETF ID") @PathVariable Long etfId
    ) {
        portfolioService.removeEtfFromPortfolio(userDetails.getUserId(), portfolioId, etfId);
        return ResponseEntity.ok(ApiResponse.success("ETF가 포트폴리오에서 삭제되었습니다."));
    }
}
