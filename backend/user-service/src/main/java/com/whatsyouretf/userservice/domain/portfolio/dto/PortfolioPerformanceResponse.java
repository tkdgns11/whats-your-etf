package com.whatsyouretf.userservice.domain.portfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 포트폴리오 수익률 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioPerformanceResponse {

    private Long portfolioId;
    private String name;
    private BigDecimal totalInvestment;
    private BigDecimal currentValue;
    private BigDecimal totalReturn;
    private BigDecimal totalReturnRate;
    private List<DailyReturn> dailyReturns;
    private BenchmarkComparison benchmarkComparison;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DailyReturn {
        private LocalDate date;
        private BigDecimal value;
        private BigDecimal returnRate;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BenchmarkComparison {
        private String benchmark;
        private BigDecimal benchmarkReturn;
        private BigDecimal alpha;
    }
}
