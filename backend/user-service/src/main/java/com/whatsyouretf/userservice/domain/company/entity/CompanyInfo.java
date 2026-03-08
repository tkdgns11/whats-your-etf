package com.whatsyouretf.userservice.domain.company.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 상장 회사 정보 엔티티
 * <p>
 * 국내 상장 회사 정보를 저장합니다.
 * 주식 정보(ticker, close 등)는 Stock 엔티티에 저장됩니다.
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

    /** 산업분류 코드 (industry_classification FK, 소분류) */
    @Column(name = "industry_code", length = 10)
    private String industryCode;

    /** 회사명 */
    @Column(name = "company_name", nullable = false, length = 100)
    private String companyName;

    /** 투자테마 그룹 (IT_SEMI, BIO 등) */
    @Column(name = "industry_group", length = 50)
    private String industryGroup;

    /** 대표자명 */
    @Column(name = "ceo_name", length = 100)
    private String ceoName;

    /** 홈페이지 URL */
    @Column(length = 200)
    private String homepage;

    /** 지역 */
    @Column(length = 50)
    private String region;

    /** 회사 설명/사업 내용 */
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
