package com.whatsyouretf.userservice.domain.etf.service;

import com.whatsyouretf.userservice.domain.etf.entity.EtfPrice;
import org.springframework.data.domain.Page;

import java.awt.print.Pageable;
import java.time.LocalDate;

/**
 * ETF 서비스 인터페이스
 */
public interface EtfService {

    /**
     * ETF 상세 조회 (클러스터 뷰 포함)
     *
     * @param etfId ETF 종목코드
     * @param startDate 시작일
     * @param endDate 종료일
     * @param pageable 페이지 정보
     * @return ETF 상세 정보
     */
    Page<EtfPrice> getEtfHistory(Long etfId, LocalDate startDate, LocalDate endDate, Pageable pageable);
}
