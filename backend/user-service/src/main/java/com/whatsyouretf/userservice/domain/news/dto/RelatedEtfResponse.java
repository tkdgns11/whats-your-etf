package com.whatsyouretf.userservice.domain.news.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 관련 ETF 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RelatedEtfResponse {

    /** ETF ID */
    private Long etfId;

    /** ETF 종목코드 */
    private String ticker;

    /** ETF 명칭 */
    private String name;

    /** 해당 종목의 ETF 내 비중 */
    private BigDecimal weightPct;
}
