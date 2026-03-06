package com.d102.wye.presentation.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class LoginViewModel @Inject constructor(
    // TODO: AuthRepository 주입
    // private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _loginEvent = MutableSharedFlow<LoginEvent>()
    val loginEvent: SharedFlow<LoginEvent> = _loginEvent

    fun onEmailChanged(email: String) {
        _uiState.update {
            it.copy(
                email = email,
                errorMessage = null
            )
        }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update {
            it.copy(
                password = password,
                errorMessage = null
            )
        }
    }

    fun onPasswordVisibilityToggle() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun onLoginClick() {
        val currentState = _uiState.value
        val email = currentState.email.trim()
        val password = currentState.password

        when {
            email.isBlank() -> setError("이메일을 입력해 주세요.")
            !EMAIL_REGEX.matches(email) -> setError("올바른 이메일 형식을 입력해 주세요.")
            password.isBlank() -> setError("비밀번호를 입력해 주세요.")
            else -> {
                _uiState.update {
                    it.copy(
                        email = email,
                        isLoading = true,
                        errorMessage = null
                    )
                }

                // TODO: authRepository.login(email, password) 호출
                // TODO: 로그인 성공 시 토큰 저장
                // TODO: 서버 에러를 사용자 메시지로 매핑
                // TODO: 실제 로그인 성공 시 emitLoginSuccess() 호출
            }
        }
    }

    private fun setError(message: String) {
        _uiState.update { it.copy(isLoading = false, errorMessage = message) }
    }

    private fun emitLoginSuccess() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = false) }
            _loginEvent.emit(LoginEvent.LoginSuccess)
        }
    }

    companion object {
        private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
    }
}

sealed interface LoginEvent {
    data object LoginSuccess : LoginEvent
}
