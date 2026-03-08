package com.whatsyouretf.userservice.domain.etf.repository;

import com.whatsyouretf.userservice.domain.etf.entity.EtfOtherClusterMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ETF 비주식 클러스터 매핑 Repository
 */
@Repository
public interface EtfOtherClusterMappingRepository extends JpaRepository<EtfOtherClusterMapping, Long> {

    /**
     * ETF ID로 클러스터 매핑 조회
     */
    List<EtfOtherClusterMapping> findByEtfId(Long etfId);

    /**
     * 섹터 코드로 클러스터 매핑 조회
     */
    List<EtfOtherClusterMapping> findBySectorCode(String sectorCode);
}
