package com.whatsyouretf.userservice.domain.etf.repository.mock;

import com.whatsyouretf.userservice.domain.etf.dto.EtfSummary;
import com.whatsyouretf.userservice.domain.etf.dto.RiskType;
import com.whatsyouretf.userservice.domain.etf.entity.Etf;
import com.whatsyouretf.userservice.domain.etf.entity.Fundamental;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Component
public class EtfMockRepository {
        private final Map<String, Etf> db = Map.ofEntries(
                Map.entry("069500", Etf.builder().id(1L).stockCode("069500").name("KODEX 200").englishName("KOSPI 200 Index Tracking Fund").strategyType("시장 대표").sector(null).assetManager("삼성").isLeveraged(false).isInverse(false).isHedged(false).expenseRatio(BigDecimal.valueOf(0.15)).nav(BigDecimal.valueOf(35000)).aum(5000000000000L).dividendYield(BigDecimal.valueOf(1.5)).dividendFreq("QUARTERLY").riskType(RiskType.STABLE).listingDate(LocalDate.of(2006, 10, 13)).fundamental(Fundamental.calculateFundamental(22.7, 1.99)).isActive(true).build()),
                Map.entry("102780", Etf.builder().id(2L).stockCode("102780").name("KODEX 삼성그룹").englishName("Samsung Group Index Fund").strategyType("시장 대표").sector(null).assetManager("삼성").isLeveraged(false).isInverse(false).isHedged(false).expenseRatio(BigDecimal.valueOf(0.25)).nav(BigDecimal.valueOf(12000)).aum(800000000000L).dividendYield(BigDecimal.valueOf(2.0)).dividendFreq("QUARTERLY").riskType(RiskType.STABLE).listingDate(LocalDate.of(2012, 5, 15)).fundamental(Fundamental.calculateFundamental(32.36, 1.92)).isActive(true).build()),
                Map.entry("091160", Etf.builder().id(3L).stockCode("091160").name("KODEX 반도체").englishName("Semiconductor Index Fund").strategyType("테마형").sector("반도체").assetManager("삼성").isLeveraged(false).isInverse(false).isHedged(false).expenseRatio(BigDecimal.valueOf(0.45)).nav(BigDecimal.valueOf(42000)).aum(1200000000000L).dividendYield(BigDecimal.valueOf(0.5)).dividendFreq("ANNUAL").riskType(RiskType.MODERATE).listingDate(LocalDate.of(2006, 6, 27)).fundamental(Fundamental.calculateFundamental(37.92, 5.24)).isActive(true).build()),
                Map.entry("133690", Etf.builder().id(4L).stockCode("133690").name("TIGER 미국나스닥100").englishName("US NASDAQ 100 Index Fund").strategyType("해외주식").sector("나스닥").assetManager("미래에셋").isLeveraged(false).isInverse(false).isHedged(false).expenseRatio(BigDecimal.valueOf(0.07)).nav(BigDecimal.valueOf(95000)).aum(3000000000000L).dividendYield(BigDecimal.valueOf(0.3)).dividendFreq("QUARTERLY").riskType(RiskType.MODERATE).listingDate(LocalDate.of(2010, 10, 18)).fundamental(Fundamental.calculateFundamental(35.2, 5.1)).isActive(true).build()),
                Map.entry("360750", Etf.builder().id(5L).stockCode("360750").name("TIGER 미국S&P500").englishName("US S&P 500 Index Fund").strategyType("해외주식").sector("S&P500").assetManager("미래에셋").isLeveraged(false).isInverse(false).isHedged(false).expenseRatio(BigDecimal.valueOf(0.07)).nav(BigDecimal.valueOf(18000)).aum(2500000000000L).dividendYield(BigDecimal.valueOf(1.2)).dividendFreq("QUARTERLY").riskType(RiskType.STABLE).listingDate(LocalDate.of(2020, 8, 7)).fundamental(Fundamental.calculateFundamental(24.8, 4.3)).isActive(true).build()),
                Map.entry("122630", Etf.builder().id(6L).stockCode("122630").name("KODEX 레버리지").englishName("KOSPI 200 Leveraged Fund").strategyType("파생형").sector("레버리지").assetManager("삼성").isLeveraged(true).isInverse(false).isHedged(false).expenseRatio(BigDecimal.valueOf(0.64)).nav(BigDecimal.valueOf(18000)).aum(1800000000000L).dividendYield(BigDecimal.valueOf(0.0)).dividendFreq("NONE").riskType(RiskType.AGGRESSIVE).listingDate(LocalDate.of(2010, 2, 22)).fundamental(Fundamental.calculateFundamental(15.2, 1.1)).isActive(true).build()),
                Map.entry("252670", Etf.builder().id(7L).stockCode("252670").name("KODEX 200선물인버스2X").englishName("KOSPI 200 Inverse 2X Fund").strategyType("파생형").sector("인버스").assetManager("삼성").isLeveraged(false).isInverse(true).isHedged(false).expenseRatio(BigDecimal.valueOf(0.64)).nav(BigDecimal.valueOf(2500)).aum(500000000000L).dividendYield(BigDecimal.valueOf(0.0)).dividendFreq("NONE").riskType(RiskType.AGGRESSIVE).listingDate(LocalDate.of(2016, 9, 22)).fundamental(Fundamental.calculateFundamental(0.0, 0.0)).isActive(true).build()),
                Map.entry("305720", Etf.builder().id(8L).stockCode("305720").name("KODEX 2차전지산업").englishName("Secondary Battery Industry Fund").strategyType("테마형").sector("2차전지").assetManager("삼성").isLeveraged(false).isInverse(false).isHedged(false).expenseRatio(BigDecimal.valueOf(0.45)).nav(BigDecimal.valueOf(22000)).aum(900000000000L).dividendYield(BigDecimal.valueOf(0.2)).dividendFreq("ANNUAL").riskType(RiskType.MODERATE).listingDate(LocalDate.of(2018, 9, 10)).fundamental(Fundamental.calculateFundamental(45.5, 6.2)).isActive(true).build()),
                Map.entry("305540", Etf.builder().id(9L).stockCode("305540").name("TIGER 2차전지테마").englishName("Secondary Battery Theme Fund").strategyType("테마형").sector("2차전지").assetManager("미래에셋").isLeveraged(false).isInverse(false).isHedged(false).expenseRatio(BigDecimal.valueOf(0.50)).nav(BigDecimal.valueOf(18000)).aum(600000000000L).dividendYield(BigDecimal.valueOf(0.1)).dividendFreq("ANNUAL").riskType(RiskType.MODERATE).listingDate(LocalDate.of(2018, 9, 21)).fundamental(Fundamental.calculateFundamental(42.1, 5.8)).isActive(true).build()),
                Map.entry("244580", Etf.builder().id(10L).stockCode("244580").name("KODEX 바이오").englishName("Bio/Healthcare Index Fund").strategyType("테마형").sector("바이오").assetManager("삼성").isLeveraged(false).isInverse(false).isHedged(false).expenseRatio(BigDecimal.valueOf(0.45)).nav(BigDecimal.valueOf(8500)).aum(300000000000L).dividendYield(BigDecimal.valueOf(0.0)).dividendFreq("NONE").riskType(RiskType.AGGRESSIVE).listingDate(LocalDate.of(2016, 6, 30)).fundamental(Fundamental.calculateFundamental(65.2, 4.8)).isActive(true).build())
        );

        public Etf findByTicker(String ticker) {
                return db.get(ticker);
        }

        public Page<EtfSummary> findEtfList(Pageable pageable) {
                List<EtfSummary> pagedEtfs = db.values().stream()
                        .sorted(Comparator.comparing(Etf::getId))
                        .skip(pageable.getOffset())
                        .limit(pageable.getPageSize())
                        .map(etf ->
                                new EtfSummary(etf.getId(), etf.getStockCode(), etf.getName(), Boolean.FALSE, etf.getRiskType().getTypeName()))
                        .toList();

                return new PageImpl<>(pagedEtfs, pageable, db.size());
        }
}
