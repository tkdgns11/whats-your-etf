package com.whatsyouretf.userservice.domain.etf.dto;

import com.whatsyouretf.userservice.domain.etf.entity.Etf;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * ETF 클러스터 상세 응답 DTO (클러스터별 ETF 목록)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EtfClusterDetailResponse {

    private ClusterInfo cluster;
    private List<EtfItem> etfs;
    private int page;
    private int totalPages;
    private long totalElements;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClusterInfo {
        private String id;
        private String name;
        private String description;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EtfItem {
        private Long id;
        private String ticker;
        private String name;
        private String issuer;
        private BigDecimal expenseRatio;
        private Long totalAsset;

        public static EtfItem from(Etf etf) {
            return EtfItem.builder()
                    .id(etf.getId())
                    .ticker(etf.getStockCode())
                    .name(etf.getName())
                    .issuer(etf.getAssetManager())
                    .expenseRatio(etf.getExpenseRatio())
                    .totalAsset(etf.getAum())
                    .build();
        }
    }
}
