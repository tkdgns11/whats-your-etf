package com.whatsyouretf.userservice.domain.company.service;

import java.math.BigDecimal;

public record EtfIncludesStock(
    String etfName,
    String manager,
    String ticker,
    BigDecimal ratio
) {
}
