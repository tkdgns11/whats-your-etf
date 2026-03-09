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

    fun showNicknameEditDialog() {
        updateSuccessState { data ->
            data.copy(
                isNicknameDialogVisible = true,
                nicknameDraft = data.nickname
            )
        }
    }

    fun dismissNicknameEditDialog() {
        updateSuccessState { data ->
            data.copy(
                isNicknameDialogVisible = false,
                nicknameDraft = data.nickname,
                isNicknameSaving = false
            )
        }
    }

    fun onNicknameDraftChange(value: String) {
        updateSuccessState { data ->
            data.copy(nicknameDraft = value.take(20))
        }
    }

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

    private fun updateSuccessState(transform: (MyPageData) -> MyPageData) {
        _uiState.update { current ->
            when (current) {
                is UiState.Success -> UiState.Success(transform(current.data))
                else -> current
            }
        }
    }

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
