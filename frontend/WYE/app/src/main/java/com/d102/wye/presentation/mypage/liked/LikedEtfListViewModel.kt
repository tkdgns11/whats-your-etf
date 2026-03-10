package com.d102.wye.presentation.mypage.liked

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d102.wye.domain.common.BaseResult
import com.d102.wye.domain.model.Etf
import com.d102.wye.domain.repository.EtfRepository
import com.d102.wye.presentation.model.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LikedEtfListViewModel @Inject constructor(
    private val etfRepository: EtfRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<LikedEtfListData>>(UiState.Idle)
    val uiState: StateFlow<UiState<LikedEtfListData>> = _uiState.asStateFlow()

    private var likedEtfs: List<LikedEtfUiModel> = emptyList()

    init {
        observeLikedEtfs()
    }

    private fun observeLikedEtfs() {
        viewModelScope.launch {
            _uiState.update { UiState.Loading }
            etfRepository.getLikedEtfList().collect { etfs ->
                likedEtfs = etfs.map { it.toUiModel() }
                _uiState.update { UiState.Success(LikedEtfListData(likedEtfs = likedEtfs)) }
            }
        }
    }

    fun onLikeToggled(ticker: String) {
        val target = likedEtfs.firstOrNull { it.ticker == ticker } ?: return
        viewModelScope.launch {
            when (val result = etfRepository.toggleLike(target.toDomain())) {
                is BaseResult.Success -> Unit
                is BaseResult.Error -> _uiState.update { UiState.Error(result.error.message) }
            }
        }
    }
}

data class LikedEtfListData(
    val likedEtfs: List<LikedEtfUiModel>
)

data class LikedEtfUiModel(
    val ticker: String,
    val name: String,
    val currentPrice: Long,
    val changeRate: Double,
    val changeAmount: Long,
    val riskLevel: Int,
    val isLiked: Boolean,
)

private fun Etf.toUiModel() = LikedEtfUiModel(
    ticker = ticker,
    name = name,
    currentPrice = currentPrice,
    changeRate = changeRate,
    changeAmount = changeAmount,
    riskLevel = riskLevel,
    isLiked = true,
)

private fun LikedEtfUiModel.toDomain() = Etf(
    ticker = ticker,
    name = name,
    currentPrice = currentPrice,
    changeRate = changeRate,
    changeAmount = changeAmount,
    volume = 0L,
    riskLevel = riskLevel,
    investmentStrategy = "",
    assetClass = "",
    theme = "",
    dividendRate = 0.0,
    dividendCycle = "",
    hasDerivative = false,
    per = 0.0,
    pbr = 0.0,
    roe = 0.0,
    expenseRatio = 0.0,
    netAsset = 0L,
)
