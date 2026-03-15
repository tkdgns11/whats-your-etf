package com.whatsyouretf.userservice.domain.portfolio.service;

import com.whatsyouretf.userservice.domain.portfolio.entity.Portfolio;
import com.whatsyouretf.userservice.domain.portfolio.repository.PortfolioEtfInfo;

import java.util.List;
import java.util.Map;

public interface PortfolioReader {
        List<Portfolio> getUsersPortfolios(Long userId);

        List<PortfolioEtfInfo> getPortfolioEtf(Long portfolioId);

        Map<Long, List<PortfolioEtfInfo>> getPortfolioInfoMap(List<Long> b);
}
