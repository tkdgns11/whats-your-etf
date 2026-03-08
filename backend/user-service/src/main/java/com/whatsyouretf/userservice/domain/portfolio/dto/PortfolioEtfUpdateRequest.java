package com.whatsyouretf.userservice.domain.portfolio.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 포트폴리오 ETF 비중 수정 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioEtfUpdateRequest {

    @NotEmpty(message = "최소 1개의 ETF가 필요합니다.")
    @Valid
    private List<EtfWeight> etfs;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EtfWeight {
        @NotNull(message = "ETF ID는 필수입니다.")
        @Positive(message = "ETF ID는 양수여야 합니다.")
        private Long etfId;

        @NotNull(message = "비중은 필수입니다.")
        @DecimalMin(value = "0.001", message = "최소 비중은 0.001%입니다.")
        @DecimalMax(value = "100", message = "최대 비중은 100%입니다.")
        private BigDecimal weightPct;
    }
}
