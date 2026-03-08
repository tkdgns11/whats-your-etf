package com.whatsyouretf.userservice.domain.etf.repository;

import com.whatsyouretf.userservice.domain.etf.entity.EtfStockComposition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * ETF 주식 구성종목 Repository
 */
@Repository
public interface EtfStockCompositionRepository extends JpaRepository<EtfStockComposition, Long> {

    /**
     * ETF ID와 기준일로 구성종목 조회
     */
    List<EtfStockComposition> findByEtfIdAndBaseDate(Long etfId, LocalDate baseDate);

    /**
     * ETF ID로 최신 기준일의 구성종목 조회
     */
    @Query("SELECT ec FROM EtfStockComposition ec WHERE ec.etf.id = :etfId " +
           "AND ec.baseDate = (SELECT MAX(e.baseDate) FROM EtfStockComposition e WHERE e.etf.id = :etfId)")
    List<EtfStockComposition> findLatestByEtfId(@Param("etfId") Long etfId);

    /**
     * 특정 주식을 포함하는 ETF 구성종목 조회
     */
    @Query("SELECT ec FROM EtfStockComposition ec JOIN FETCH ec.etf WHERE ec.stock.id = :stockId " +
           "AND ec.baseDate = (SELECT MAX(e.baseDate) FROM EtfStockComposition e WHERE e.stock.id = :stockId)")
    List<EtfStockComposition> findLatestByStockId(@Param("stockId") Long stockId);
}
