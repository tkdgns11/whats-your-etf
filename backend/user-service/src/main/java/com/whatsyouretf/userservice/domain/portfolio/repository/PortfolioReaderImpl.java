package com.whatsyouretf.userservice.domain.portfolio.repository;

import com.whatsyouretf.userservice.common.exception.BusinessException;
import com.whatsyouretf.userservice.common.exception.ErrorCode;
import com.whatsyouretf.userservice.domain.portfolio.entity.Portfolio;
import com.whatsyouretf.userservice.domain.portfolio.entity.PortfolioEtf;
import com.whatsyouretf.userservice.domain.portfolio.service.PortfolioReader;
import com.whatsyouretf.userservice.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Component
@RequiredArgsConstructor
public class PortfolioReaderImpl implements PortfolioReader {
        private final PortfolioRepository portfolioRepository;
        private final PortfolioQuerydslRepository portfolioQuerydslRepository;
        @Override
        public List<Portfolio> getUsersPortfolios(Long userId) {
                return portfolioRepository.findByUser(User.of(userId));
        }

        @Override
        public Map<Long, List<PortfolioEtfInfo>> getPortfolioInfoMap(List<Long> portfolioList) {
                return portfolioQuerydslRepository.getPortfolioInfoMap(portfolioList);
        }

        @Override
        public List<PortfolioEtf> getPortfolioDetail(Long portfolioId) {
                try {
                        return portfolioRepository.findByPortfolioId(portfolioId);
                } catch (NoSuchElementException e) {
                        throw new BusinessException(ErrorCode.PORTFOLIO_NOT_FOUND);
                }
        }
}
