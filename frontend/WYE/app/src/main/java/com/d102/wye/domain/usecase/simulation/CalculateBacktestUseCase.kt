package com.d102.wye.domain.usecase.simulation

import com.d102.wye.domain.model.BacktestPoint
import com.d102.wye.domain.model.EtfPriceHistory
import com.d102.wye.domain.model.Portfolio
import com.d102.wye.domain.state.InvestmentType
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
        investmentType: InvestmentType
    ): Result {
        val commonDates: List<String> = priceHistories.values
            .map { it.content.map { p -> p.date }.toSet() }
            .reduceOrNull { acc, set -> acc.intersect(set) }
            ?.sorted()
            ?: return Result(emptyList(), 0L, 0.0, 0L)

        if (commonDates.isEmpty()) return Result(emptyList(), 0L, 0.0, 0L)

        val dailyReturnMap: Map<String, Map<String, Double>> =
            priceHistories.mapValues { (_, history) ->
                history.content.associate { it.date to it.dailyReturn }
            }

        return when (investmentType) {
            InvestmentType.INSTALLMENT -> calcInstallment(
                portfolios, commonDates, dailyReturnMap, investmentAmount
            )
            InvestmentType.LUMP_SUM -> calcLumpSum(
                portfolios, commonDates, dailyReturnMap, investmentAmount
            )
        }
    }

    private fun calcInstallment(
        portfolios: List<Portfolio>,
        dates: List<String>,
        dailyReturnMap: Map<String, Map<String, Double>>,
        monthlyAmount: Long
    ): Result {
        var portfolioValue = 0.0
        var totalInvested = 0.0
        var lastMonth = ""
        val points = mutableListOf<BacktestPoint>()

        dates.forEach { date ->
            val month = date.take(7)
            if (month != lastMonth) {
                portfolioValue += monthlyAmount
                totalInvested += monthlyAmount
                lastMonth = month
            }

            val dayReturnPct = portfolios.sumOf { portfolio ->
                val dailyReturn = dailyReturnMap[portfolio.ticker]?.get(date) ?: 0.0
                (portfolio.weightPercent / 100.0) * dailyReturn
            }
            portfolioValue *= (1.0 + dayReturnPct / 100.0)

            val cumulativeReturn = if (totalInvested > 0)
                (portfolioValue - totalInvested) / totalInvested * 100.0
            else 0.0

            points.add(BacktestPoint(date = date, value = cumulativeReturn))
        }

        val finalReturn = points.lastOrNull()?.value ?: 0.0
        val totalInvestment = totalInvested.roundToLong()
        val estimatedFinal = (totalInvested * (1.0 + finalReturn / 100.0)).roundToLong()

        return Result(
            points = points,
            estimatedFinalValue = estimatedFinal,
            totalReturn = finalReturn,
            totalInvestment = totalInvestment
        )
    }

    private fun calcLumpSum(
        portfolios: List<Portfolio>,
        dates: List<String>,
        dailyReturnMap: Map<String, Map<String, Double>>,
        initialAmount: Long
    ): Result {
        var portfolioValue = initialAmount.toDouble()
        val points = mutableListOf<BacktestPoint>()

        dates.forEach { date ->
            val dayReturnPct = portfolios.sumOf { portfolio ->
                val dailyReturn = dailyReturnMap[portfolio.ticker]?.get(date) ?: 0.0
                (portfolio.weightPercent / 100.0) * dailyReturn
            }
            portfolioValue *= (1.0 + dayReturnPct / 100.0)
            points.add(BacktestPoint(date = date, value = portfolioValue))
        }

        val finalValue = portfolioValue.roundToLong()
        val totalReturn = ((finalValue - initialAmount).toDouble() / initialAmount) * 100.0

        return Result(
            points = points,
            estimatedFinalValue = finalValue,
            totalReturn = totalReturn,
            totalInvestment = initialAmount
        )
    }
}