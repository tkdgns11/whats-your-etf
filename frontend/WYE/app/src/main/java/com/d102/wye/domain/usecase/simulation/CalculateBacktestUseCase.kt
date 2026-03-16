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
        // 전체 공통 날짜 교집합
        val allCommonDates: List<String> = priceHistories.values
            .map { it.content.map { p -> p.date }.toSet() }
            .reduceOrNull { acc, set -> acc.intersect(set) }
            ?.sorted()
            ?: return Result(emptyList(), 0L, 0.0, 0L)

        if (allCommonDates.isEmpty()) return Result(emptyList(), 0L, 0.0, 0L)

        // periodMonths 기간만큼 필터링
        val startDate = LocalDate.now().minusMonths(periodMonths.toLong()).toString()
        val commonDates = allCommonDates.filter { it >= startDate }

        if (commonDates.isEmpty()) return Result(emptyList(), 0L, 0.0, 0L)

        // 날짜 → 주가 빠른 조회용 맵
        val priceMap: Map<String, Map<String, Double>> =
            priceHistories.mapValues { (_, history) ->
                history.content.associate { it.date to it.stockPrice.toDouble() }
            }

        return when (investmentType) {
            InvestmentType.REGULAR_SAVING -> calcInstallment(
                portfolios, commonDates, priceMap, investmentAmount, periodMonths
            )
            InvestmentType.LUMP_SUM -> calcLumpSum(
                portfolios, commonDates, priceMap, investmentAmount
            )
        }
    }

    /**
     * 적립형 계산
     *
     * 매월:
     * 1. ETF별 해당 월 평균 주가 계산
     * 2. 각 ETF에 (monthlyAmount × weight) 만큼 투자 → 평균 단가로 수량 매입
     * 3. 월말 평가금액 = Σ(누적 수량_i × 월말 주가_i)
     *
     * y축: 월말 평가금액 (원)
     */
    private fun calcInstallment(
        portfolios: List<Portfolio>,
        dates: List<String>,
        priceMap: Map<String, Map<String, Double>>,
        monthlyAmount: Long,
        periodMonths: Int
    ): Result {
        // ETF별 누적 보유 수량
        val accumulatedShares = mutableMapOf<String, Double>()
        portfolios.forEach { accumulatedShares[it.ticker] = 0.0 }

        // 월별로 날짜 그룹핑
        val datesByMonth: Map<String, List<String>> = dates.groupBy { it.take(7) }

        val points = mutableListOf<BacktestPoint>()

        datesByMonth.entries.sortedBy { it.key }.forEach { (_, monthDates) ->
            portfolios.forEach { portfolio ->
                val ticker = portfolio.ticker
                val weight = portfolio.weightPercent / 100.0
                val monthlyInvestPerEtf = monthlyAmount * weight

                // 해당 월 ETF 평균 주가
                val monthlyPrices = monthDates.mapNotNull { priceMap[ticker]?.get(it) }
                if (monthlyPrices.isEmpty()) return@forEach

                val avgPrice = monthlyPrices.average()
                if (avgPrice <= 0) return@forEach

                // 평균 단가로 수량 매입
                accumulatedShares[ticker] = (accumulatedShares[ticker] ?: 0.0) + (monthlyInvestPerEtf / avgPrice)
            }

            // 월말 평가금액 = Σ(누적 수량_i × 월말 주가_i)
            val lastDate = monthDates.last()
            val monthEndValue = portfolios.sumOf { portfolio ->
                val ticker = portfolio.ticker
                val shares = accumulatedShares[ticker] ?: 0.0
                val lastPrice = priceMap[ticker]?.get(lastDate) ?: 0.0
                shares * lastPrice
            }

            points.add(BacktestPoint(date = lastDate, value = monthEndValue))
        }

        val totalInvestment = monthlyAmount * periodMonths
        val estimatedFinalValue = points.lastOrNull()?.value?.roundToLong() ?: 0L
        val totalReturn = if (totalInvestment > 0)
            ((estimatedFinalValue - totalInvestment).toDouble() / totalInvestment) * 100.0
        else 0.0

        return Result(
            points = points,
            estimatedFinalValue = estimatedFinalValue,
            totalReturn = totalReturn,
            totalInvestment = totalInvestment
        )
    }

    /**
     * 거치형 계산
     *
     * 최초 일괄 투자 → 일별 평가금액 추적
     * y축: 일별 평가금액 (원)
     */
    private fun calcLumpSum(
        portfolios: List<Portfolio>,
        dates: List<String>,
        priceMap: Map<String, Map<String, Double>>,
        initialAmount: Long
    ): Result {
        // 최초 ETF별 매입 수량 (첫날 주가 기준)
        val firstDate = dates.first()
        val shares = mutableMapOf<String, Double>()

        portfolios.forEach { portfolio ->
            val ticker = portfolio.ticker
            val weight = portfolio.weightPercent / 100.0
            val investPerEtf = initialAmount * weight
            val firstPrice = priceMap[ticker]?.get(firstDate) ?: return@forEach
            if (firstPrice > 0) {
                shares[ticker] = investPerEtf / firstPrice
            }
        }

        val points = dates.map { date ->
            val portfolioValue = portfolios.sumOf { portfolio ->
                val ticker = portfolio.ticker
                val s = shares[ticker] ?: 0.0
                val price = priceMap[ticker]?.get(date) ?: 0.0
                s * price
            }
            BacktestPoint(date = date, value = portfolioValue)
        }

        val finalValue = points.lastOrNull()?.value?.roundToLong() ?: 0L
        val totalReturn = if (initialAmount > 0)
            ((finalValue - initialAmount).toDouble() / initialAmount) * 100.0
        else 0.0

        return Result(
            points = points,
            estimatedFinalValue = finalValue,
            totalReturn = totalReturn,
            totalInvestment = initialAmount
        )
    }
}