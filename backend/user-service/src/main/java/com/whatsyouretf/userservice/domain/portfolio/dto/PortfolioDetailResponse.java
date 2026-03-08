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
 * 포트폴리오 상세 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioDetailResponse {

    private Long id;
    private String name;
    private String description;
    private BigDecimal totalInvestment;
    private BigDecimal currentValue;
    private BigDecimal returnRate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<PortfolioEtfItem> etfs;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PortfolioEtfItem {
        private Long etfId;
        private String ticker;
        private String name;
        private BigDecimal weightPct;
        private BigDecimal investedAmount;
        private BigDecimal currentAmount;
        private BigDecimal returnRate;
    }

    public static PortfolioDetailResponse from(Portfolio portfolio,
                                                BigDecimal currentValue,
                                                List<PortfolioEtfItem> etfs) {
        BigDecimal returnRate = BigDecimal.ZERO;
        if (portfolio.getInvestAmount() != null && portfolio.getInvestAmount().compareTo(BigDecimal.ZERO) > 0
                && currentValue != null) {
            returnRate = currentValue.subtract(portfolio.getInvestAmount())
                    .divide(portfolio.getInvestAmount(), 4, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        return PortfolioDetailResponse.builder()
                .id(portfolio.getId())
                .name(portfolio.getName())
                .description(portfolio.getDescription())
                .totalInvestment(portfolio.getInvestAmount())
                .currentValue(currentValue)
                .returnRate(returnRate)
                .createdAt(portfolio.getCreatedAt())
                .updatedAt(portfolio.getUpdatedAt())
                .etfs(etfs)
                .build();
    }
}
