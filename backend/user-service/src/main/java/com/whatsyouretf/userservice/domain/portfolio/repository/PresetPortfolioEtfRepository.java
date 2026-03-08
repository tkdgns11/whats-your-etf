package com.whatsyouretf.userservice.domain.portfolio.repository;

import com.whatsyouretf.userservice.domain.portfolio.entity.PresetPortfolioEtf;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 꾸러미 ETF 구성 Repository
 */
@Repository
public interface PresetPortfolioEtfRepository extends JpaRepository<PresetPortfolioEtf, Long> {

    /**
     * 꾸러미 ID로 ETF 구성 조회 (ETF 정보 포함)
     */
    @Query("SELECT pe FROM PresetPortfolioEtf pe JOIN FETCH pe.etf WHERE pe.presetPortfolio.id = :presetPortfolioId")
    List<PresetPortfolioEtf> findByPresetPortfolioIdWithEtf(@Param("presetPortfolioId") Long presetPortfolioId);
}
