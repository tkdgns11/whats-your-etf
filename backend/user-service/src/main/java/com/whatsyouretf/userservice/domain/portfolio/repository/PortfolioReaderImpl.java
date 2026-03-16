package com.whatsyouretf.userservice.domain.portfolio.repository;

import com.whatsyouretf.userservice.domain.portfolio.entity.Portfolio;
import com.whatsyouretf.userservice.domain.portfolio.entity.PortfolioEtf;
import com.whatsyouretf.userservice.domain.portfolio.service.PortfolioReader;
import com.whatsyouretf.userservice.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PortfolioReaderImpl implements PortfolioReader {
        private final PortfolioRepository portfolioRepository;
        private final PortfolioQuerydslRepository portfolioQuerydslRepository;
        @Override
        public List<Portfolio> getUsersPortfolios(Long userId) {
                return portfolioRepository.findByUserOrderByCreatedAtDesc(User.of(userId));
        }

        @Override
        public Map<Long, List<PortfolioEtfInfo>> getPortfolioInfoMap(List<Long> portfolioList) {
                return portfolioQuerydslRepository.getPortfolioInfoMap(portfolioList);
        }

        @Override
        public List<PortfolioEtf> getPortfolioDetail(Long portfolioId) {
                return portfolioRepository.findByPortfolioId(portfolioId);
        }
}
