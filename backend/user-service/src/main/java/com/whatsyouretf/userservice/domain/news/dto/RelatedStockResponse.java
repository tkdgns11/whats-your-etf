package com.whatsyouretf.userservice.domain.news.dto;

import com.whatsyouretf.userservice.domain.company.entity.CompanyInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 관련 종목 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RelatedStockResponse {

    /** 회사 ID */
    private Long companyId;

    /** 회사명 */
    private String companyName;

    /** 산업 그룹 */
    private String industryGroup;

    /** ETF 내 비중 (ETF 뉴스 조회 시) */
    private BigDecimal weightPct;

    /**
     * CompanyInfo -> DTO 변환
     */
    public static RelatedStockResponse from(CompanyInfo company) {
        return RelatedStockResponse.builder()
                .companyId(company.getId())
                .companyName(company.getStockName())
                .industryGroup(company.getIndustryGroup())
                .build();
    }

    /**
     * CompanyInfo + 비중 -> DTO 변환
     */
    public static RelatedStockResponse from(CompanyInfo company, BigDecimal weightPct) {
        return RelatedStockResponse.builder()
                .companyId(company.getId())
                .companyName(company.getStockName())
                .industryGroup(company.getIndustryGroup())
                .weightPct(weightPct)
                .build();
    }
}
