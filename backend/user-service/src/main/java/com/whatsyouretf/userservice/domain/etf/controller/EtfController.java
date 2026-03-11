package com.whatsyouretf.userservice.domain.etf.controller;

import com.whatsyouretf.userservice.common.response.ApiResponse;
import com.whatsyouretf.userservice.domain.etf.dto.EtfDetailResponse;
import com.whatsyouretf.userservice.domain.etf.service.EtfService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * ETF 관련 API Controller
 */
@Tag(name = "ETF", description = "ETF API")
@RestController
@RequestMapping("/api/v1/etf")
@RequiredArgsConstructor
public class EtfController {

    private final EtfService etfService;

    /**
     * ETF 상세 조회 (클러스터 뷰 포함)
     * <p>
     * ETF 기본 정보, 섹터 클러스터, 영향력 종목을 조회합니다.
     *
     * @param ticker ETF 종목코드 (예: 091160)
     * @return ETF 상세 정보
     */
    @Operation(summary = "ETF 상세 조회", description = "ETF 상세 정보를 조회합니다. 섹터 클러스터와 영향력 종목을 포함합니다.")
    @GetMapping("/{ticker}")
    public ResponseEntity<ApiResponse<EtfDetailResponse>> getEtfDetail(
            @Parameter(description = "ETF 종목코드", example = "091160")
            @PathVariable String ticker
    ) {
        EtfDetailResponse response = etfService.getEtfDetail(ticker);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
