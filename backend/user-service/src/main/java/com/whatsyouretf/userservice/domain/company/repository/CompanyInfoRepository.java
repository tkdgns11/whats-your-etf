package com.whatsyouretf.userservice.domain.company.repository;

import com.whatsyouretf.userservice.domain.company.entity.CompanyInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 회사 정보 Repository
 */
@Repository
public interface CompanyInfoRepository extends JpaRepository<CompanyInfo, Long> {

    /**
     * 종목코드로 회사 조회
     */
    Optional<CompanyInfo> findByStockCode(String stockCode);

    /**
     * 종목코드 목록으로 회사 목록 조회
     */
    List<CompanyInfo> findByStockCodeIn(List<String> stockCodes);

    /**
     * 종목명으로 검색 (부분 일치)
     */
    List<CompanyInfo> findByStockNameContaining(String stockName);

    /**
     * 산업분류 코드로 회사 목록 조회
     */
    List<CompanyInfo> findByIndustryCode(String industryCode);

    /**
     * 활성화된 회사만 조회
     */
    List<CompanyInfo> findByIsActiveTrue();
}
