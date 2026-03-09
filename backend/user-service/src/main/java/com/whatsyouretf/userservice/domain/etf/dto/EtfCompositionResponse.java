package com.whatsyouretf.userservice.domain.etf.dto;

import com.whatsyouretf.userservice.domain.company.entity.Stock;
import com.whatsyouretf.userservice.domain.etf.entity.EtfStockComposition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * ETF 구성종목 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EtfCompositionResponse {

    private Long etfId;
    private String ticker;
    private String name;
    private LocalDate baseDate;
    private List<CompositionItem> compositions;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CompositionItem {
        private String componentTicker;
        private String componentName;
        private BigDecimal weightPct;
        private String industry;

        public static CompositionItem from(EtfStockComposition composition) {
            Stock stock = composition.getStock();
            return CompositionItem.builder()
                    .componentTicker(stock != null ? stock.getTicker() : "기타")
                    .componentName(stock != null && stock.getCompany() != null
                            ? stock.getCompany().getStockName() : "기타")
                    .weightPct(composition.getWeightPct())
                    .industry(stock != null && stock.getCompany() != null
                            ? stock.getCompany().getIndustryGroup() : null)
                    .build();
        }
    }
}
