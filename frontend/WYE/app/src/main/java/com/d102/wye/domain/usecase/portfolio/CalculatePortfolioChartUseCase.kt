package com.d102.wye.domain.usecase.portfolio

import com.d102.wye.domain.model.BacktestPoint
import com.d102.wye.domain.model.EtfPriceHistory
import com.d102.wye.domain.model.PortfolioCount
import javax.inject.Inject
import kotlin.math.roundToLong

/**
 * 포트폴리오 상세 화면용 차트 계산
 *
 * counts(수량) × stockPrice(주가) 로 일별 평가금액 계산
 *
 * 두 구간:
 * - 최근 성과: createdAt ~ 오늘
 * - 과거 1년: createdAt-1년 ~ createdAt
 */
class CalculatePortfolioChartUseCase @Inject constructor() {

    data class Result(
        val recentPoints: List<BacktestPoint>,   // createdAt ~ 오늘
        val pastPoints: List<BacktestPoint>,     // createdAt-1년 ~ createdAt
        val recentReturn: Double,                // 최근 수익률 (%)
        val pastReturn: Double,                  // 과거 1년 수익률 (%)
        val estimatedFinalValue: Long            // 현재 평가금액
    )

    operator fun invoke(
        counts: List<PortfolioCount>,
        priceHistories: Map<String, EtfPriceHistory>,
        createdAt: String   // "2026-03-15"
    ): Result {
        // 전체 날짜 교집합 (오름차순)
        val allDates: List<String> = priceHistories.values
            .map { it.content.map { p -> p.date }.toSet() }
            .reduceOrNull { acc, set -> acc.intersect(set) }
            ?.sorted()
            ?: return Result(emptyList(), emptyList(), 0.0, 0.0, 0L)

        // 날짜 → 주가 맵
        val priceMap: Map<String, Map<String, Double>> =
            priceHistories.mapValues { (_, history) ->
                history.content.associate { it.date to it.stockPrice.toDouble() }
            }

        // 평가금액 계산 함수
        fun portfolioValue(date: String): Double =
            counts.sumOf { count ->
                val price = priceMap[count.ticker]?.get(date) ?: 0.0
                count.counts * price
            }

        // 최근 구간: createdAt ~ 오늘
        val recentDates = allDates.filter { it >= createdAt }
        val recentPoints = recentDates.map { date ->
            BacktestPoint(date = date, value = portfolioValue(date))
        }

        // 과거 구간: createdAt-1년 ~ createdAt
        val pastStartDate = createdAt.toLocalDateMinusOneYear()
        val pastDates = allDates.filter { it in pastStartDate..createdAt }
        val pastPoints = pastDates.map { date ->
            BacktestPoint(date = date, value = portfolioValue(date))
        }

        // 수익률 계산
        val recentReturn = calcReturn(recentPoints)
        val pastReturn = calcReturn(pastPoints)
        val estimatedFinalValue = recentPoints.lastOrNull()?.value?.roundToLong() ?: 0L

        return Result(
            recentPoints = recentPoints,
            pastPoints = pastPoints,
            recentReturn = recentReturn,
            pastReturn = pastReturn,
            estimatedFinalValue = estimatedFinalValue
        )
    }

    private fun calcReturn(points: List<BacktestPoint>): Double {
        val first = points.firstOrNull()?.value ?: return 0.0
        val last = points.lastOrNull()?.value ?: return 0.0
        if (first <= 0) return 0.0
        return ((last - first) / first) * 100.0
    }
}

/** "2026-03-15" → 1년 전 "2025-03-15" */
private fun String.toLocalDateMinusOneYear(): String {
    val parts = split("-")
    if (parts.size != 3) return this
    val year = parts[0].toIntOrNull() ?: return this
    return "${year - 1}-${parts[1]}-${parts[2]}"
}