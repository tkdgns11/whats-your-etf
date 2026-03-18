package com.d102.wye.presentation.mypage.liked

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d102.wye.domain.common.BaseResult
import com.d102.wye.domain.model.FavoriteEtf
import com.d102.wye.domain.model.FavoriteEtfSort
import com.d102.wye.domain.repository.UserRepository
import com.d102.wye.presentation.model.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class LikedEtfListViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<LikedEtfListData>>(UiState.Idle)
    val uiState: StateFlow<UiState<LikedEtfListData>> = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<LikedEtfListEvent>()
    val event: SharedFlow<LikedEtfListEvent> = _event.asSharedFlow()

    private var currentSort = FavoriteEtfSort.RECENT

    init {
        loadLikedEtfs()
    }

    fun loadLikedEtfs(sort: FavoriteEtfSort = currentSort) {
        viewModelScope.launch {
            currentSort = sort
            _uiState.update { UiState.Loading }
            when (val result = userRepository.getFavoriteEtfs(sort)) {
                is BaseResult.Success -> {
                    _uiState.update {
                        UiState.Success(
                            LikedEtfListData(
                                likedEtfs = result.data.favorites.map { it.toUiModel() },
                                selectedSort = sort,
                                totalCount = result.data.totalCount
                            )
                        )
                    }
                }
                is BaseResult.Error -> _uiState.update { UiState.Error(result.error.message) }
            }
        }
    }

    fun onSortChanged(sort: FavoriteEtfSort) {
        if (sort == currentSort) return
        loadLikedEtfs(sort)
    }

    fun onLikeToggled() {
        viewModelScope.launch {
            _event.emit(LikedEtfListEvent.ShowMessage("관심 ETF 추가/삭제는 다음 단계에서 서버 연동합니다."))
        }
    }
}

data class LikedEtfListData(
    val likedEtfs: List<LikedEtfUiModel>,
    val selectedSort: FavoriteEtfSort,
    val totalCount: Int
)

data class LikedEtfUiModel(
    val etfId: Long,
    val ticker: String,
    val name: String,
    val currentPrice: Long,
    val changeRate: Double,
    val changeAmount: Long,
    val riskType: String,
    val isLiked: Boolean,
)

private fun FavoriteEtf.toUiModel() = LikedEtfUiModel(
    etfId = etfId,
    ticker = ticker,
    name = name,
    currentPrice = currentPrice,
    changeRate = changeRate,
    changeAmount = 0L,
    riskType = category ?: DEFAULT_RISK_TYPE,
    isLiked = true,
)

sealed interface LikedEtfListEvent {
    data class ShowMessage(val message: String) : LikedEtfListEvent
}

private const val DEFAULT_RISK_TYPE = "위험중립형"
