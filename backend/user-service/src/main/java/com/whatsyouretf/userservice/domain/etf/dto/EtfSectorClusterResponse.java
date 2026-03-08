package com.whatsyouretf.userservice.domain.etf.dto;

import com.whatsyouretf.userservice.domain.etf.entity.EtfSectorCluster;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * ETF 섹터 분포 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EtfSectorClusterResponse {

    private Long etfId;
    private String ticker;
    private String name;
    private LocalDate baseDate;
    private List<SectorItem> sectorCluster;
    private int totalSectors;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SectorItem {
        private String groupCode;
        private String groupName;
        private BigDecimal weightPct;
        private Integer stockCount;
        private List<String> topStocks;

        public static SectorItem from(EtfSectorCluster cluster, List<String> topStocks) {
            return SectorItem.builder()
                    .groupCode(cluster.getGroupCode())
                    .groupName(cluster.getGroupName())
                    .weightPct(cluster.getWeightPct())
                    .stockCount(cluster.getStockCount())
                    .topStocks(topStocks)
                    .build();
        }
    }
}
