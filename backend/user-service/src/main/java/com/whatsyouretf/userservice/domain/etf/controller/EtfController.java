package com.whatsyouretf.userservice.domain.etf.controller;

import com.whatsyouretf.userservice.common.response.ApiResponse;
import com.whatsyouretf.userservice.common.response.PaginatedResponse;
import com.whatsyouretf.userservice.domain.etf.dto.EtfPriceHistoryRequest;
import com.whatsyouretf.userservice.domain.etf.dto.EtfPriceHistoryResponse;
import com.whatsyouretf.userservice.domain.etf.service.EtfService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.awt.print.Pageable;

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
    @GetMapping("/{etfId}/price-history")
    public ResponseEntity<ApiResponse<PaginatedResponse<EtfPriceHistoryResponse>>> getEtfPriceHistories(
        @Valid EtfPriceHistoryRequest request,
        @PathVariable Long etfId,
        Pageable pageable
    ) {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponse.success(
                PaginatedResponse.createPaginatedResponse(
                    etfService.getEtfHistory(etfId, request.getStartDate(), request.getEndDate(), pageable)
                        .map(EtfPriceHistoryResponse::from))
            ));
    }
}
