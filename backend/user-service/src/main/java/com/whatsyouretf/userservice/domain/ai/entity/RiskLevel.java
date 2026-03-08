package com.whatsyouretf.userservice.domain.ai.entity;

/**
 * 포트폴리오 리스크 레벨 Enum
 */
public enum RiskLevel {
    /** 낮음 - 변동성 낮음, 안정적 배당/채권형 중심 */
    LOW,
    /** 중간 - 적절한 분산, 성장주와 안전자산 혼합 */
    MEDIUM,
    /** 높음 - 테마형/레버리지 ETF 과다, 섹터 집중 */
    HIGH
}
