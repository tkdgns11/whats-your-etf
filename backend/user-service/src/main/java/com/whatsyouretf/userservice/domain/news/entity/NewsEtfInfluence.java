package com.whatsyouretf.userservice.domain.news.entity;

import com.whatsyouretf.userservice.domain.etf.entity.Etf;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 뉴스-ETF 영향도 분석 엔티티
 * <p>
 * AI가 분석한 뉴스의 ETF 영향도 정보를 저장합니다.
 */
@Entity
@Table(name = "news_etf_influence", indexes = {
        @Index(name = "idx_news_etf_influence_news", columnList = "news_id"),
        @Index(name = "idx_news_etf_influence_etf", columnList = "etf_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class NewsEtfInfluence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 뉴스 기사 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "news_id", nullable = false)
    private NewsArticle newsArticle;

    /** ETF */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etf_id", nullable = false)
    private Etf etf;

    /** 영향도 점수 (0.0000 ~ 1.0000) */
    @Column(name = "influence_score", precision = 5, scale = 4)
    private BigDecimal influenceScore;

    /** 영향 유형 (POSITIVE, NEGATIVE, NEUTRAL) */
    @Column(name = "influence_type", length = 20)
    private String influenceType;

    /** 타임라인 제목 */
    @Column(name = "timeline_title", length = 100)
    private String timelineTitle;

    /** 타임라인 요약 */
    @Column(name = "timeline_summary", length = 200)
    private String timelineSummary;

    /** 분석 이유 */
    @Column(name = "analysis_reason", columnDefinition = "TEXT")
    private String analysisReason;

    /** 실제 변동률 (검증용) */
    @Column(name = "actual_change_rate", precision = 8, scale = 4)
    private BigDecimal actualChangeRate;

    /** 검증 일시 */
    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    /** 검증 여부 */
    @Column(name = "is_verified")
    @Builder.Default
    private Boolean isVerified = false;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
