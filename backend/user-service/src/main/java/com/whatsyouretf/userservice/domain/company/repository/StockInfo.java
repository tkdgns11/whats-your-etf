package com.whatsyouretf.userservice.domain.company.repository;

import java.math.BigDecimal;

public record StockInfo(
    String ticker,
    String stockName,
    BigDecimal marketCapitalization,
    BigDecimal currentPrice,
    BigDecimal dailyFluctuation,
    String description
) {
}
