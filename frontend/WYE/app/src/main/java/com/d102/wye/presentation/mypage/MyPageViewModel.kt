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
    // private val etfRepository: EtfRepository,
    // private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<MyPageData>>(UiState.Idle)
    val uiState: StateFlow<UiState<MyPageData>> = _uiState.asStateFlow()

    // 로그아웃 완료 이벤트 (단발성 → SharedFlow)
    private val _logoutEvent = MutableStateFlow(false)
    val logoutEvent: StateFlow<Boolean> = _logoutEvent.asStateFlow()

    init {
        loadMyPageData()
    }

    fun loadMyPageData() {
        viewModelScope.launch {
            _uiState.update { UiState.Loading }

            // TODO: 유저 정보 + 관심 ETF + 보유 ETF 로드
            // coroutineScope {
            //     val userDeferred     = async { userRepository.getMyProfile() }
            //     val likedEtfDeferred = async { etfRepository.getLikedEtfList().first() }
            //     val myEtfDeferred    = async { userRepository.getMyEtfs() }
            //
            //     val user = userDeferred.await()
            //     if (user is BaseResult.Success) {
            //         _uiState.update {
            //             UiState.Success(
            //                 MyPageData(
            //                     nickname = user.data.nickname,
            //                     email = user.data.email,
            //                     profileImage = user.data.profileImage,
            //                     likedEtfs = likedEtfDeferred.await().map { it.toMyPageEtfUiModel() },
            //                     myEtfs = (myEtfDeferred.await() as? BaseResult.Success)?.data
            //                         ?.map { it.toMyPageEtfUiModel() } ?: emptyList()
            //                 )
            //             )
            //         }
            //     }
            // }
        }
    }

    fun onLogoutClick() {
        viewModelScope.launch {
            // TODO: authRepository.logout()
            // 성공 시 로그인 화면으로 이동 트리거
            _logoutEvent.update { true }
        }
    }

    fun onPasswordChangeClick() {
        // TODO: 비밀번호 변경 화면 이동은 Screen에서 람다로 처리
        //       ViewModel은 필요한 경우 서버 호출만 담당
    }
}

// ─────────────────────────────────────────
// 화면 데이터 모델
// ─────────────────────────────────────────

data class MyPageData(
    val nickname: String,
    val email: String,
    val profileImage: String?,
    val likedEtfs: List<MyPageEtfUiModel>,    // 관심 ETF
    val myEtfs: List<MyPageEtfUiModel>         // 보유 ETF (마이데이터)
)

data class MyPageEtfUiModel(
    val ticker: String,
    val name: String,
    val currentPrice: Long,
    val changeRate: Double
)