package com.whatsyouretf.userservice.domain.etf.dto;

import java.math.BigDecimal;

public record EtfCurrentInfo(
        String ticker,
        BigDecimal currentPrice,
        BigDecimal previousPrice,
        Long volume,
        BigDecimal nav
) {

}
