package com.whatsyouretf.userservice.domain.portfolio.service;

import com.whatsyouretf.userservice.domain.etf.entity.Etf;
import com.whatsyouretf.userservice.domain.portfolio.entity.Portfolio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PortfolioServiceImpl implements PortfolioService {

        private final PortfolioStore portfolioStore;

        @Override
        public Portfolio savePortfolio(Long userId, String portfolioName, BigDecimal investAmount, Integer investPeriod) {
                return portfolioStore.storePortfolio(Portfolio.createPortfolio(userId, portfolioName, investAmount, investPeriod));
        }

        @Override
        public void savePortfolioEtfs(Map<String, Etf> etfs, List<PortfolioCommand> list, Portfolio portfolio) {
                portfolioStore.storePortfolioEtfs(portfolio, etfs, list);
        }
}
