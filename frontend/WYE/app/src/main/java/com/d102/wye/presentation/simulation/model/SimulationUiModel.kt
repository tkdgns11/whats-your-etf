package com.d102.wye.presentation.simulation.model

import com.d102.wye.domain.model.BacktestPoint
import com.d102.wye.domain.model.SimulationResult
import com.d102.wye.domain.state.InvestmentType

/**
 * 시뮬레이션 화면 전용 UI 모델
 */
data class SimulationUiModel(
    // 요약 카드
    val estimatedFinalAsset: String,  // "1억 2,345만원"
    val yieldRate: String,            // "+12.34%" or "-5.67%"
    val totalInvestment: String,      // "3,600만원"

    // 펀더멘털
    val per: String,   // "12.5배"
    val pbr: String,   // "1.2배"
    val roe: String,   // "9.8%"

    // 배당금
    val expectedAnnualDividend: String,   // "24만원"
    val expectedMonthlyDividend: String,  // "2만원"

    // 차트 데이터 (raw 값 그대로 — Canvas가 직접 계산에 사용)
    val backtestPoints: List<BacktestPoint>,
    val investmentType: InvestmentType,
    val isPositiveReturn: Boolean
)

// ─────────────────────────────────────────────────────────────────────────────
// 변환 확장함수
// ─────────────────────────────────────────────────────────────────────────────

fun SimulationResult.toUiModel(investmentType: InvestmentType): SimulationUiModel {
    val isPositive = totalReturn >= 0.0
    val returnSign = if (isPositive) "+" else ""

    return SimulationUiModel(
        estimatedFinalAsset = estimatedFinalValue.formatAmount(),
        yieldRate = "$returnSign${String.format("%.2f", totalReturn)}%",
        totalInvestment = totalInvestment.formatAmount(),
        per = "${String.format("%.1f", fundamentals.per)}배",
        pbr = "${String.format("%.1f", fundamentals.pbr)}배",
        roe = "${String.format("%.1f", fundamentals.roe)}%",
        expectedAnnualDividend = expectedAnnualDividend.formatAmount(),
        expectedMonthlyDividend = expectedMonthlyDividend.formatAmount(),
        backtestPoints = backtestPoints,
        investmentType = investmentType,
        isPositiveReturn = isPositive
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// 포맷팅 유틸 (simulation feature 내부 전용)
// ─────────────────────────────────────────────────────────────────────────────

private fun Long.formatAmount(): String {
    val eok = this / 100_000_000L
    val man = (this % 100_000_000L) / 10_000L
    val won = this % 10_000L

    return when {
        eok > 0 && man > 0 -> "${eok}억 ${"%,d".format(man)}만원"
        eok > 0             -> "${eok}억원"
        man > 0             -> "${"%,d".format(man)}만원"
        else                -> "${"%,d".format(won)}원"
    }
}