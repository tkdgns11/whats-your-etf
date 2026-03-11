package com.whatsyouretf.userservice.domain.etf.controller;

import com.whatsyouretf.userservice.common.response.ApiResponse;
import com.whatsyouretf.userservice.common.response.PaginatedResponse;
import com.whatsyouretf.userservice.domain.etf.dto.*;
import com.whatsyouretf.userservice.domain.etf.service.EtfService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * ETF 관련 API Controller
 */
@Tag(name = "ETF", description = "ETF API")
@RestController
@RequestMapping("/api/v1/etfs")
@RequiredArgsConstructor
public class EtfController {

    private final EtfService etfService;

    @Operation()
    @GetMapping("/{ticker}/price-history")
    public ResponseEntity<ApiResponse<PaginatedResponse<EtfPriceHistoryResponse>>> getEtfPriceHistories(
        @Valid EtfPriceHistoryRequest request,
        @PathVariable String ticker,
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
    public ResponseEntity<ApiResponse<EtfDetailResponse>> getEtfDetail(@PathVariable String ticker) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(ApiResponse.success(
                            EtfDetailResponse.from(
                                etfService.getEtfDetail(ticker),
                                etfService.getEtfCurrentInfo(ticker))));
    }


    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<EtfListResponse>>> getEtfList(
            @RequestBody EtfListRequest request,
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
