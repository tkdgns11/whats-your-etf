package com.whatsyouretf.userservice.domain.etf.dto;

public record EtfSummary(
        Long etfId,
        String ticker,
        String etfName,
        Boolean isFavorite,
        String riskType
) {
}
