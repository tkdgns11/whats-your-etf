package com.whatsyouretf.userservice.domain.portfolio.entity;

import com.whatsyouretf.userservice.domain.common.entity.Category;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 꾸러미 (시스템 제공 포트폴리오) 엔티티
 * <p>
 * 시스템에서 제공하는 예시 포트폴리오 정보를 저장합니다.
 */
@Entity
@Table(name = "preset_portfolios")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PresetPortfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 꾸러미 이름 */
    @Column(nullable = false, length = 100)
    private String name;

    /** 짧은 설명 (카드 표시용) */
    @Column(name = "short_description", length = 200)
    private String shortDescription;

    /** 상세 설명 */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** 카테고리 (FK -> category, PORTFOLIO_DIVIDEND 등) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_code")
    private Category category;

    /** 노출 순서 */
    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    /** 활성 여부 */
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    /** 꾸러미 ETF 구성 */
    @OneToMany(mappedBy = "presetPortfolio", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PresetPortfolioEtf> presetPortfolioEtfs = new ArrayList<>();
}
