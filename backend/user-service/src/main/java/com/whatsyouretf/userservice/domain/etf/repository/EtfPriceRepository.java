package com.whatsyouretf.userservice.domain.etf.repository;

import com.whatsyouretf.userservice.domain.etf.entity.EtfPrice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * ETF 시세 Repository
 */
@Repository
public interface EtfPriceRepository extends JpaRepository<EtfPrice, Long> {

    /**
     * ETF의 최신 시세 조회
     */
    @Query("""
        SELECT ep FROM EtfPrice ep
        WHERE ep.etf.id = :etfId
        ORDER BY ep.tradeDate DESC
        LIMIT 1
        """)
    Optional<EtfPrice> findLatestByEtfId(@Param("etfId") Long etfId);

    /**
     * 여러 ETF의 최신 시세 조회
     */
    @Query("""
        SELECT ep FROM EtfPrice ep
        JOIN FETCH ep.etf
        WHERE ep.etf.id IN :etfIds
          AND ep.tradeDate = (
              SELECT MAX(ep2.tradeDate) FROM EtfPrice ep2 WHERE ep2.etf = ep.etf
          )
        """)
    List<EtfPrice> findLatestByEtfIds(@Param("etfIds") List<Long> etfIds);

    /**
     * ETF 종목코드와 날짜 범위로 가격 이력 조회
     *
     * @param ticker    종목코드
     * @param startDate 시작일 (null이면 조건 없음)
     * @param endDate   종료일 (null이면 조건 없음)
     * @param pageable  페이징
     * @return 가격 이력 페이지 (최신 순 정렬)
     */
    @Query("""
        SELECT ep FROM EtfPrice ep
        WHERE ep.etf.stockCode = :ticker
        AND (:startDate IS NULL OR ep.tradeDate >= :startDate)
        AND (:endDate IS NULL OR ep.tradeDate <= :endDate)
        ORDER BY ep.tradeDate DESC
        """)
    Page<EtfPrice> findByEtfTickerAndDateRange(
            @Param("ticker") String ticker,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );
}
