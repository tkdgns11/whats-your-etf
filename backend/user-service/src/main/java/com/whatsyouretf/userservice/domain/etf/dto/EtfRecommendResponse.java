package com.whatsyouretf.userservice.domain.etf.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * ETF 추천 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EtfRecommendResponse {

    private List<RecommendItem> recommendations;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecommendItem {
        private EtfInfo etf;
        private String reason;
        private BigDecimal similarity;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EtfInfo {
        private Long id;
        private String ticker;
        private String name;
        private String issuer;
        private String strategyType;
    }
}
