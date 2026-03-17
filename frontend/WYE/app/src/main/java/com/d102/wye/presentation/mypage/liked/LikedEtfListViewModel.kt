package com.d102.wye.presentation.mypage.liked

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d102.wye.domain.common.BaseResult
import com.d102.wye.domain.model.EtfLikeData
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
            when (val result = etfRepository.toggleLike(target.toEtfLikeData())) {
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
    val riskType: String,
    val isLiked: Boolean,
)

// ─── Domain → UiModel 변환 ───────────────────────────────────────
private fun EtfLikeData.toUiModel() = LikedEtfUiModel(
    ticker = ticker,
    name = name,
    currentPrice = currentPrice,
    changeRate = changeRate,
    changeAmount = changeAmount,
    riskType = riskType,
    isLiked = true,
)

// ─── UiModel → EtfLikeData 변환 (toggleLike 전용) ────────────────
private fun LikedEtfUiModel.toEtfLikeData() = EtfLikeData(
    ticker = ticker,
    name = name,
    currentPrice = currentPrice,
    changeRate = changeRate,
    changeAmount = changeAmount,
    riskType = riskType,
)
