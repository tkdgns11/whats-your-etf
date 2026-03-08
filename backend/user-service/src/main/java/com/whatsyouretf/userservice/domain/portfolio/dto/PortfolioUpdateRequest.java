package com.whatsyouretf.userservice.domain.portfolio.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 포트폴리오 수정 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioUpdateRequest {

    /** 포트폴리오 이름 */
    @Size(min = 1, max = 100, message = "포트폴리오 이름은 1~100자여야 합니다.")
    @Pattern(regexp = "^[가-힣a-zA-Z0-9\\s\\-_]{1,100}$", message = "포트폴리오 이름에 허용되지 않는 문자가 포함되어 있습니다.")
    private String name;

    /** 포트폴리오 설명 */
    @Size(max = 1000, message = "설명은 최대 1000자입니다.")
    private String description;

    /** 총 투자금액 */
    @DecimalMin(value = "10000", message = "최소 투자금액은 10,000원입니다.")
    @DecimalMax(value = "10000000000", message = "최대 투자금액은 100억원입니다.")
    private BigDecimal totalInvestment;
}
