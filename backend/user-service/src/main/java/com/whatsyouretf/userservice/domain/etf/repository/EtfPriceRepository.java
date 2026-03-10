package com.whatsyouretf.userservice.domain.etf.repository;

import com.whatsyouretf.userservice.domain.etf.entity.EtfPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * ETF 시세 Repository
 */
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
    @Query(value = """
        SELECT DISTINCT ON (etf_id) *
        FROM etf_prices
        WHERE etf_id IN (:etfIds)
        ORDER BY etf_id, trade_date DESC
        """, nativeQuery = true)
    List<EtfPrice> findLatestByEtfIds(@Param("etfIds") List<Long> etfIds);
}
