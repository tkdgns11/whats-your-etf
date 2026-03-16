package com.d102.wye.presentation.auth.join

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d102.wye.domain.common.BaseResult
import com.d102.wye.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class JoinViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(JoinUiState())
    val uiState: StateFlow<JoinUiState> = _uiState.asStateFlow()

    fun onNicknameChanged(value: String) {
        _uiState.update { it.copy(nickname = value, errorMessage = null) }
    }

    fun onEmailChanged(value: String) {
        _uiState.update { it.copy(email = value, errorMessage = null) }
    }

    fun onVerificationCodeChanged(value: String) {
        _uiState.update {
            it.copy(
                verificationCode = value.take(6),
                errorMessage = null
            )
        }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = null) }
    }

    fun onPasswordConfirmChanged(value: String) {
        _uiState.update { it.copy(passwordConfirm = value, errorMessage = null) }
    }

    fun onPasswordVisibilityToggle() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun onPasswordConfirmVisibilityToggle() {
        _uiState.update { it.copy(isPasswordConfirmVisible = !it.isPasswordConfirmVisible) }
    }

    /** 현재 단계 입력값을 검증하고 다음 단계 액션을 실행한다. */
    fun onNextClick() {
        val current = _uiState.value
        when (current.currentStep) {
            JoinStep.NICKNAME -> {
                val nickname = current.nickname.trim()
                // 인증 문서 기준: 닉네임은 2~20자, 한글/영문/숫자만 허용한다.
                when {
                    nickname.isBlank() -> setError("닉네임을 입력해 주세요.")
                    nickname.length !in 2..20 -> setError("닉네임은 2자 이상 20자 이하로 입력해 주세요.")
                    !NICKNAME_REGEX.matches(nickname) -> setError("닉네임은 한글, 영문, 숫자만 사용할 수 있습니다.")
                    else -> {
                        _uiState.update {
                            it.copy(
                                currentStep = JoinStep.EMAIL,
                                nickname = nickname,
                                errorMessage = null
                            )
                        }
                    }
                }
            }

            JoinStep.EMAIL -> {
                if (!EMAIL_REGEX.matches(current.email.trim())) {
                    setError("올바른 이메일 형식을 입력해 주세요.")
                } else {
                    _uiState.update { it.copy(currentStep = JoinStep.PASSWORD, email = current.email.trim(), errorMessage = null) }
                }
            }

            JoinStep.PASSWORD -> {
                when {
                    current.password.length < 8 -> setError("비밀번호는 8자 이상이어야 합니다.")
                    !PASSWORD_REGEX.matches(current.password) -> setError("비밀번호는 영문, 숫자, 특수문자를 모두 포함해야 합니다.")
                    current.password != current.passwordConfirm -> setError("비밀번호가 일치하지 않습니다.")
                    else -> requestSignup()
                }
            }

            JoinStep.VERIFICATION -> {
                if (current.verificationCode.length < 6) {
                    setError("인증번호 6자리를 입력해 주세요.")
                } else {
                    verifySignupCode()
                }
            }

            JoinStep.SUCCESS -> {
                // TODO: 로그인 화면 혹은 메인 화면 이동 이벤트 추가
            }
        }
    }

    fun onBackClick() {
        _uiState.update {
            it.copy(
                currentStep = when (it.currentStep) {
                    JoinStep.NICKNAME -> JoinStep.NICKNAME
                    JoinStep.EMAIL -> JoinStep.NICKNAME
                    JoinStep.PASSWORD -> JoinStep.EMAIL
                    JoinStep.VERIFICATION -> JoinStep.PASSWORD
                    JoinStep.SUCCESS -> JoinStep.VERIFICATION
                },
                pendingTokenPair = if (it.currentStep == JoinStep.SUCCESS) null else it.pendingTokenPair,
                errorMessage = null
            )
        }
    }

    /** 인증 코드를 다시 요청한다. */
    fun onResendCodeClick() {
        val email = _uiState.value.email.trim()
        if (email.isBlank()) {
            setError("이메일을 다시 입력해 주세요.")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = authRepository.resendSignupCode(email)) {
                is BaseResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            helperMessage = "인증번호를 다시 보냈습니다.",
                            errorMessage = null
                        )
                    }
                }
                is BaseResult.Error -> setError(result.error.message)
            }
        }
    }

    /** 성공 화면에서 시작하기를 누르면 토큰 저장을 확정한다. */
    fun onStartClick() {
        val tokenPair = _uiState.value.pendingTokenPair
        if (tokenPair == null) {
            setError("회원가입 정보를 다시 확인해 주세요.")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            authRepository.saveAuthTokens(tokenPair)
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    /** 화면에 표시할 에러 메시지를 저장하고 로딩을 해제한다. */
    private fun setError(message: String) {
        _uiState.update { it.copy(isLoading = false, errorMessage = message) }
    }

    /** 입력한 닉네임/이메일/비밀번호로 회원가입을 요청한다. */
    private fun requestSignup() {
        val current = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (
                val result = authRepository.signup(
                    email = current.email.trim(),
                    password = current.password,
                    passwordConfirm = current.passwordConfirm,
                    nickname = current.nickname.trim()
                )
            ) {
                is BaseResult.Success -> {
                    _uiState.update {
                        it.copy(
                            currentStep = JoinStep.VERIFICATION,
                            isLoading = false,
                            helperMessage = "인증번호 재전송",
                            errorMessage = null
                        )
                    }
                }
                is BaseResult.Error -> setError(result.error.message)
            }
        }
    }

    /** 입력한 인증번호를 검증하고 성공 시 완료 단계로 전환한다. */
    private fun verifySignupCode() {
        val current = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (
                val result = authRepository.verifySignup(
                    email = current.email.trim(),
                    token = current.verificationCode
                )
            ) {
                is BaseResult.Success -> {
                    _uiState.update {
                        it.copy(
                            currentStep = JoinStep.SUCCESS,
                            pendingTokenPair = result.data,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
                is BaseResult.Error -> setError(result.error.message)
            }
        }
    }

    companion object {
        private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
        private val NICKNAME_REGEX = Regex("^[가-힣A-Za-z0-9]+$")
        private val PASSWORD_REGEX =
            Regex("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,72}$")
    }
}
