package com.d102.wye.domain.usecase.simulation

import com.d102.wye.domain.model.BacktestPoint
import com.d102.wye.domain.model.EtfPriceHistory
import com.d102.wye.domain.model.Portfolio
import com.d102.wye.domain.state.InvestmentType
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.roundToLong

class CalculateBacktestUseCase @Inject constructor() {

    data class Result(
        val points: List<BacktestPoint>,
        val estimatedFinalValue: Long,
        val totalReturn: Double,
        val totalInvestment: Long
    )

    operator fun invoke(
        portfolios: List<Portfolio>,
        priceHistories: Map<String, EtfPriceHistory>,
        investmentAmount: Long,
        investmentType: InvestmentType,
        periodMonths: Int
    ): Result {
        val allCommonDates: List<String> = priceHistories.values
            .map { it.content.map { p -> p.date }.toSet() }
            .reduceOrNull { acc, set -> acc.intersect(set) }
            ?.sorted()
            ?: return Result(emptyList(), 0L, 0.0, 0L)

        if (allCommonDates.isEmpty()) return Result(emptyList(), 0L, 0.0, 0L)

        val startDate = LocalDate.now().minusMonths(periodMonths.toLong()).toString()
        val commonDates = allCommonDates.filter { it >= startDate }

        if (commonDates.isEmpty()) return Result(emptyList(), 0L, 0.0, 0L)

        val priceMap: Map<String, Map<String, Double>> =
            priceHistories.mapValues { (_, history) ->
                history.content.associate { it.date to it.stockPrice.toDouble() }
            }

        val result = when (investmentType) {
            InvestmentType.REGULAR_SAVING -> calcInstallment(
                portfolios, commonDates, priceMap, investmentAmount, periodMonths
            )
            InvestmentType.LUMP_SUM -> calcLumpSum(
                portfolios, commonDates, priceMap, investmentAmount
            )
        }

        return result.copy(points = downsample(result.points, 120))
    }

    /**
     * 적립형
     * y축 = 수익률 (%)
     *   매월 평균 단가로 수량 매입
     *   월말 수익률 = (월말 평가금액 - 누적 납입금) / 누적 납입금 × 100
     */
    private fun calcInstallment(
        portfolios: List<Portfolio>,
        dates: List<String>,
        priceMap: Map<String, Map<String, Double>>,
        monthlyAmount: Long,
        periodMonths: Int
    ): Result {
        val accumulatedShares = mutableMapOf<String, Double>()
        portfolios.forEach { accumulatedShares[it.ticker] = 0.0 }

        val datesByMonth: Map<String, List<String>> = dates.groupBy { it.take(7) }
        val points = mutableListOf<BacktestPoint>()
        var monthCount = 0

        datesByMonth.entries.sortedBy { it.key }.forEach { (_, monthDates) ->
            monthCount++

            portfolios.forEach { portfolio ->
                val ticker = portfolio.ticker
                val weight = portfolio.weightPercent / 100.0
                val monthlyInvestPerEtf = monthlyAmount * weight

                val monthlyPrices = monthDates.mapNotNull { priceMap[ticker]?.get(it) }
                if (monthlyPrices.isEmpty()) return@forEach

                val avgPrice = monthlyPrices.average()
                if (avgPrice <= 0) return@forEach

                accumulatedShares[ticker] = (accumulatedShares[ticker] ?: 0.0) + (monthlyInvestPerEtf / avgPrice)
            }

            val lastDate = monthDates.last()
            val monthEndValue = portfolios.sumOf { portfolio ->
                val shares = accumulatedShares[portfolio.ticker] ?: 0.0
                val lastPrice = priceMap[portfolio.ticker]?.get(lastDate) ?: 0.0
                shares * lastPrice
            }

            // y축 = 수익률 (%)
            val cumulativeInvest = monthlyAmount * monthCount
            val returnRate = if (cumulativeInvest > 0)
                (monthEndValue - cumulativeInvest) / cumulativeInvest * 100.0
            else 0.0

            points.add(BacktestPoint(date = lastDate, value = returnRate))
        }

        val totalInvestment = monthlyAmount * periodMonths
        val estimatedFinalValue = points.lastOrNull()?.let {
            // 최종 평가금액 복원 (totalReturn으로 역산)
            (totalInvestment * (1.0 + it.value / 100.0)).roundToLong()
        } ?: 0L
        val totalReturn = points.lastOrNull()?.value ?: 0.0

        return Result(
            points = points,
            estimatedFinalValue = estimatedFinalValue,
            totalReturn = totalReturn,
            totalInvestment = totalInvestment
        )
    }

    /**
     * 거치형
     * y축 = 수익률 (%)
     *   초기 투자 후 일별 수익률 = (일별 평가금액 - 초기 투자금) / 초기 투자금 × 100
     */
    private fun calcLumpSum(
        portfolios: List<Portfolio>,
        dates: List<String>,
        priceMap: Map<String, Map<String, Double>>,
        initialAmount: Long
    ): Result {
        val firstDate = dates.first()
        val shares = mutableMapOf<String, Double>()

        portfolios.forEach { portfolio ->
            val ticker = portfolio.ticker
            val weight = portfolio.weightPercent / 100.0
            val investPerEtf = initialAmount * weight
            val firstPrice = priceMap[ticker]?.get(firstDate) ?: return@forEach
            if (firstPrice > 0) shares[ticker] = investPerEtf / firstPrice
        }

        val points = dates.map { date ->
            val portfolioValue = portfolios.sumOf { portfolio ->
                val s = shares[portfolio.ticker] ?: 0.0
                val price = priceMap[portfolio.ticker]?.get(date) ?: 0.0
                s * price
            }
            // y축 = 수익률 (%)
            val returnRate = if (initialAmount > 0)
                (portfolioValue - initialAmount) / initialAmount * 100.0
            else 0.0
            BacktestPoint(date = date, value = returnRate)
        }

        val finalReturn = points.lastOrNull()?.value ?: 0.0
        val estimatedFinalValue = (initialAmount * (1.0 + finalReturn / 100.0)).roundToLong()

        return Result(
            points = points,
            estimatedFinalValue = estimatedFinalValue,
            totalReturn = finalReturn,
            totalInvestment = initialAmount
        )
    }
}

private fun downsample(points: List<BacktestPoint>, maxCount: Int): List<BacktestPoint> {
    if (points.size <= maxCount) return points
    val step = points.size.toFloat() / maxCount
    return (0 until maxCount).map { i ->
        points[(i * step).toInt().coerceAtMost(points.size - 1)]
    } + listOf(points.last())
}