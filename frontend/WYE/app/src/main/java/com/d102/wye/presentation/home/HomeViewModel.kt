package com.d102.wye.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d102.wye.presentation.model.NewsUiModel
import com.d102.wye.presentation.model.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(
    // TODO: Repository 주입
    // private val etfRepository: EtfRepository,
    // private val newsRepository: NewsRepository,
    // private val portfolioRepository: PortfolioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<HomeData>>(UiState.Idle)
    val uiState: StateFlow<UiState<HomeData>> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.update { UiState.Loading }

            // TODO: 세 가지 데이터 병렬 로드
            // coroutineScope {
            //     val top10Deferred     = async { etfRepository.getTop10ByVolume() }
            //     val newsDeferred      = async { newsRepository.getLatestNews() }
            //     val portfolioDeferred = async { portfolioRepository.getMyReturnRate() }
            //
            //     _uiState.update {
            //         UiState.Success(
            //             HomeData(
            //                 top10Etfs = top10Deferred.await().map { it.toUiModel() },
            //                 portfolioReturnRate = portfolioDeferred.await(),
            //                 newsList = newsDeferred.await().map { it.toUiModel() }
            //             )
            //         )
            //     }
            // }
        }
    }
}

// ─────────────────────────────────────────
// 화면 전용 데이터 모델
// UiState는 presentation/model/UiState.kt의 공통 클래스 사용
// ─────────────────────────────────────────

data class HomeData(
    val top10Etfs: List<Top10EtfUiModel>,
    val portfolioReturnRate: Double?,   // null = 포트폴리오 미설정
    val newsList: List<NewsUiModel>
)

data class Top10EtfUiModel(
    val ticker: String,
    val name: String,
    val volume: Long,
    val changeRate: Double
)