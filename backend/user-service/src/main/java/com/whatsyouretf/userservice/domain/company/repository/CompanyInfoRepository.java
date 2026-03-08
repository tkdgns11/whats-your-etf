package com.whatsyouretf.userservice.domain.company.repository;

import com.whatsyouretf.userservice.domain.company.entity.CompanyInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 회사 정보 Repository
 */
@Repository
public interface CompanyInfoRepository extends JpaRepository<CompanyInfo, Long> {

    /**
     * 종목코드로 회사 정보 조회
     */
    Optional<CompanyInfo> findByStockCode(String stockCode);

    /**
     * 종목코드 존재 여부 확인
     */
    boolean existsByStockCode(String stockCode);
}
