package com.whatsyouretf.userservice.domain.etf.repository;

import com.whatsyouretf.userservice.domain.etf.entity.EtfStockClusterMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * ETF 주식 클러스터 매핑 Repository
 * <p>
 * etf_stock_cluster_mapping 테이블을 활용하여 효율적인 섹터별 종목 조회
 */
public interface EtfStockClusterMappingRepository extends JpaRepository<EtfStockClusterMapping, Long> {

    /**
     * ETF의 특정 섹터코드(세분류) 종목들 조회
     * - 테마형 ETF용: sector_code로 직접 조회
     */
    @Query("""
        SELECT m FROM EtfStockClusterMapping m
        JOIN FETCH m.composition c
        JOIN FETCH c.stock s
        JOIN FETCH s.company
        JOIN FETCH m.sector
        WHERE m.etf.stockCode = :ticker
          AND m.sector.code = :sectorCode
        ORDER BY c.weightPct DESC
        """)
    List<EtfStockClusterMapping> findByEtfTickerAndSectorCode(
            @Param("ticker") String ticker,
            @Param("sectorCode") String sectorCode
    );

    /**
     * ETF의 특정 그룹코드 종목들 조회
     * - 시장형 ETF용: group_code로 필터
     */
    @Query("""
        SELECT m FROM EtfStockClusterMapping m
        JOIN FETCH m.composition c
        JOIN FETCH c.stock s
        JOIN FETCH s.company
        JOIN FETCH m.sector
        WHERE m.etf.stockCode = :ticker
          AND m.sector.groupCode = :groupCode
        ORDER BY c.weightPct DESC
        """)
    List<EtfStockClusterMapping> findByEtfTickerAndGroupCode(
            @Param("ticker") String ticker,
            @Param("groupCode") String groupCode
    );

    /**
     * ETF의 모든 클러스터 매핑 조회 (sector FK 포함)
     */
    @Query("""
        SELECT m FROM EtfStockClusterMapping m
        JOIN FETCH m.composition c
        JOIN FETCH c.stock s
        JOIN FETCH s.company
        JOIN FETCH m.sector
        WHERE m.etf.stockCode = :ticker
        ORDER BY c.weightPct DESC
        """)
    List<EtfStockClusterMapping> findAllByEtfTicker(@Param("ticker") String ticker);
}
