package com.d102.wye.presentation.strategy.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d102.wye.domain.common.BaseResult
import com.d102.wye.domain.repository.PortfolioRepository
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
    private val portfolioRepository: PortfolioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<StrategyListData>>(UiState.Idle)
    val uiState: StateFlow<UiState<StrategyListData>> = _uiState.asStateFlow()

    init {
        loadStrategies()
    }

    fun loadStrategies() {
        viewModelScope.launch {
            _uiState.update { UiState.Loading }

            when (val result = portfolioRepository.getPortfolioList()) {
                is BaseResult.Success -> {
                    val strategies = result.data.map { item ->
                        StrategyCardUiModel(
                            id = item.portfolioId.toString(),
                            title = item.title,
                            date = item.createdAt,
                            tags = item.etfList.map { "#${it.name}" },
                            totalReturn = item.totalReturn
                        )
                    }
                    _uiState.update {
                        UiState.Success(
                            StrategyListData(
                                realAsset = null,  // TODO: 실제 자산 연동 후 분리
                                strategies = strategies
                            )
                        )
                    }
                }
                is BaseResult.Error -> {
                    _uiState.update { UiState.Error(result.error.message) }
                }
            }
        }
    }

    fun onDeleteStrategy(strategyId: String) {
        viewModelScope.launch {
            when (portfolioRepository.deletePortfolio(strategyId.toLong())) {
                is BaseResult.Success -> {
                    val current = (_uiState.value as? UiState.Success)?.data ?: return@launch
                    _uiState.update {
                        UiState.Success(
                            current.copy(
                                strategies = current.strategies.filter { it.id != strategyId }
                            )
                        )
                    }
                }
                is BaseResult.Error -> Unit // TODO: 에러 처리
            }
        }
    }
}

// ─────────────────────────────────────────
// 화면 데이터 모델
// ─────────────────────────────────────────

data class StrategyCardUiModel(
    val id: String,
    val title: String,
    val date: String,
    val tags: List<String>,
    val totalReturn: Double = 0.0,
    val isRealAsset: Boolean = false
)

data class StrategyListData(
    val realAsset: StrategyCardUiModel? = null,
    val strategies: List<StrategyCardUiModel> = emptyList()
) {
    val isCompletelyEmpty = realAsset == null && strategies.isEmpty()
}