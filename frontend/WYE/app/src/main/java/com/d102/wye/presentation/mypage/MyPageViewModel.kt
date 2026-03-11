package com.d102.wye.presentation.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d102.wye.domain.repository.AuthRepository
import com.d102.wye.presentation.model.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyPageViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    // TODO: Repository 주입
    // private val userRepository: UserRepository,
    // private val etfRepository: EtfRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<MyPageData>>(UiState.Idle)
    val uiState: StateFlow<UiState<MyPageData>> = _uiState.asStateFlow()
    private val _event = MutableSharedFlow<MyPageEvent>()
    val event: SharedFlow<MyPageEvent> = _event

    init {
        loadMyPageData()
    }

    /** 마이페이지 진입 시 필요한 데이터를 로드한다. */
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

    /** 닉네임 수정 다이얼로그를 연다. */
    fun showNicknameEditDialog() {
        updateSuccessState { data ->
            data.copy(
                isNicknameDialogVisible = true,
                nicknameDraft = data.nickname
            )
        }
    }

    /** 닉네임 수정 다이얼로그를 닫고 임시 상태를 초기화한다. */
    fun dismissNicknameEditDialog() {
        updateSuccessState { data ->
            data.copy(
                isNicknameDialogVisible = false,
                nicknameDraft = data.nickname,
                isNicknameSaving = false
            )
        }
    }

    /** 닉네임 입력 초안을 최대 20자로 제한해 저장한다. */
    fun onNicknameDraftChange(value: String) {
        updateSuccessState { data ->
            data.copy(nicknameDraft = value.take(20))
        }
    }

    /** 프로필 이미지를 임시 반영하고 추후 서버 연동 지점을 TODO로 남긴다. */
    fun onProfileImageSelected(imageUri: String) {
        if (imageUri.isBlank()) return

        viewModelScope.launch {
            updateSuccessState { data -> data.copy(profileImage = imageUri) }

            // TODO: userRepository.updateProfileImage(imageUri)
            // TODO: 업로드용 multipart/file 변환은 data 레이어에서 처리
            // TODO: 성공 시 서버가 내려준 최신 profileImageUrl로 다시 갱신
            // TODO: 실패 시 기존 이미지로 롤백하고 에러 스낵바 노출
        }
    }

    /** 닉네임 저장을 처리하고 추후 서버 동기화 지점을 TODO로 남긴다. */
    fun saveNickname() {
        val currentState = _uiState.value as? UiState.Success ?: return
        val newNickname = currentState.data.nicknameDraft.trim()
        if (newNickname.isEmpty()) return

        viewModelScope.launch {
            updateSuccessState { data -> data.copy(isNicknameSaving = true) }

            // TODO: userRepository.updateNickname(newNickname)
            // TODO: 성공 시 서버 응답의 최신 닉네임으로 profile state 갱신
            // TODO: 실패 시 에러 메시지 스낵바 노출 후 isNicknameSaving 롤백

            updateSuccessState { data ->
                data.copy(
                    nickname = newNickname,
                    nicknameDraft = newNickname,
                    isNicknameDialogVisible = false,
                    isNicknameSaving = false
                )
            }
        }
    }

    /** 로컬 세션을 정리하고 로그아웃 완료 이벤트를 화면에 전달한다. */
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _event.emit(MyPageEvent.LogoutSuccess)
        }
    }

    /** Success 상태일 때만 데이터를 변경한다. */
    private fun updateSuccessState(transform: (MyPageData) -> MyPageData) {
        _uiState.update { current ->
            when (current) {
                is UiState.Success -> UiState.Success(transform(current.data))
                else -> current
            }
        }
    }

    /** 서버 없이 마이페이지를 보여주기 위한 임시 데이터를 만든다. */
    private fun mockMyPageData(): MyPageData = MyPageData(
        nickname = "레전드투자자",
        nicknameDraft = "레전드투자자",
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
    val nicknameDraft: String,
    val profileImage: String?,
    val likedEtfCount: Int,
    val holdingEtfs: List<MyPageHoldingEtfUiModel>,
    val isNicknameDialogVisible: Boolean = false,
    val isNicknameSaving: Boolean = false
)

sealed interface MyPageEvent {
    data object LogoutSuccess : MyPageEvent
}
