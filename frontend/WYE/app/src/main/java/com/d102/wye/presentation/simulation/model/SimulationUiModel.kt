package com.d102.wye.presentation.simulation.model

import com.d102.wye.domain.model.BacktestPoint
import com.d102.wye.domain.model.SimulationResult
import com.d102.wye.domain.state.InvestmentType
import com.d102.wye.presentation.simulation.progress.SectorWeightUiModel

/**
 * 시뮬레이션 화면 전용 UI 모델
 */
data class SimulationUiModel(
    // 요약 카드
    val estimatedFinalAsset: String,  // "1억 2,345만원"
    val netProfit: String,            // "+2,345만원" or "-500만원"
    val yieldRate: String,            // "+12.34%" or "-5.67%"
    val totalInvestment: String,      // "3,600만원"

    // 펀더멘털
    val per: String,
    val pbr: String,
    val roe: String,

    // 배당금
    val expectedAnnualDividend: String,
    val expectedMonthlyDividend: String,

    // 차트 데이터
    val backtestPoints: List<BacktestPoint>,
    val investmentType: InvestmentType,
    val isPositiveReturn: Boolean,
    val sectorWeights: List<SectorWeightUiModel> = emptyList()
)

fun SimulationResult.toUiModel(
    investmentType: InvestmentType,
    sectorWeights: List<SectorWeightUiModel> = emptyList()
): SimulationUiModel {
    val isPositive = totalReturn >= 0.0
    val netProfitValue = estimatedFinalValue - totalInvestment

    return SimulationUiModel(
        estimatedFinalAsset = estimatedFinalValue.formatAmount(),
        netProfit = if (netProfitValue >= 0) "+${netProfitValue.formatAmount()}"
        else "-${Math.abs(netProfitValue).formatAmount()}",
        yieldRate = "${if (isPositive) "+" else ""}${String.format("%.2f", totalReturn)}%",
        totalInvestment = totalInvestment.formatAmount(),
        per = "${String.format("%.1f", fundamentals.per)}배",
        pbr = "${String.format("%.1f", fundamentals.pbr)}배",
        roe = "${String.format("%.1f", fundamentals.roe)}%",
        expectedAnnualDividend = expectedAnnualDividend.formatAmount(),
        expectedMonthlyDividend = expectedMonthlyDividend.formatAmount(),
        backtestPoints = backtestPoints,
        investmentType = investmentType,
        isPositiveReturn = isPositive,
        sectorWeights = sectorWeights
    )
}

private fun Long.formatAmount(): String {
    val absValue = Math.abs(this)
    val eok = absValue / 100_000_000L
    val remainder = absValue % 100_000_000L

    return when {
        eok > 0 -> {
            // 억 단위가 있을 경우: "1억 2,345,678원"
            "${eok}억 ${"%,d".format(remainder)}원"
        }

        else -> {
            // 억 단위가 없을 경우: "1,234,567원"
            "%,d원".format(remainder)
        }
    }
}