package com.d102.wye.presentation.auth.passwordreset

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@HiltViewModel
class PasswordResetViewModel @Inject constructor(
    // TODO: AuthRepository 주입
) : ViewModel() {

    private val _uiState = MutableStateFlow(PasswordResetUiState())
    val uiState: StateFlow<PasswordResetUiState> = _uiState.asStateFlow()

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

    fun onNextClick() {
        val current = _uiState.value
        when (current.currentStep) {
            PasswordResetStep.EMAIL -> {
                if (!EMAIL_REGEX.matches(current.email.trim())) {
                    setError("올바른 이메일 형식을 입력해 주세요.")
                } else {
                    _uiState.update {
                        it.copy(
                            currentStep = PasswordResetStep.VERIFICATION,
                            email = current.email.trim(),
                            helperMessage = "인증번호 재전송",
                            errorMessage = null
                        )
                    }
                    // TODO: 인증번호 발송 API 호출
                }
            }

            PasswordResetStep.VERIFICATION -> {
                if (current.verificationCode.length < 6) {
                    setError("인증번호 6자리를 입력해 주세요.")
                } else {
                    _uiState.update { it.copy(currentStep = PasswordResetStep.NEW_PASSWORD, errorMessage = null) }
                    // TODO: 인증번호 검증 API 호출
                }
            }

            PasswordResetStep.NEW_PASSWORD -> {
                when {
                    current.password.length < 8 -> setError("비밀번호는 8자 이상이어야 합니다.")
                    current.password != current.passwordConfirm -> setError("비밀번호가 일치하지 않습니다.")
                    else -> {
                        _uiState.update { it.copy(currentStep = PasswordResetStep.SUCCESS, errorMessage = null) }
                        // TODO: 비밀번호 변경 API 호출
                    }
                }
            }

            PasswordResetStep.SUCCESS -> Unit
        }
    }

    fun onBackClick() {
        _uiState.update {
            it.copy(
                currentStep = when (it.currentStep) {
                    PasswordResetStep.EMAIL -> PasswordResetStep.EMAIL
                    PasswordResetStep.VERIFICATION -> PasswordResetStep.EMAIL
                    PasswordResetStep.NEW_PASSWORD -> PasswordResetStep.VERIFICATION
                    PasswordResetStep.SUCCESS -> PasswordResetStep.NEW_PASSWORD
                },
                errorMessage = null
            )
        }
    }

    fun onResendCodeClick() {
        // TODO: 인증번호 재발송 API 호출
    }

    private fun setError(message: String) {
        _uiState.update { it.copy(errorMessage = message) }
    }

    companion object {
        private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
    }
}


