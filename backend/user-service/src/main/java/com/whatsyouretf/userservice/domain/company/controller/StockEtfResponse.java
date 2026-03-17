package com.whatsyouretf.userservice.domain.company.controller;

import java.math.BigDecimal;

public record StockEtfResponse(
    String etfName,
    String manager,
    String ticker,
    BigDecimal ratio
) {
}
