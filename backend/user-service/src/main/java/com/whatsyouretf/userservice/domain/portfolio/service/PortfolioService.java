package com.whatsyouretf.userservice.domain.portfolio.service;

import com.whatsyouretf.userservice.domain.etf.entity.Etf;
import com.whatsyouretf.userservice.domain.portfolio.entity.Portfolio;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface PortfolioService {
        void savePortfolioEtfs(Map<String, Etf> etfs, List<PortfolioCommand> list, Portfolio portfolio);

        Portfolio savePortfolio(Long userId, String portfolioName, BigDecimal investAmount, Integer investPeriod);
}
