package com.whatsyouretf.userservice.domain.etf.dto;

import com.whatsyouretf.userservice.domain.etf.entity.Etf;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * ETF 검색 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EtfSearchResponse {

    private List<EtfSearchItem> etfs;
    private long totalCount;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EtfSearchItem {
        private Long id;
        private String ticker;
        private String name;
        private String issuer;
        private String strategyType;

        public static EtfSearchItem from(Etf etf) {
            return EtfSearchItem.builder()
                    .id(etf.getId())
                    .ticker(etf.getStockCode())
                    .name(etf.getName())
                    .issuer(etf.getAssetManager())
                    .strategyType(etf.getStrategyType())
                    .build();
        }
    }
}
