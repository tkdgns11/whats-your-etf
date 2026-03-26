package com.whatsyouretf.userservice.domain.etf.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * ETF 클러스터 응답 DTO (버블 차트 + 영향력 종목)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EtfClusterResponse {

    /** ETF 영문명 */
    private String englishName;

    /** 섹터 클러스터 목록 (버블 차트용) */
    private List<EtfSectorResponse> sectors;

    /** 영향력 종목 목록 (비중 상위) */
    private List<EtfInfluentialStockResponse> influentialStocks;

    /** 비주식 구성종목 (채권/선물/현금 등) */
    private List<EtfSectorStockResponse> otherCompositions;
}
