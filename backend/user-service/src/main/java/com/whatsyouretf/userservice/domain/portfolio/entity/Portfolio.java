package com.whatsyouretf.userservice.domain.portfolio.entity;

import com.whatsyouretf.userservice.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 포트폴리오 엔티티
 * <p>
 * 사용자의 포트폴리오 정보를 저장합니다.
 */
@Entity
@Table(name = "portfolio", indexes = {
        @Index(name = "idx_portfolio_user", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 사용자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 포트폴리오 이름 */
    @Column(nullable = false, length = 100)
    private String name;

    /** 포트폴리오 설명 */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** 투자 금액 */
    @Column(name = "invest_amount", precision = 15, scale = 2)
    private BigDecimal investAmount;

    /** 저장 시점 ETF 구성 + 비중 (TEXT) */
    @Column(name = "snapshot_etfs", columnDefinition = "TEXT")
    private String snapshotEtfs;

    /** 저장 시점 시뮬 지표 (TEXT) */
    @Column(name = "snapshot_metrics", columnDefinition = "TEXT")
    private String snapshotMetrics;

    /** 알림 허용 여부 */
    @Column(name = "is_alert_enabled")
    @Builder.Default
    private Boolean isAlertEnabled = false;

    /** 현재 수익률 */
    @Column(name = "current_return", precision = 8, scale = 4)
    private BigDecimal currentReturn;

    /** 전일 종가 (포트폴리오 평가액) */
    @Column(name = "prev_close_value", precision = 15, scale = 2)
    private BigDecimal prevCloseValue;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    /** 포트폴리오 ETF 구성 */
    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PortfolioEtf> portfolioEtfs = new ArrayList<>();

}
