package com.whatsyouretf.userservice.domain.portfolio.controller;

import java.math.BigDecimal;
import java.util.List;

public record SavePortfolioRequest(
        String portfolioName,
        BigDecimal investAmount,
        Integer investPeriod,
        List<PortfolioEtfCount> etfs
) {
}
