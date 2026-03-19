package com.d102.wye.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d102.wye.domain.common.BaseResult
import com.d102.wye.domain.model.BacktestPoint
import com.d102.wye.domain.model.News
import com.d102.wye.domain.model.PortfolioDetail
import com.d102.wye.domain.model.PortfolioListItem
import com.d102.wye.domain.model.TopVolumeEtf
import com.d102.wye.domain.repository.EtfRepository
import com.d102.wye.domain.repository.NewsRepository
import com.d102.wye.domain.repository.PortfolioRepository
import com.d102.wye.domain.repository.SimulationRepository
import com.d102.wye.domain.state.InvestmentType
import com.d102.wye.domain.usecase.portfolio.CalculatePortfolioChartUseCase
import com.d102.wye.presentation.model.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val newsRepository: NewsRepository,
    private val etfRepository: EtfRepository,
    private val portfolioRepository: PortfolioRepository,
    private val simulationRepository: SimulationRepository,
    private val calculatePortfolioChartUseCase: CalculatePortfolioChartUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<HomeData>>(UiState.Idle)
    val uiState: StateFlow<UiState<HomeData>> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.update { UiState.Loading }

            coroutineScope {
                val newsDeferred = async { newsRepository.getNewsList() }
                val topVolumeDeferred = async { etfRepository.getTopVolumeEtfs() }
                val portfoliosDeferred = async { loadHomePortfolios() }

                when (val newsResult = newsDeferred.await()) {
                    is BaseResult.Error -> _uiState.update { UiState.Error(newsResult.error.message) }
                    is BaseResult.Success -> {
                        when (val topVolumeResult = topVolumeDeferred.await()) {
                            is BaseResult.Success -> {
                                _uiState.update {
                                    UiState.Success(
                                        HomeData(
                                            top10Etfs = topVolumeResult.data.map { it.toHomeTop10UiModel() },
                                            portfolios = portfoliosDeferred.await(),
                                            newsList = newsResult.data
                                                .take(HOME_NEWS_LIMIT)
                                                .map { it.toHomeNewsUiModel() }
                                        )
                                    )
                                }
                            }
                            is BaseResult.Error -> _uiState.update { UiState.Error(topVolumeResult.error.message) }
                        }
                    }
                }
            }
        }
    }

    /** 뉴스 도메인 모델을 홈 화면 뉴스 카드 UI 모델로 변환한다. */
    private fun News.toHomeNewsUiModel() = HomeNewsUiModel(
        id = id,
        category = categoryName,
        title = title,
        timeAgo = publishedAt.toTimeAgo(),
        source = source,
        thumbnailUrl = thumbnailUrl
    )

    /** 거래량 TOP 10 도메인 모델을 홈 탭 UI 모델로 변환한다. */
    private fun TopVolumeEtf.toHomeTop10UiModel() = Top10EtfUiModel(
        ticker = ticker,
        name = name,
        changeRate = dailyReturn
    )

    /** 서버 시간을 홈 화면 카드용 상대 시간 문자열로 변환한다. */
    private fun String.toTimeAgo(): String {
        return runCatching {
            val publishedAt = LocalDateTime.parse(this, DateTimeFormatter.ISO_DATE_TIME)
            val duration = Duration.between(publishedAt, LocalDateTime.now())
            when {
                duration.toMinutes() < 1 -> "방금 전"
                duration.toHours() < 1 -> "${duration.toMinutes()}분 전"
                duration.toDays() < 1 -> "${duration.toHours()}시간 전"
                duration.toDays() < 7 -> "${duration.toDays()}일 전"
                else -> publishedAt.toLocalDate().toString()
            }
        }.getOrDefault(this)
    }

    private suspend fun loadHomePortfolios(): List<PortfolioSummaryUiModel> = coroutineScope {
        val portfolioList = when (val result = portfolioRepository.getPortfolioList()) {
            is BaseResult.Success -> result.data
            is BaseResult.Error -> return@coroutineScope emptyList()
        }

        portfolioList.map { portfolio ->
            async { buildPortfolioSummary(portfolio) }
        }.awaitAll().filterNotNull()
    }

    private suspend fun buildPortfolioSummary(portfolio: PortfolioListItem): PortfolioSummaryUiModel? {
        val detail = when (val result = portfolioRepository.getPortfolioDetail(portfolio.portfolioId)) {
            is BaseResult.Success -> result.data
            is BaseResult.Error -> return null
        }

        val priceHistories = when (val result = simulationRepository.getEtfPriceHistories(
            tickers = detail.counts.map { it.ticker },
            startDate = detail.createdAt.minusOneYear(),
            endDate = LocalDate.now().toString()
        )) {
            is BaseResult.Success -> result.data
            is BaseResult.Error -> return null
        }

        val chartResult = calculatePortfolioChartUseCase(
            counts = detail.counts,
            priceHistories = priceHistories,
            createdAt = detail.createdAt
        )

        return detail.toHomePortfolioSummary(chartResult)
    }
}

data class HomeData(
    val top10Etfs: List<Top10EtfUiModel>,
    val portfolios: List<PortfolioSummaryUiModel>,
    val newsList: List<HomeNewsUiModel>
)

data class Top10EtfUiModel(
    val ticker: String,
    val name: String,
    val changeRate: Double
)

data class PortfolioSummaryUiModel(
    val id: Long,
    val name: String,
    val totalAmount: String,
    val profitRateText: String,
    val profitAmountText: String,
    val chartPoints: List<BacktestPoint>,
    val pastPointCount: Int,
    val investmentType: InvestmentType,
    val isPositive: Boolean,
)

data class HomeNewsUiModel(
    val id: Long,
    val category: String,
    val title: String,
    val timeAgo: String,
    val source: String,
    val thumbnailUrl: String? = null
)

private const val HOME_NEWS_LIMIT = 2

private fun PortfolioDetail.toHomePortfolioSummary(
    chartResult: CalculatePortfolioChartUseCase.Result
) = PortfolioSummaryUiModel(
    id = portfolioId,
    name = portfolioName,
    totalAmount = chartResult.estimatedFinalValue.formatAmount(),
    profitRateText = chartResult.recentReturn.toSignedPercentText(),
    profitAmountText = (chartResult.estimatedFinalValue - investAmount).toSignedAmountText(),
    chartPoints = (chartResult.pastPoints + chartResult.recentPoints)
        .distinctBy { it.date }
        .sortedBy { it.date },
    pastPointCount = chartResult.pastPoints.size,
    investmentType = portfolioType,
    isPositive = chartResult.recentReturn >= 0
)

private fun String.minusOneYear(): String {
    val parts = split("-")
    if (parts.size != 3) return this
    val year = parts[0].toIntOrNull() ?: return this
    return "${year - 1}-${parts[1]}-${parts[2]}"
}

private fun Double.toSignedPercentText(): String {
    val sign = if (this >= 0) "+" else ""
    return "$sign${"%.2f".format(this)}%"
}

private fun Long.toSignedAmountText(): String {
    val sign = if (this >= 0) "+" else "-"
    return "(${sign}${kotlin.math.abs(this).formatAmount()})"
}

private fun Long.formatAmount(): String {
    val eok = this / 100_000_000L
    val man = (this % 100_000_000L) / 10_000L
    return when {
        eok > 0 && man > 0 -> "${eok}억 ${"%,d".format(man)}만원"
        eok > 0 -> "${eok}억원"
        man > 0 -> "${"%,d".format(man)}만원"
        else -> "${"%,d".format(this % 10_000L)}원"
    }
}
