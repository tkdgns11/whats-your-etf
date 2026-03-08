package com.whatsyouretf.userservice.domain.portfolio.dto;

import com.whatsyouretf.userservice.domain.portfolio.entity.Portfolio;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 포트폴리오 목록 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioListResponse {

    private List<PortfolioSummary> portfolios;
    private int page;
    private int totalPages;
    private long totalElements;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PortfolioSummary {
        private Long id;
        private String name;
        private BigDecimal totalInvestment;
        private BigDecimal currentValue;
        private BigDecimal returnRate;
        private int etfCount;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static PortfolioSummary from(Portfolio portfolio, BigDecimal currentValue) {
            BigDecimal returnRate = BigDecimal.ZERO;
            if (portfolio.getInvestAmount() != null && portfolio.getInvestAmount().compareTo(BigDecimal.ZERO) > 0
                    && currentValue != null) {
                returnRate = currentValue.subtract(portfolio.getInvestAmount())
                        .divide(portfolio.getInvestAmount(), 4, java.math.RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
            }

            return PortfolioSummary.builder()
                    .id(portfolio.getId())
                    .name(portfolio.getName())
                    .totalInvestment(portfolio.getInvestAmount())
                    .currentValue(currentValue)
                    .returnRate(returnRate)
                    .etfCount(portfolio.getPortfolioEtfs().size())
                    .createdAt(portfolio.getCreatedAt())
                    .updatedAt(portfolio.getUpdatedAt())
                    .build();
        }
    }
}
