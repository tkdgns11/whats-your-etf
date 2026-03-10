package com.whatsyouretf.userservice.domain.company.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 상장 회사 정보 엔티티
 * <p>
 * 국내 상장 회사 정보를 저장합니다.
 * 이 테이블의 데이터는 팀원이 담당하며, user-service에서는 조회만 합니다.
 */
@Entity
@Table(name = "company_info")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CompanyInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 회사명 */
    @Column(name = "company_name", length = 100)
    private String companyName;

    /** 시장 구분 (KOSPI, KOSDAQ) */
    @Column(name = "market_type", length = 20)
    private String marketType;

    /** 산업분류 코드 (세분류: SEMI_HBM 등) */
    @Column(name = "industry_code", length = 20)
    private String industryCode;

    /** 산업분류명 (WICS 소분류: 반도체와반도체장비 등) */
    @Column(name = "industry_name", length = 100)
    private String industryName;

    /** 투자테마 그룹 (대분류: IT_SEMI, BIO 등) */
    @Column(name = "industry_group", length = 50)
    private String industryGroup;

    /** 회사 설명/사업 내용 */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** 상장일 */
    @Column(name = "listing_date")
    private LocalDate listingDate;

    /** 결산월 */
    @Column(name = "fiscal_month")
    private Integer fiscalMonth;

    /** 대표자명 */
    @Column(name = "ceo_name", length = 100)
    private String ceoName;

    /** 홈페이지 URL */
    @Column(length = 200)
    private String homepage;

    /** 지역 */
    @Column(length = 50)
    private String region;

    /** 액면가 */
    @Column(name = "face_value")
    private Integer faceValue;

    /** 상장주식수 */
    @Column(name = "listed_shares")
    private Long listedShares;

    /** 활성 여부 */
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    /** 데이터 출처 */
    @Column(name = "data_source", length = 50)
    private String dataSource;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
