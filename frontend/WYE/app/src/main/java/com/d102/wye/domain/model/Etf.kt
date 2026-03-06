package com.d102.wye.domain.model

/**
 * 예시 코드
 * domain/model/ 에는 순수 Kotlin 데이터 클래스를 작성한다
 * -> Android import가 없어야 한다
 */
data class Etf(
    val ticker: String,
    val name: String,
    val currentPrice: Long,
    val changeRate: Double,
    val changeAmount: Long,         // 변동금액 (▲400, ▼50)
    val volume: Long,
    val riskLevel: Int,             // 1~5
    val investmentStrategy: String, // 시장대표 / 테마형 / 배당형 / 채권형
    val assetClass: String,         // 주식 / 채권 / 원자재 / 금융
    val theme: String,              // 전자/IT, 바이오/의약 등
    val dividendRate: Double,       // 배당률
    val dividendCycle: String,      // 월/반기/분기/년
    val hasDerivative: Boolean,     // 파생상품 여부
    val per: Double,
    val pbr: Double,
    val roe: Double,
    val expenseRatio: Double,       // 운용보수(수수료)
    val netAsset: Long,             // 순자산액
)
