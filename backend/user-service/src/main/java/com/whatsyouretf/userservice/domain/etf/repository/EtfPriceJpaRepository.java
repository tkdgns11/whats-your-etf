package com.whatsyouretf.userservice.domain.etf.repository;

import com.whatsyouretf.userservice.domain.etf.entity.EtfPrice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

/**
 * ETF 가격 이력 JPA 저장소
 */
@Repository
public interface EtfPriceJpaRepository extends JpaRepository<EtfPrice, Long> {
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
