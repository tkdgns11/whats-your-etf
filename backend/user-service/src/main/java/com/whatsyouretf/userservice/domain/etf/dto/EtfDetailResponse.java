package com.whatsyouretf.userservice.domain.etf.dto;

import com.whatsyouretf.userservice.domain.etf.entity.Etf;
import com.whatsyouretf.userservice.domain.etf.entity.EtfPrice;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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

    /** 영문명 */
    private String englishName;

    /** 위험등급 (1~5) */
    private Integer riskLevel;

    /** 현재가 */
    private Long currentPrice;

    /** 전일 대비 변동금액 */
    private Long changeAmount;

    /** 전일 대비 변동률 (%) */
    private BigDecimal changeRate;

    /** iNAV */
    private Long iNav;

    /** iNAV 변동금액 */
    private Long iNavChangeAmount;

    /** iNAV 변동률 (%) */
    private BigDecimal iNavChangeRate;

    /** 1개월 수익률 (%) */
    private BigDecimal returnRate1M;

    /** 거래량 */
    private Long volume;

    /** 섹터 클러스터 목록 (버블 차트용) */
    private List<EtfSectorResponse> sectors;

    /** 영향력 종목 목록 */
    private List<EtfInfluentialStockResponse> influentialStocks;

    /** 자산운용사 */
    private String manager;

    /** 변동성 */
    private String volatility;

    /** 총보수율 (%) */
    private BigDecimal expenseRatio;

    /** 순자산 (원) */
    private Long netAsset;

    /** 상장일 */
    private LocalDate listedDate;

    /**
     * Entity -> DTO 변환
     */
    public static EtfDetailResponse from(
            Etf etf,
            EtfPrice latestPrice,
            EtfPrice previousPrice,
            List<EtfSectorResponse> sectors,
            List<EtfInfluentialStockResponse> influentialStocks
    ) {
        // 등락 계산
        Long currentPrice = latestPrice != null && latestPrice.getClose() != null
                ? latestPrice.getClose().longValue() : null;
        Long prevPrice = previousPrice != null && previousPrice.getClose() != null
                ? previousPrice.getClose().longValue() : null;

        Long changeAmount = null;
        BigDecimal changeRate = null;
        if (currentPrice != null && prevPrice != null && prevPrice != 0) {
            changeAmount = currentPrice - prevPrice;
            changeRate = latestPrice.getChangeRate();
        }

        // 위험등급 매핑 (HIGH_RISK -> 5, MODERATE -> 3, STABLE -> 1)
        Integer riskLevel = mapRiskLevel(etf.getRiskGrade());

        // 변동성 문자열
        String volatility = mapVolatility(etf.getVolatility1y());

        return EtfDetailResponse.builder()
                .ticker(etf.getStockCode())
                .name(etf.getName())
                .englishName(null) // ETF 엔티티에 영문명 없음
                .riskLevel(riskLevel)
                .currentPrice(currentPrice)
                .changeAmount(changeAmount)
                .changeRate(changeRate)
                .iNav(etf.getNav() != null ? etf.getNav().longValue() : null)
                .iNavChangeAmount(null) // iNAV 변동은 별도 계산 필요
                .iNavChangeRate(null)
                .returnRate1M(null) // 별도 계산 필요
                .volume(latestPrice != null ? latestPrice.getVolume() : null)
                .sectors(sectors)
                .influentialStocks(influentialStocks)
                .manager(etf.getAssetManager())
                .volatility(volatility)
                .expenseRatio(etf.getExpenseRatio())
                .netAsset(etf.getAum())
                .listedDate(etf.getListingDate())
                .build();
    }

    private static Integer mapRiskLevel(String riskGrade) {
        if (riskGrade == null) return 3;
        return switch (riskGrade.toUpperCase()) {
            case "HIGH_RISK", "VERY_HIGH" -> 5;
            case "MODERATELY_HIGH" -> 4;
            case "MODERATE" -> 3;
            case "MODERATELY_LOW" -> 2;
            case "STABLE", "LOW" -> 1;
            default -> 3;
        };
    }

    private static String mapVolatility(BigDecimal volatility1y) {
        if (volatility1y == null) return "보통";
        double vol = volatility1y.doubleValue();
        if (vol >= 30) return "매우 높음";
        if (vol >= 20) return "높음";
        if (vol >= 10) return "보통";
        if (vol >= 5) return "낮음";
        return "매우 낮음";
    }
}
