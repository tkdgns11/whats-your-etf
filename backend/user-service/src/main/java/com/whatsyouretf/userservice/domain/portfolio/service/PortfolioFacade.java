package com.whatsyouretf.userservice.domain.portfolio.service;

import com.whatsyouretf.userservice.domain.etf.entity.Etf;
import com.whatsyouretf.userservice.domain.etf.service.EtfService;
import com.whatsyouretf.userservice.domain.portfolio.entity.Portfolio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PortfolioFacade {
        private final PortfolioService portfolioService;
        private final EtfService etfService;

        @Transactional
        public void savePortfolio(List<PortfolioCommand> commands, Long userId, String portfolioName, BigDecimal investAmount, Integer investPeriod) {
                // db 에 있는 etf 목록 조회
                Map<String, Etf> etfs = etfService.getEtfListInTickers(commands.stream().map(PortfolioCommand::stockCode).toList());

                // 포트폴리오 저장
                Portfolio portfolio = portfolioService.savePortfolio(userId, portfolioName, investAmount, investPeriod);

                // 포트폴리오 구성 종목 저장
                portfolioService.savePortfolioEtfs(etfs, commands, portfolio);
        }
}
