package com.whatsyouretf.userservice.domain.etf.dto;

import com.whatsyouretf.userservice.domain.etf.entity.Etf;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * ETF 상세 응답 DTO (클러스터 뷰 포함)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EtfDetailResponse {

    /** ETF 티커 */
    private String ticker;

    /** ETF 명칭 */
    private String name;

    /** 현재가 */
    private BigDecimal currentPrice;

    /** 전일 대비 변동금액 */
    private BigDecimal dailyFluctuation;

    /** 전일 대비 변동률 (%) */
    private BigDecimal dailyFluctuationRatio;

    /** iNAV */
    private BigDecimal iNav;

    /** iNAV 변동금액 */
    private BigDecimal iNavChangeAmount;

    /** iNAV 변동률 (%) */
    private BigDecimal iNavChangeRate;

    /** 거래량 */
    private Long volume;

    /** 자산운용사 */
    private String company;

    /** 위험등급 (1~5) */
    private Integer riskGrade;

    /** 위험유형 */
    private String riskType;

    /** 총보수율 (%) */
    private BigDecimal expenseRatio;

    private Double per;

    private Double pbr;

    private Double roe;

    /** 순자산 총액 */
    private Long aum;

    /** 상장일 */
    private LocalDate listingDate;

    /**
     * Entity -> DTO 변환
     */
    public static EtfDetailResponse from(
            Etf etf,
            EtfCurrentInfo info
    ) {
        BigDecimal previousPrice = info.previousPrice() == null ? info.currentPrice() : info.previousPrice();
        BigDecimal priceFluctuation = info.currentPrice().subtract(info.previousPrice());
        BigDecimal navFluctuation = info.nav().subtract(etf.getNav());

        return new EtfDetailResponse(
            etf.getStockCode(),
            etf.getName(),
            info.currentPrice(),
            priceFluctuation,
            info.dailyReturn(),
            info.nav(),
            navFluctuation,
            navFluctuation.divide(etf.getNav(), RoundingMode.DOWN),
            info.volume(),
            etf.getAssetManager(),
            etf.getRiskType().getRiskGrade(),
            etf.getRiskType().getTypeName(),
            etf.getExpenseRatio(),
            etf.getFundamental().getPer(),
            etf.getFundamental().getPbr(),
            etf.getFundamental().getRoe(),
            etf.getAum(),
            etf.getListingDate()
        );
    }
}
