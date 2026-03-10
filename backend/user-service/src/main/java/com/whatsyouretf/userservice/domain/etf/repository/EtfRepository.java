package com.whatsyouretf.userservice.domain.etf.repository;

import com.whatsyouretf.userservice.domain.etf.entity.Etf;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * ETF Repository
 */
public interface EtfRepository extends JpaRepository<Etf, Long> {

    /**
     * 종목코드(티커)로 ETF 조회
     */
    Optional<Etf> findByStockCode(String stockCode);

    /**
     * 뉴스와 관련된 ETF 목록 조회
     * <p>
     * news_stock_mapping → stock (company_id) → etf_stock_composition → etf
     * 비중(weight_pct)이 높은 순으로 정렬
     *
     * @param newsId 뉴스 ID
     * @param limit 조회 개수
     * @return 관련 ETF 목록
     */
    @Query(value = """
        SELECT DISTINCT e.* FROM etf e
        JOIN etf_stock_composition esc ON e.id = esc.etf_id
        JOIN stock s ON esc.stock_id = s.id
        JOIN news_stock_mapping nsm ON s.company_id = nsm.company_id
        WHERE nsm.news_id = :newsId
          AND e.is_active = true
        ORDER BY esc.weight_pct DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Etf> findRelatedEtfsByNewsId(@Param("newsId") Long newsId, @Param("limit") int limit);
}
