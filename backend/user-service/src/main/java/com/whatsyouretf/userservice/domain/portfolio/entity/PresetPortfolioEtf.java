package com.whatsyouretf.userservice.domain.portfolio.entity;

import com.whatsyouretf.userservice.domain.etf.entity.Etf;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 꾸러미 ETF 구성 엔티티
 * <p>
 * 꾸러미(시스템 제공 포트폴리오)의 ETF 구성 정보를 저장합니다.
 * 비중은 사용자가 결정합니다.
 */
@Entity
@Table(name = "preset_portfolio_etfs", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"preset_portfolio_id", "etf_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PresetPortfolioEtf {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 꾸러미 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preset_portfolio_id", nullable = false)
    private PresetPortfolio presetPortfolio;

    /** ETF */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etf_id", nullable = false)
    private Etf etf;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
