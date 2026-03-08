package com.whatsyouretf.userservice.domain.company.repository;

import com.whatsyouretf.userservice.domain.company.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 주식 Repository
 */
@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    /**
     * 티커로 주식 조회
     */
    Optional<Stock> findByTicker(String ticker);

    /**
     * 회사 ID로 주식 목록 조회
     */
    Optional<Stock> findByCompanyId(Long companyId);
}
