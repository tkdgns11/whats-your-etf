package com.whatsyouretf.userservice.domain.portfolio.repository;

import com.whatsyouretf.userservice.domain.portfolio.entity.PortfolioEtf;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 포트폴리오 ETF Repository
 */
@Repository
public interface PortfolioEtfRepository extends JpaRepository<PortfolioEtf, Long> {

    /**
     * 포트폴리오의 ETF 목록 조회
     */
    List<PortfolioEtf> findByPortfolioIdOrderByWeightPctDesc(Long portfolioId);

    /**
     * 포트폴리오 ID와 ETF ID로 조회
     */
    Optional<PortfolioEtf> findByPortfolioIdAndEtfId(Long portfolioId, Long etfId);

    /**
     * 포트폴리오의 ETF 개수 조회
     */
    long countByPortfolioId(Long portfolioId);

    /**
     * 포트폴리오의 ETF 삭제
     */
    void deleteByPortfolioIdAndEtfId(Long portfolioId, Long etfId);
}
