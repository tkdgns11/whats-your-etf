package com.whatsyouretf.userservice.domain.etf.dto;

import java.math.BigDecimal;

public record EtfListResponse(
        Long etfId,
        String ticker,
        String etfName,
        BigDecimal etfPrice,
        BigDecimal dailyReturn,
        BigDecimal dailyFluctuation,
        Boolean isFavorite,
        String riskType
) {
        public static EtfListResponse of(EtfSummary etfSummary, EtfCurrentInfo info) {
                return new EtfListResponse(
                        etfSummary.etfId(),
                        etfSummary.ticker(),
                        etfSummary.etfName(),
                        info.currentPrice(),
                        info.dailyReturn(),
                        info.dailyFluctuation(),
                        etfSummary.isFavorite(),
                        etfSummary.riskType()
                );
        }
}
