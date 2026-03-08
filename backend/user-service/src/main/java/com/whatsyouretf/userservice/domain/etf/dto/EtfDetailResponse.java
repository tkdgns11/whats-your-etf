package com.whatsyouretf.userservice.domain.etf.dto;

import com.whatsyouretf.userservice.domain.etf.entity.Etf;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * ETF 상세 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EtfDetailResponse {

    private Long id;
    private String ticker;
    private String name;
    private String issuer;
    private String strategyType;
    private BigDecimal expenseRatio;
    private Long totalAsset;
    private LocalDate listingDate;
    private BigDecimal latestPrice;
    private BigDecimal priceChangeRate;
    private String description;
    private String category;
    private String dividendCycle;
    private String riskGrade;

    public static EtfDetailResponse from(Etf etf, BigDecimal latestPrice, BigDecimal changeRate) {
        return EtfDetailResponse.builder()
                .id(etf.getId())
                .ticker(etf.getStockCode())
                .name(etf.getName())
                .issuer(etf.getAssetManager())
                .strategyType(etf.getStrategyType())
                .expenseRatio(etf.getExpenseRatio())
                .totalAsset(etf.getAum())
                .listingDate(etf.getListingDate())
                .latestPrice(latestPrice)
                .priceChangeRate(changeRate)
                .category(etf.getCategory())
                .dividendCycle(etf.getDividendFreq())
                .riskGrade(etf.getRiskGrade())
                .build();
    }
}
