package com.whatsyouretf.userservice.domain.etf.dto;

import com.whatsyouretf.userservice.domain.etf.entity.Etf;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * ETF 목록 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EtfListResponse {

    /** ETF 목록 */
    private List<EtfItem> etfs;

    /** 현재 페이지 */
    private int page;

    /** 전체 페이지 수 */
    private int totalPages;

    /** 전체 ETF 수 */
    private long totalElements;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EtfItem {
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

        public static EtfItem from(Etf etf, BigDecimal latestPrice, BigDecimal changeRate) {
            return EtfItem.builder()
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
                    .build();
        }
    }
}
