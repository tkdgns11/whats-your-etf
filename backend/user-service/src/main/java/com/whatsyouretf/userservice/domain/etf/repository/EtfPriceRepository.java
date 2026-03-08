package com.whatsyouretf.userservice.domain.etf.repository;

import com.whatsyouretf.userservice.domain.etf.entity.EtfPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * ETF 일별 시세 Repository
 */
@Repository
public interface EtfPriceRepository extends JpaRepository<EtfPrice, Long> {

    /**
     * ETF의 최신 시세 조회
     */
    @Query("SELECT ep FROM EtfPrice ep WHERE ep.etf.id = :etfId ORDER BY ep.tradeDate DESC LIMIT 1")
    Optional<EtfPrice> findLatestByEtfId(@Param("etfId") Long etfId);

    /**
     * ETF의 특정 기간 시세 조회
     */
    @Query("SELECT ep FROM EtfPrice ep WHERE ep.etf.id = :etfId " +
            "AND ep.tradeDate BETWEEN :startDate AND :endDate ORDER BY ep.tradeDate DESC")
    List<EtfPrice> findByEtfIdAndDateRange(
            @Param("etfId") Long etfId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * 여러 ETF의 최신 시세 조회
     */
    @Query("SELECT ep FROM EtfPrice ep WHERE ep.etf.id IN :etfIds " +
            "AND ep.tradeDate = (SELECT MAX(ep2.tradeDate) FROM EtfPrice ep2 WHERE ep2.etf.id = ep.etf.id)")
    List<EtfPrice> findLatestByEtfIds(@Param("etfIds") List<Long> etfIds);
}
