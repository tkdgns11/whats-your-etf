package com.whatsyouretf.userservice.domain.etf.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * ETF 클러스터 목록 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EtfClusterListResponse {

    private List<ClusterItem> clusters;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClusterItem {
        private String id;           // group_code
        private String name;         // group_name
        private String description;
        private int etfCount;
        private BigDecimal avgExpenseRatio;
        private BigDecimal avgReturn1Y;
    }
}
