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

    /**
     * 모든 고유 클러스터(그룹) 목록 조회
     */
    @Query("SELECT DISTINCT sc.groupCode, sc.groupName FROM EtfSectorCluster sc " +
           "WHERE sc.clusterType = 'GROUP_CODE' AND sc.groupCode IS NOT NULL " +
           "ORDER BY sc.groupName")
    List<Object[]> findDistinctClusters();

    /**
     * 클러스터별 ETF ID 목록 조회
     */
    @Query("SELECT DISTINCT sc.etf.id FROM EtfSectorCluster sc " +
           "WHERE sc.groupCode = :groupCode AND sc.clusterType = 'GROUP_CODE'")
    List<Long> findEtfIdsByGroupCode(@Param("groupCode") String groupCode);

    /**
     * 클러스터별 ETF 개수 조회
     */
    @Query("SELECT sc.groupCode, COUNT(DISTINCT sc.etf.id) FROM EtfSectorCluster sc " +
           "WHERE sc.clusterType = 'GROUP_CODE' AND sc.groupCode IS NOT NULL " +
           "GROUP BY sc.groupCode")
    List<Object[]> countEtfsByCluster();
}
