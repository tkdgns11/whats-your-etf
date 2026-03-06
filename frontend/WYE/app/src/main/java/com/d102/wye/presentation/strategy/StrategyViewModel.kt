package com.d102.wye.presentation.strategy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d102.wye.domain.state.InvestmentType
import com.d102.wye.presentation.model.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StrategyViewModel @Inject constructor(
    // TODO: Repository 주입
    // private val portfolioRepository: PortfolioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<StrategyListData>>(UiState.Idle)
    val uiState: StateFlow<UiState<StrategyListData>> = _uiState.asStateFlow()

    init {
        loadStrategies()
    }

    fun loadStrategies() {
        viewModelScope.launch {
            _uiState.update { UiState.Loading }

            // TODO: 저장된 전략 목록 Flow 구독 (Room 캐시)
            // portfolioRepository.getStrategyList().collect { strategies ->
            //     _uiState.update {
            //         UiState.Success(
            //             StrategyListData(
            //                 strategies = strategies.map { it.toStrategyCardUiModel() }
            //             )
            //         )
            //     }
            // }
        }
    }

    fun onDeleteStrategy(strategyId: Long) {
        viewModelScope.launch {
            // TODO: portfolioRepository.deleteStrategy(strategyId)
        }
    }
}

// ─────────────────────────────────────────
// 화면 데이터 모델
// ─────────────────────────────────────────

data class StrategyListData(
    val strategies: List<StrategyCardUiModel>
)

data class StrategyCardUiModel(
    val id: Long,
    val name: String,
    val investmentType: InvestmentType,
    val totalAmount: Long,
    val cumulativeReturn: Double,        // 누적 수익률 (%)
    val createdAt: String,
    val topEtfTickers: List<String>      // 대표 ETF 티커 (카드에 미리보기용)
)