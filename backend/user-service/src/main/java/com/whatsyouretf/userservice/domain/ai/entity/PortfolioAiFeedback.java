package com.whatsyouretf.userservice.domain.ai.entity;

import com.whatsyouretf.userservice.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 포트폴리오 AI 피드백 엔티티
 * <p>
 * 사용자 포트폴리오에 대한 AI 분석 결과를 저장합니다.
 */
@Entity
@Table(name = "portfolio_ai_feedback", indexes = {
        @Index(name = "idx_ai_feedback_user", columnList = "user_id"),
        @Index(name = "idx_ai_feedback_created", columnList = "created_at DESC")
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PortfolioAiFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 사용자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 사용된 프롬프트 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prompt_id")
    private AiPrompt prompt;

    /** 포트폴리오 스냅샷 ID (저장된 포트폴리오 참조, nullable) */
    @Column(name = "portfolio_snapshot_id")
    private Long portfolioSnapshotId;

    /** 요청 시 포트폴리오 정보 (JSON) */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "portfolio_data", columnDefinition = "jsonb")
    private String portfolioData;

    /** 처리 상태 */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private ReviewStatus status = ReviewStatus.PROCESSING;

    /** Bull 리뷰 (JSON) */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "bull_review", columnDefinition = "jsonb")
    private String bullReview;

    /** Bear 리뷰 (JSON) */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "bear_review", columnDefinition = "jsonb")
    private String bearReview;

    /** 종합 점수 (0.0 ~ 10.0) */
    @Column(name = "overall_score", precision = 4, scale = 2)
    private BigDecimal overallScore;

    /** 리스크 레벨 */
    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", length = 20)
    private RiskLevel riskLevel;

    /** 추천 코멘트 */
    @Column(columnDefinition = "TEXT")
    private String recommendation;

    /** 관련 뉴스 (JSON 배열) */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "related_news", columnDefinition = "jsonb")
    private String relatedNews;

    /** 진단 결과 헤드라인 */
    @Column(length = 100)
    private String headline;

    /** 서브 헤드라인 */
    @Column(name = "sub_headline", length = 200)
    private String subHeadline;

    /** 분석 키워드 (JSON 배열) */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String keywords;

    /** 종합 분석 결과 */
    @Column(columnDefinition = "TEXT")
    private String analysis;

    /** 사용된 LLM 모델 */
    @Column(name = "llm_model", length = 50)
    private String llmModel;

    /** 사용자 평가 */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private RatingType rating;

    /** 사용자 코멘트 */
    @Column(name = "rating_comment", length = 500)
    private String ratingComment;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * 리뷰 완료 처리
     */
    public void complete(String bullReview, String bearReview, BigDecimal overallScore,
                         RiskLevel riskLevel, String recommendation, String llmModel) {
        this.status = ReviewStatus.COMPLETED;
        this.bullReview = bullReview;
        this.bearReview = bearReview;
        this.overallScore = overallScore;
        this.riskLevel = riskLevel;
        this.recommendation = recommendation;
        this.llmModel = llmModel;
    }

    /**
     * 리뷰 실패 처리
     */
    public void fail() {
        this.status = ReviewStatus.FAILED;
    }

    /**
     * 리뷰 평가 등록
     */
    public void rate(RatingType rating, String comment) {
        this.rating = rating;
        this.ratingComment = comment;
    }

    /**
     * 이미 평가되었는지 확인
     */
    public boolean isRated() {
        return this.rating != null;
    }
}
