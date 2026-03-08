package com.whatsyouretf.userservice.domain.etf.repository;

import com.whatsyouretf.userservice.domain.etf.entity.EtfSectorCluster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ETF 섹터 분포 Repository
 */
@Repository
public interface EtfSectorClusterRepository extends JpaRepository<EtfSectorCluster, Long> {

    /**
     * ETF의 최신 섹터 분포 조회 (GROUP_CODE 타입만)
     */
    @Query("SELECT sc FROM EtfSectorCluster sc " +
           "WHERE sc.etf.id = :etfId " +
           "AND sc.clusterType = 'GROUP_CODE' " +
           "AND sc.baseDate = (SELECT MAX(sc2.baseDate) FROM EtfSectorCluster sc2 " +
           "                   WHERE sc2.etf.id = :etfId AND sc2.clusterType = 'GROUP_CODE') " +
           "ORDER BY sc.weightPct DESC")
    List<EtfSectorCluster> findLatestByEtfId(@Param("etfId") Long etfId);
}
