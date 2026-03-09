package com.d102.wye.presentation.strategy.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d102.wye.presentation.model.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StrategyDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val strategyId: Long = checkNotNull(savedStateHandle["strategyId"])

    private val _detailState = MutableStateFlow<UiState<StrategyDetailData>>(UiState.Idle)
    val detailState: StateFlow<UiState<StrategyDetailData>> = _detailState.asStateFlow()

    init {
        fetchStrategyDetail()
    }

    fun fetchStrategyDetail() {
        viewModelScope.launch {
            _detailState.value = UiState.Loading
            delay(800)
            _detailState.value = UiState.Success(
                StrategyDetailData(
                    id = strategyId,
                    title = "기술주 중심 성장 전략",
                    saveDate = "2026.03.02",
                    investmentType = "적립형",
                    summaryMetrics = listOf(
                        "예상 최종 자산" to "12.5억",
                        "실제 수익률" to "+2.1%",
                        "총 투자 금액" to "1억"
                    ),
                    recentPerformance = PerformanceData("최근 1개월", "+8.2%", "2024.04.15 - 2024.05.15", emptyList()),
                    pastPerformance = PerformanceData("과거 1년", "+12.4%", "2023.04.15 - 2024.04.15", emptyList()),
                    timelines = listOf(
                        TimelineItem("2024.03.20", "연준 기준금리 동결 발표", "시장 예상치 부합, 기술주 중심의 반등세 지속"),
                        TimelineItem("2024.02.15", "주요 기업 4분기 실적 발표", "반도체 섹터 어닝 서프라이즈로 포트폴리오 수익률 견인")
                    ),
                    relatedNews = listOf(
                        NewsItem("미국 ETF 시장, 인공지능 테마로 자금 유입 역대 최고 기록", "글로벌 AI 경쟁 가속화로 관련 ETF 수익률 고공행진...", ""),
                        NewsItem("반도체 수요 회복세에 관련 ETF 수익률 두 자리수 달성", "데이터센터 및 PC 시장 회복에 따른 반도체 부문의...", "")
                    )
                )
            )
        }
    }
}
data class StrategyDetailData(
    val id: Long,
    val title: String,
    val saveDate: String,
    val investmentType: String,
    val summaryMetrics: List<Pair<String, String>>, // 예상 최종 자산, 수익률 등
    val recentPerformance: PerformanceData,
    val pastPerformance: PerformanceData,
    val timelines: List<TimelineItem>,
    val relatedNews: List<NewsItem>
)

data class PerformanceData(
    val period: String,
    val rate: String,
    val dateRange: String,
    val points: List<Float> // 차트용 데이터
)

data class TimelineItem(
    val date: String,
    val title: String,
    val content: String
)

data class NewsItem(
    val title: String,
    val summary: String,
    val source: String,
)