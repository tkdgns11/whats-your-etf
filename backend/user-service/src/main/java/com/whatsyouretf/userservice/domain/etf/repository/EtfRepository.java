package com.whatsyouretf.userservice.domain.etf.repository;

import com.whatsyouretf.userservice.domain.etf.entity.Etf;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ETF Repository
 * <p>
 * ETF 테이블은 팀원이 데이터를 관리하며, user-service에서는 조회만 수행합니다.
 */
@Repository
public interface EtfRepository extends JpaRepository<Etf, Long> {

    /**
     * 종목코드로 ETF 조회
     */
    Optional<Etf> findByStockCode(String stockCode);

    /**
     * 활성 상태인 ETF만 조회
     */
    List<Etf> findByIsActiveTrue();

    /**
     * ID 목록으로 ETF 조회
     */
    @Query("SELECT e FROM Etf e WHERE e.id IN :ids AND e.isActive = true")
    List<Etf> findAllByIdInAndActive(@Param("ids") List<Long> ids);

    /**
     * 활성 ETF 페이징 조회
     */
    Page<Etf> findByIsActiveTrue(Pageable pageable);

    /**
     * 전략 유형으로 필터링하여 조회
     */
    Page<Etf> findByStrategyTypeAndIsActiveTrue(String strategyType, Pageable pageable);

    /**
     * ETF 검색 (이름 또는 종목코드)
     */
    @Query("SELECT e FROM Etf e WHERE e.isActive = true " +
           "AND (LOWER(e.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR e.stockCode LIKE CONCAT('%', :keyword, '%'))")
    List<Etf> searchByKeyword(@Param("keyword") String keyword);

    /**
     * 섹터 기반 유사 ETF 조회
     */
    @Query("SELECT e FROM Etf e WHERE e.isActive = true " +
           "AND e.sector = :sector AND e.id != :excludeId " +
           "ORDER BY e.aum DESC")
    List<Etf> findSimilarBySector(@Param("sector") String sector,
                                   @Param("excludeId") Long excludeId,
                                   Pageable pageable);
}
