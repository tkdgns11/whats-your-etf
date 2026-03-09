package com.d102.wye.presentation.mypage.liked

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d102.wye.presentation.model.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LikedEtfListViewModel @Inject constructor(
    // TODO: domain/repository/EtfRepository 주입
    // private val etfRepository: EtfRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<LikedEtfListData>>(UiState.Idle)
    val uiState: StateFlow<UiState<LikedEtfListData>> = _uiState.asStateFlow()

    private var likedEtfs: List<LikedEtfUiModel> = emptyList()

    init {
        loadLikedEtfs()
    }

    fun loadLikedEtfs() {
        viewModelScope.launch {
            _uiState.update { UiState.Loading }

            // TODO: data/repository 구현 후 etfRepository.getLikedEtfList()를 collect해서 반영
            // TODO: domain model -> presentation UI model 변환 로직 추가
            // TODO: 실패 시 UiState.Error로 매핑
            likedEtfs = listOf(
                LikedEtfUiModel("069500", "KODEX 200", 32_450, 1.24, 400, 1, true),
                LikedEtfUiModel("360750", "TIGER 미국S&P500", 104_820, 2.15, 2_210, 1, true),
                LikedEtfUiModel("133690", "TIGER 미국나스닥100", 15_340, 0.85, 130, 2, true),
                LikedEtfUiModel("438900", "SOL 미국배당다우존스", 10_120, 0.42, 45, 3, true),
                LikedEtfUiModel("114800", "KODEX 인버스", 2_145, -2.31, 50, 5, true),
            )

            _uiState.update { UiState.Success(LikedEtfListData(likedEtfs = likedEtfs)) }
        }
    }

    fun onLikeToggled(ticker: String) {
        // TODO: API 연동 시 etfRepository.toggleLike(ticker) 호출
        // TODO: 성공 후 getLikedEtfList() 재수집 또는 현재 목록 갱신
        // TODO: 실패 시 스낵바 노출용 UiState.Error 또는 별도 event로 전달
        likedEtfs = likedEtfs.map {
            if (it.ticker == ticker) it.copy(isLiked = !it.isLiked) else it
        }.filter { it.isLiked }

        _uiState.update { UiState.Success(LikedEtfListData(likedEtfs = likedEtfs)) }
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
