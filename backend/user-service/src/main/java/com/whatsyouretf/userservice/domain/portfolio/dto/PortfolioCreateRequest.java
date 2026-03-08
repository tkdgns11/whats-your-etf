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
 * 포트폴리오 생성 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioCreateRequest {

    /** 포트폴리오 이름 */
    @NotBlank(message = "포트폴리오 이름은 필수입니다.")
    @Size(min = 1, max = 100, message = "포트폴리오 이름은 1~100자여야 합니다.")
    @Pattern(regexp = "^[가-힣a-zA-Z0-9\\s\\-_]{1,100}$", message = "포트폴리오 이름에 허용되지 않는 문자가 포함되어 있습니다.")
    private String name;

    /** 포트폴리오 설명 */
    @Size(max = 1000, message = "설명은 최대 1000자입니다.")
    private String description;

    /** 총 투자금액 */
    @NotNull(message = "총 투자금액은 필수입니다.")
    @DecimalMin(value = "10000", message = "최소 투자금액은 10,000원입니다.")
    @DecimalMax(value = "10000000000", message = "최대 투자금액은 100억원입니다.")
    private BigDecimal totalInvestment;

    /** ETF 구성 */
    @NotEmpty(message = "최소 1개의 ETF가 필요합니다.")
    @Size(max = 20, message = "최대 20개의 ETF만 포함할 수 있습니다.")
    @Valid
    private List<EtfItem> etfs;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EtfItem {
        @NotNull(message = "ETF ID는 필수입니다.")
        @Positive(message = "ETF ID는 양수여야 합니다.")
        private Long etfId;

        @NotNull(message = "비중은 필수입니다.")
        @DecimalMin(value = "0.001", message = "최소 비중은 0.001%입니다.")
        @DecimalMax(value = "100", message = "최대 비중은 100%입니다.")
        private BigDecimal weightPct;
    }
}
