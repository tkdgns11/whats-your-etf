package com.whatsyouretf.userservice.domain.portfolio.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 포트폴리오 ETF 추가 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioEtfAddRequest {

    @NotNull(message = "ETF ID는 필수입니다.")
    @Positive(message = "ETF ID는 양수여야 합니다.")
    private Long etfId;

    @NotNull(message = "비중은 필수입니다.")
    @DecimalMin(value = "0.001", message = "최소 비중은 0.001%입니다.")
    @DecimalMax(value = "100", message = "최대 비중은 100%입니다.")
    private BigDecimal weightPct;
}
