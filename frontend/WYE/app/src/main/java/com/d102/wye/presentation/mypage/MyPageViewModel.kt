package com.d102.wye.presentation.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d102.wye.domain.common.BaseResult
import com.d102.wye.domain.model.FavoriteEtfSort
import com.d102.wye.domain.repository.AuthRepository
import com.d102.wye.domain.repository.UserRepository
import com.d102.wye.domain.usecase.user.ValidateNicknameUseCase
import com.d102.wye.presentation.model.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyPageViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val validateNicknameUseCase: ValidateNicknameUseCase,
    // private val etfRepository: EtfRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<MyPageData>>(UiState.Idle)
    val uiState: StateFlow<UiState<MyPageData>> = _uiState.asStateFlow()
    private val _event = MutableSharedFlow<MyPageEvent>()
    val event: SharedFlow<MyPageEvent> = _event

    init {
        observeFavoriteEtfChanges()
        loadMyPageData()
    }

    /** 마이페이지 진입 시 필요한 데이터를 로드한다. */
    fun loadMyPageData() {
        viewModelScope.launch {
            _uiState.update { UiState.Loading }
            val profileResult = userRepository.getMyProfile()
            val favoriteResult = userRepository.getFavoriteEtfs(FavoriteEtfSort.RECENT)

            when (profileResult) {
                is BaseResult.Error -> _uiState.update { UiState.Error(profileResult.error.message) }
                is BaseResult.Success -> {
                    val likedEtfCount = when (favoriteResult) {
                        is BaseResult.Success -> favoriteResult.data.totalCount
                        is BaseResult.Error -> 0
                    }

                    _uiState.update {
                        UiState.Success(
                            MyPageData(
                                nickname = profileResult.data.nickname,
                                nicknameDraft = profileResult.data.nickname,
                                profileImage = profileResult.data.profileImage,
                                likedEtfCount = likedEtfCount,
                                holdingEtfs = emptyList()
                            )
                        )
                    }
                }
            }
        }
    }

    /** 닉네임 수정 다이얼로그를 연다. */
    fun showNicknameEditDialog() {
        updateSuccessState { data ->
            data.copy(
                isNicknameDialogVisible = true,
                nicknameDraft = data.nickname,
                nicknameValidationMessage = null
            )
        }
    }

    /** 닉네임 수정 다이얼로그를 닫고 임시 상태를 초기화한다. */
    fun dismissNicknameEditDialog() {
        updateSuccessState { data ->
            data.copy(
                isNicknameDialogVisible = false,
                nicknameDraft = data.nickname,
                isNicknameSaving = false,
                nicknameValidationMessage = null
            )
        }
    }

    /** 닉네임 입력 초안을 최대 20자로 제한해 저장한다. */
    fun onNicknameDraftChange(value: String) {
        val draft = value.take(20)
        updateSuccessState { data ->
            data.copy(
                nicknameDraft = draft,
                nicknameValidationMessage = validateNicknameDraft(draft)
            )
        }
    }

    /** 프로필 이미지를 임시 반영하고 추후 서버 연동 지점을 TODO로 남긴다. */
    fun onProfileImageSelected(imageUri: String) {
        if (imageUri.isBlank()) return

        viewModelScope.launch {
            updateSuccessState { data ->
                data.copy(isProfileImageSaving = true)
            }

            when (val result = userRepository.uploadProfileImage(imageUri)) {
                is BaseResult.Success -> {
                    updateSuccessState { data ->
                        data.copy(
                            profileImage = result.data.profileImage,
                            isProfileImageSaving = false
                        )
                    }
                }
                is BaseResult.Error -> {
                    updateSuccessState { data -> data.copy(isProfileImageSaving = false) }
                    _event.emit(MyPageEvent.ShowMessage(result.error.message))
                }
            }
        }
    }

    fun deleteProfileImage() {
        viewModelScope.launch {
            updateSuccessState { data ->
                data.copy(isProfileImageSaving = true)
            }

            when (val result = userRepository.deleteProfileImage()) {
                is BaseResult.Success -> {
                    updateSuccessState { data ->
                        data.copy(
                            profileImage = result.data.profileImage,
                            isProfileImageSaving = false
                        )
                    }
                }
                is BaseResult.Error -> {
                    updateSuccessState { data -> data.copy(isProfileImageSaving = false) }
                    _event.emit(MyPageEvent.ShowMessage(result.error.message))
                }
            }
        }
    }

    /** 닉네임 저장을 처리하고 추후 서버 동기화 지점을 TODO로 남긴다. */
    fun saveNickname() {
        val currentState = _uiState.value as? UiState.Success ?: return
        val newNickname = currentState.data.nicknameDraft.trim()
        val validationMessage = validateNickname(newNickname)
        if (validationMessage != null) {
            updateSuccessState { data ->
                data.copy(nicknameValidationMessage = validationMessage)
            }
            return
        }

        viewModelScope.launch {
            updateSuccessState { data ->
                data.copy(
                    isNicknameSaving = true,
                    nicknameValidationMessage = null
                )
            }
            when (val result = userRepository.updateMyProfile(nickname = newNickname)) {
                is BaseResult.Success -> {
                    updateSuccessState { data ->
                        data.copy(
                            nickname = result.data.nickname,
                            nicknameDraft = result.data.nickname,
                            profileImage = result.data.profileImage,
                            isNicknameDialogVisible = false,
                            isNicknameSaving = false,
                            nicknameValidationMessage = null
                        )
                    }
                }
                is BaseResult.Error -> {
                    updateSuccessState { data -> data.copy(isNicknameSaving = false) }
                    _event.emit(MyPageEvent.ShowMessage(result.error.message))
                }
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

    private fun observeFavoriteEtfChanges() {
        viewModelScope.launch {
            userRepository.favoriteEtfChanged.collectLatest {
                when (val result = userRepository.getFavoriteEtfs(FavoriteEtfSort.RECENT)) {
                    is BaseResult.Success -> {
                        updateSuccessState { data ->
                            data.copy(likedEtfCount = result.data.totalCount)
                        }
                    }
                    is BaseResult.Error -> Unit
                }
            }
        }
    }

    private fun validateNicknameDraft(nickname: String): String? {
        if (nickname.isEmpty()) return null
        return validateNicknameUseCase(nickname.trim())
    }

    private fun validateNickname(nickname: String): String? = when {
        nickname.isEmpty() -> null
        else -> validateNicknameUseCase(nickname)
    }
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
    val isNicknameSaving: Boolean = false,
    val nicknameValidationMessage: String? = null,
    val isProfileImageSaving: Boolean = false
)

sealed interface MyPageEvent {
    data object LogoutSuccess : MyPageEvent
    data class ShowMessage(val message: String) : MyPageEvent
}
