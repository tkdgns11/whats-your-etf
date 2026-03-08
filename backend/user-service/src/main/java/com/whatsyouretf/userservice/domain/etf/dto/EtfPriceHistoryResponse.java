package com.whatsyouretf.userservice.domain.etf.dto;

import com.whatsyouretf.userservice.domain.etf.entity.EtfPrice;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * ETF 가격 이력 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EtfPriceHistoryResponse {

    private Long etfId;
    private String ticker;
    private String name;
    private List<PriceItem> prices;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PriceItem {
        private LocalDate date;
        private BigDecimal close;
        private Long volume;

        public static PriceItem from(EtfPrice price) {
            return PriceItem.builder()
                    .date(price.getTradeDate())
                    .close(price.getClose())
                    .volume(price.getVolume())
                    .build();
        }
    }
}
