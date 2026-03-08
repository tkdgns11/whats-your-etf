package com.whatsyouretf.userservice.domain.etf.entity;

import com.whatsyouretf.userservice.domain.ai.entity.AiPrompt;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ETF 섹터 분포 엔티티
 * <p>
 * ETF 구성종목의 산업별 집계 정보를 저장합니다.
 */
@Entity
@Table(name = "etf_sector_cluster", indexes = {
        @Index(name = "idx_sector_cluster_etf", columnList = "etf_id"),
        @Index(name = "idx_sector_cluster_date", columnList = "etf_id, base_date DESC")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class EtfSectorCluster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ETF */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etf_id", nullable = false)
    private Etf etf;

    /** 클러스터 유형 (GROUP_CODE / INDUSTRY / SUB_SECTOR) */
    @Column(name = "cluster_type", nullable = false, length = 20)
    private String clusterType;

    /** KSIC 산업코드 */
    @Column(name = "industry_code", length = 10)
    private String industryCode;

    /** 산업명 */
    @Column(name = "industry_name", length = 100)
    private String industryName;

    /** 그룹코드 */
    @Column(name = "group_code", length = 20)
    private String groupCode;

    /** 그룹명 */
    @Column(name = "group_name", length = 50)
    private String groupName;

    /** 세부 섹터명 (테마 ETF용) */
    @Column(name = "sub_sector", length = 100)
    private String subSector;

    /** 비중 (%) */
    @Column(name = "weight_pct", nullable = false, precision = 6, scale = 3)
    private BigDecimal weightPct;

    /** 해당 섹터 종목 수 */
    @Column(name = "stock_count")
    private Integer stockCount;

    /** 버블 X 좌표 (UMAP) */
    @Column(name = "pos_x", precision = 10, scale = 6)
    private BigDecimal posX;

    /** 버블 Y 좌표 (UMAP) */
    @Column(name = "pos_y", precision = 10, scale = 6)
    private BigDecimal posY;

    /** 버블 반지름 */
    @Column(precision = 10, scale = 6)
    private BigDecimal radius;

    /** ETF 중심까지 거리 */
    @Column(name = "distance_to_center", precision = 10, scale = 6)
    private BigDecimal distanceToCenter;

    /** 섹터 영향력 AI 분석 결과 */
    @Column(name = "ai_analysis", columnDefinition = "TEXT")
    private String aiAnalysis;

    /** 사용된 프롬프트 (FK) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prompt_id")
    private AiPrompt prompt;

    /** 기준일 */
    @Column(name = "base_date", nullable = false)
    private LocalDate baseDate;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
