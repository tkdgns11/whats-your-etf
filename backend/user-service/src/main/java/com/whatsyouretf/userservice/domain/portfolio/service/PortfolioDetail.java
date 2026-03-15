package com.whatsyouretf.userservice.domain.portfolio.service;

import com.whatsyouretf.userservice.domain.portfolio.controller.PortfolioEtfCount;
import com.whatsyouretf.userservice.domain.portfolio.entity.Portfolio;
import com.whatsyouretf.userservice.domain.portfolio.entity.PortfolioEtf;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PortfolioDetail(
        Long portfolioId,
        String portfolioName,
        List<PortfolioEtfCount> counts,
        BigDecimal investAmount,
        LocalDateTime createdAt
) {
        public static PortfolioDetail of(
                List<PortfolioEtf> etfs
        ) {
                Portfolio portfolio = etfs.getFirst().getPortfolio();
                return new PortfolioDetail(
                        portfolio.getId(),
                        portfolio.getName(),
                        etfs.stream().map(etf -> new PortfolioEtfCount(etf.getEtf().getStockCode(), etf.getEtfCount())).toList(),
                        portfolio.getInvestAmount(),
                        portfolio.getCreatedAt()
                );
        }
}
