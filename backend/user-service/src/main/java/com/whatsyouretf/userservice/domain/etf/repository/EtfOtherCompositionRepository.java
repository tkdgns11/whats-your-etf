package com.whatsyouretf.userservice.domain.etf.repository;

import com.whatsyouretf.userservice.domain.etf.entity.EtfOtherComposition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ETF 비주식 구성종목 Repository
 */
@Repository
public interface EtfOtherCompositionRepository extends JpaRepository<EtfOtherComposition, Long> {

    /**
     * ETF ID로 비주식 구성종목 조회
     */
    List<EtfOtherComposition> findByEtfId(Long etfId);

    /**
     * ETF ID와 자산 유형으로 조회
     */
    List<EtfOtherComposition> findByEtfIdAndAssetType(Long etfId, String assetType);
}
