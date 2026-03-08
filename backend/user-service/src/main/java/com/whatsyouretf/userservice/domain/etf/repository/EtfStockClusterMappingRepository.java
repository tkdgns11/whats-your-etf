package com.whatsyouretf.userservice.domain.etf.repository;

import com.whatsyouretf.userservice.domain.etf.entity.EtfStockClusterMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ETF 주식 클러스터 매핑 Repository
 */
@Repository
public interface EtfStockClusterMappingRepository extends JpaRepository<EtfStockClusterMapping, Long> {

    /**
     * ETF ID로 클러스터 매핑 조회
     */
    List<EtfStockClusterMapping> findByEtfId(Long etfId);

    /**
     * 섹터 코드로 클러스터 매핑 조회
     */
    List<EtfStockClusterMapping> findBySectorCode(String sectorCode);
}
