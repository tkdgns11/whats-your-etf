package com.d102.wye.presentation.strategy.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d102.wye.domain.common.BaseResult
import com.d102.wye.domain.repository.PortfolioRepository
import com.d102.wye.domain.repository.SimulationRepository
import com.d102.wye.domain.state.InvestmentType
import com.d102.wye.domain.usecase.portfolio.CalculatePortfolioChartUseCase
import com.d102.wye.presentation.model.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class StrategyDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val portfolioRepository: PortfolioRepository,
    private val simulationRepository: SimulationRepository,
    private val calculatePortfolioChart: CalculatePortfolioChartUseCase
) : ViewModel() {

    private val portfolioId: Long = checkNotNull(savedStateHandle["strategyId"])

    private val _detailState = MutableStateFlow<UiState<StrategyDetailData>>(UiState.Idle)
    val detailState: StateFlow<UiState<StrategyDetailData>> = _detailState.asStateFlow()

    init {
        fetchStrategyDetail()
    }

    fun fetchStrategyDetail() {
        viewModelScope.launch {
            _detailState.value = UiState.Loading

            // 1. 포트폴리오 상세 조회
            val detail = when (val result = portfolioRepository.getPortfolioDetail(portfolioId)) {
                is BaseResult.Success -> result.data
                is BaseResult.Error -> {
                    _detailState.value = UiState.Error(result.error.message)
                    return@launch
                }
            }

            // 2. 가격이력 조회 (createdAt-1년 ~ 오늘)
            val tickers = detail.counts.map { it.ticker }
            val startDate = detail.createdAt.minusOneYear()
            val endDate = LocalDate.now().toString()

            val priceHistories = when (val result = simulationRepository.getEtfPriceHistories(
                tickers = tickers,
                startDate = startDate,
                endDate = endDate
            )) {
                is BaseResult.Success -> result.data
                is BaseResult.Error -> {
                    _detailState.value = UiState.Error(result.error.message)
                    return@launch
                }
            }

            // 3. 차트 계산 (두 구간)
            val chartResult = calculatePortfolioChart(
                counts = detail.counts,
                priceHistories = priceHistories,
                createdAt = detail.createdAt
            )

            val returnSign = { v: Double -> if (v >= 0) "+" else "" }

            _detailState.value = UiState.Success(
                StrategyDetailData(
                    id = detail.portfolioId,
                    title = detail.portfolioName,
                    saveDate = detail.createdAt,
                    investmentType = when (detail.portfolioType) {
                        InvestmentType.REGULAR_SAVING -> "적립형"
                        InvestmentType.LUMP_SUM -> "거치형"
                    },
                    summaryMetrics = listOf(
                        "예상 최종 자산" to chartResult.estimatedFinalValue.formatAmount(),
                        "실제 수익률" to "${returnSign(chartResult.recentReturn)}${
                            "%.2f".format(
                                chartResult.recentReturn
                            )
                        }%",
                        "총 투자 금액" to detail.investAmount.formatAmount()
                    ),
                    recentPerformance = PerformanceData(
                        period = "저장 시점~현재",
                        rate = "${returnSign(chartResult.recentReturn)}${"%.1f".format(chartResult.recentReturn)}%",
                        dateRange = "${detail.createdAt} (저장일) - $endDate (현재)",
                        points = chartResult.recentPoints
                    ),
                    pastPerformance = PerformanceData(
                        period = "과거 1년",
                        rate = "${returnSign(chartResult.pastReturn)}${"%.1f".format(chartResult.pastReturn)}%",
                        dateRange = "$startDate - ${detail.createdAt} (저장일)",
                        points = chartResult.pastPoints
                    ),
                    timelines = emptyList(),   // TODO: 타임라인 API 연결
                    relatedNews = emptyList()  // TODO: 뉴스 API 연결
                )
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// UiModel
// ─────────────────────────────────────────────────────────────────────────────

data class StrategyDetailData(
    val id: Long,
    val title: String,
    val saveDate: String,
    val investmentType: String,
    val summaryMetrics: List<Pair<String, String>>,
    val recentPerformance: PerformanceData,
    val pastPerformance: PerformanceData,
    val timelines: List<TimelineItem>,
    val relatedNews: List<NewsItem>
)

data class PerformanceData(
    val period: String,
    val rate: String,
    val dateRange: String,
    val points: List<com.d102.wye.domain.model.BacktestPoint>  // ← Float → BacktestPoint
)

data class TimelineItem(
    val date: String,
    val title: String,
    val content: String
)

data class NewsItem(
    val title: String,
    val summary: String,
    val source: String
)

// ─────────────────────────────────────────────────────────────────────────────
// 유틸
// ─────────────────────────────────────────────────────────────────────────────

private fun String.minusOneYear(): String {
    val parts = split("-")
    if (parts.size != 3) return this
    val year = parts[0].toIntOrNull() ?: return this
    return "${year - 1}-${parts[1]}-${parts[2]}"
}

private fun Long.formatAmount(): String {
    val abs = Math.abs(this)
    val eok = abs / 100_000_000L
    val man = (abs % 100_000_000L) / 10_000L
    return when {
        eok > 0 && man > 0 -> "${eok}억 ${"%,d".format(man)}만원"
        eok > 0 -> "${eok}억원"
        man > 0 -> "${"%,d".format(man)}만원"
        else -> "${"%,d".format(abs % 10_000L)}원"
    }
}