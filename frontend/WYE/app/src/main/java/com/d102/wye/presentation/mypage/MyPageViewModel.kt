package com.d102.wye.presentation.mypage

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
class MyPageViewModel @Inject constructor(
    // TODO: Repository 주입
    // private val userRepository: UserRepository,
    // private val etfRepository: EtfRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<MyPageData>>(UiState.Idle)
    val uiState: StateFlow<UiState<MyPageData>> = _uiState.asStateFlow()

    init {
        loadMyPageData()
    }

    fun loadMyPageData() {
        viewModelScope.launch {
            _uiState.update { UiState.Loading }

            // TODO: 유저 정보 + 관심 ETF + 보유 ETF 로드
            // TODO: coroutineScope {
            // TODO:   val profileDeferred = async { userRepository.getMyProfile() }
            // TODO:   val likedDeferred = async { etfRepository.getLikedEtfList().first() }
            // TODO:   val holdingDeferred = async { userRepository.getMyHoldingEtfs() }
            // TODO:   _uiState.update { UiState.Success(...) }
            // TODO: }

            _uiState.update { UiState.Success(mockMyPageData()) }
        }
    }

    private fun mockMyPageData(): MyPageData = MyPageData(
        nickname = "레전드투자자",
        profileImage = null,
        likedEtfCount = 12,
        holdingEtfs = emptyList() // 보유 ETF 없는 상태 UI
    )
}

data class MyPageHoldingEtfUiModel(
    val ticker: String,
    val name: String,
    val changeRateText: String
)

data class MyPageData(
    val nickname: String,
    val profileImage: String?,
    val likedEtfCount: Int,
    val holdingEtfs: List<MyPageHoldingEtfUiModel>
)
