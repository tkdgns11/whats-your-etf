package com.d102.wye.domain.usecase.simulation

import com.d102.wye.domain.common.ApiError
import com.d102.wye.domain.common.BaseResult
import com.d102.wye.domain.model.EtfPriceHistory
import com.d102.wye.domain.model.Portfolio
import com.d102.wye.domain.model.SimulationResult
import com.d102.wye.domain.model.WeightedFundamentals
import com.d102.wye.domain.state.InvestmentType
import javax.inject.Inject

class RunSimulationUseCase @Inject constructor(
    private val calculateBacktest: CalculateBacktestUseCase,
    // TODO: 백엔드 per/pbr/roe/dividendYield 필드 추가 후 아래 UseCase 연결
    // private val calculateWeightedFundamentals: CalculateWeightedFundamentalsUseCase,
    // private val calculateExpectedDividend: CalculateExpectedDividendUseCase
) {

    data class Params(
        val portfolios: List<Portfolio>,
        val investmentAmount: Long,
        val investmentType: InvestmentType,
        val periodMonths: Int,
        val priceHistories: Map<String, EtfPriceHistory>,
        val startDate: String? = null,
        val endDate: String? = null
    )

    suspend operator fun invoke(params: Params): BaseResult<SimulationResult> {
        if (params.priceHistories.isEmpty()) {
            return BaseResult.Error(
                ApiError(code = -1, message = "가격 이력 데이터가 없습니다")
            )
        }

        val backtestResult = calculateBacktest(
            portfolios = params.portfolios,
            priceHistories = params.priceHistories,
            investmentAmount = params.investmentAmount,
            investmentType = params.investmentType,
            periodMonths = params.periodMonths  // ← 전달
        )

        return BaseResult.Success(
            SimulationResult(
                backtestPoints = backtestResult.points,
                fundamentals = WeightedFundamentals(per = 0.0, pbr = 0.0, roe = 0.0),
                expectedAnnualDividend = 0L,
                expectedMonthlyDividend = 0L,
                estimatedFinalValue = backtestResult.estimatedFinalValue,
                totalReturn = backtestResult.totalReturn,
                totalInvestment = backtestResult.totalInvestment
            )
        )
    }
}