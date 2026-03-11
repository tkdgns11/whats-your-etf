package com.whatsyouretf.userservice.domain.etf.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record EtfCurrentInfo(
        String ticker,
        BigDecimal currentPrice,
        BigDecimal previousPrice,
        Long volume,
        BigDecimal nav,
        BigDecimal dailyReturn,
        BigDecimal dailyFluctuation
) {
        public static EtfCurrentInfo update(
                String ticker,
                BigDecimal currentPrice,
                BigDecimal previousPrice,
                Long volume,
                BigDecimal nav
        ) {
                return new EtfCurrentInfo(
                        ticker,
                        currentPrice,
                        previousPrice,
                        volume,
                        nav,
                        currentPrice.subtract(previousPrice).divide(previousPrice, RoundingMode.DOWN),
                        currentPrice.subtract(previousPrice)
                );
        }
}
