package com.whatsyouretf.userservice.domain.etf.service;

import com.whatsyouretf.userservice.domain.etf.dto.EtfDetailResponse;

/**
 * ETF 서비스 인터페이스
 */
public interface EtfService {

    /**
     * ETF 상세 조회 (클러스터 뷰 포함)
     *
     * @param ticker ETF 종목코드
     * @return ETF 상세 정보
     */
    EtfDetailResponse getEtfDetail(String ticker);
}
