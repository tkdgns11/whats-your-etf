package com.whatsyouretf.userservice.domain.etf.controller;

import com.whatsyouretf.userservice.common.response.ApiResponse;
import com.whatsyouretf.userservice.common.response.PaginatedResponse;
import com.whatsyouretf.userservice.domain.etf.dto.*;
import com.whatsyouretf.userservice.domain.etf.service.EtfService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ETF 관련 API Controller
 */
@Tag(name = "ETF", description = "ETF API")
@RestController
@RequestMapping("/api/v1/etfs")
@RequiredArgsConstructor
public class EtfController {

    private final EtfService etfService;

    @Operation(summary = "etf 가격 이력 조회", description = "시작일부터 종료일을 기준으로 페이징하여 응답합니다.")
    @GetMapping("/{ticker}/price-history")
    public ResponseEntity<ApiResponse<PaginatedResponse<EtfPriceHistoryResponse>>> getEtfPriceHistories(
        @Valid EtfPriceHistoryRequest request,
        @Parameter(description = "etf 종목 코드") @PathVariable String ticker,
        Pageable pageable
    ) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(ApiResponse.success(
                        PaginatedResponse.createPaginatedResponse(
                            etfService.getEtfHistory(ticker, request.getStartDate(), request.getEndDate(), pageable)
                                .map(EtfPriceHistoryResponse::from))
                    ));
    }

    @GetMapping("/{ticker}")
    @Operation(summary = "etf 단건 조회", description = "etf의 종목 코드를 기준으로 etf 상세 조회를 응답합니다")
    public ResponseEntity<ApiResponse<EtfDetailResponse>> getEtfDetail(@Parameter(description = "etf 종목 코드") @PathVariable String ticker) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(ApiResponse.success(
                            EtfDetailResponse.from(
                                etfService.getEtfDetail(ticker),
                                etfService.getEtfCurrentInfo(ticker))));
    }

    @GetMapping("/{ticker}/clusters")
    @Operation(summary = "etf 클러스터 조회", description = "etf의 종목 코드를 기준으로 etf 클러스터를 응답합니다")
    public ResponseEntity<ApiResponse<List<EtfSectorResponse>>> getEtfCluster(@Parameter(description = "etf 종목 코드") @PathVariable String ticker) {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponse.success(
                    etfService.getSectorClusters(ticker)));
    }

    @PostMapping
    @Operation(summary = "etf 목록 조회", description = "etf 조건에 맞는 etf 목록을 페이징하여 응답합니다")
    public ResponseEntity<ApiResponse<PaginatedResponse<EtfListResponse>>> getEtfList(
            @Parameter(description = "etf 검색 조건") @RequestBody EtfListRequest request,
            Pageable pageable
    ) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(ApiResponse.success(PaginatedResponse.createPaginatedResponse(
                                    etfService.getEtfList(request.toQuery(), pageable)
                                            .map(etfSummary -> EtfListResponse.of(
                                                    etfSummary,
                                                    etfService.getEtfCurrentInfo(etfSummary.ticker()))))));
    }
}
