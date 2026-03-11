package com.d102.wye.domain.usecase.simulation

import com.d102.wye.domain.common.BaseResult
import com.d102.wye.domain.model.EtfPriceHistory
import com.d102.wye.domain.model.Portfolio
import com.d102.wye.domain.model.SimulationResult
import com.d102.wye.domain.model.WeightedFundamentals
import com.d102.wye.domain.repository.SimulationRepository
import com.d102.wye.domain.state.InvestmentType
import javax.inject.Inject

class RunSimulationUseCase @Inject constructor(
    private val simulationRepository: SimulationRepository,
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
        val startDate: String? = null,
        val endDate: String? = null
    )

    suspend operator fun invoke(params: Params): BaseResult<SimulationResult> {
        val tickers = params.portfolios.map { it.ticker }

        // 1. 가격 이력 조회
        val priceResult = simulationRepository.getEtfPriceHistories(
            tickers = tickers,
            startDate = params.startDate,
            endDate = params.endDate
        )
        if (priceResult is BaseResult.Error) return priceResult
        val priceHistories = (priceResult as BaseResult.Success<Map<String, EtfPriceHistory>>).data

        // 2. 백테스트 계산
        val backtestResult = calculateBacktest(
            portfolios = params.portfolios,
            priceHistories = priceHistories,
            investmentAmount = params.investmentAmount,
            investmentType = params.investmentType
        )

        // TODO: 펀더멘털 / 배당금은 백엔드 필드 추가 후 연결
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