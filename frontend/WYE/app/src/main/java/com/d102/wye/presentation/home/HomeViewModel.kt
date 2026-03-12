package com.d102.wye.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d102.wye.domain.common.BaseResult
import com.d102.wye.domain.model.News
import com.d102.wye.domain.repository.NewsRepository
import com.d102.wye.presentation.model.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val newsRepository: NewsRepository,
    // TODO: private val marketRepository: MarketRepository,
    // TODO: private val portfolioRepository: PortfolioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<HomeData>>(UiState.Idle)
    val uiState: StateFlow<UiState<HomeData>> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.update { UiState.Loading }

            when (val newsResult = newsRepository.getNewsList()) {
                is BaseResult.Success -> {
                    _uiState.update {
                        UiState.Success(
                            mockHomeData().copy(
                                newsList = newsResult.data
                                    .take(HOME_NEWS_LIMIT)
                                    .map { it.toHomeNewsUiModel() }
                            )
                        )
                    }
                }

                is BaseResult.Error -> {
                    _uiState.update { UiState.Error(newsResult.error.message) }
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

    private fun mockHomeData(): HomeData = HomeData(
        top10Etfs = listOf(
            Top10EtfUiModel("069500", "KODEX 200", 1.24),
            Top10EtfUiModel("360750", "TIGER 미국 S&P500", 0.85),
            Top10EtfUiModel("114800", "KODEX 인버스", -0.98),
            Top10EtfUiModel("091160", "KODEX 반도체", 1.10),
            Top10EtfUiModel("133690", "TIGER 나스닥100", 0.42),
            Top10EtfUiModel("122630", "KODEX 레버리지", -0.12),
            Top10EtfUiModel("305720", "KODEX 2차전지", -1.54),
            Top10EtfUiModel("438900", "SOL 미국배당", 0.15),
            Top10EtfUiModel("371460", "TIGER 차이나전기차SOL", -0.72),
            Top10EtfUiModel("360200", "ACE 미국 S&P500", 0.81)
        ),
        portfolio = PortfolioSummaryUiModel(
            name = "내 포트폴리오 1",
            totalAmount = "123,456,789 원",
            profitRateText = "+11.4%",
            profitAmountText = "(14,074,074원)"
        ),
        newsList = listOf(
            HomeNewsUiModel(
                id = 1001L,
                category = "위험형 ETF",
                title = "나스닥 100 3배 레버리지, 변동성 확대에도 투자자 매수세 집중",
                timeAgo = "2시간 전",
                source = "파이낸셜 뉴스"
            ),
            HomeNewsUiModel(
                id = 1002L,
                category = "위험형 ETF",
                title = "반도체 섹터 ETF, 인공지능 수요 폭증에 수익률 고공행진",
                timeAgo = "5시간 전",
                source = "경제타임즈"
            )
        )
    )
}

data class HomeData(
    val top10Etfs: List<Top10EtfUiModel>,
    val portfolio: PortfolioSummaryUiModel?,
    val newsList: List<HomeNewsUiModel>
)

data class Top10EtfUiModel(
    val ticker: String,
    val name: String,
    val changeRate: Double
)

data class PortfolioSummaryUiModel(
    val name: String,
    val totalAmount: String,
    val profitRateText: String,
    val profitAmountText: String
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
