package com.d102.wye.presentation.auth.join

enum class JoinStep {
    NICKNAME,
    EMAIL,
    VERIFICATION,
    PASSWORD,
    SUCCESS
}

data class JoinUiState(
    val currentStep: JoinStep = JoinStep.NICKNAME,
    val nickname: String = "",
    val email: String = "",
    val verificationCode: String = "",
    val password: String = "",
    val passwordConfirm: String = "",
    val isPasswordVisible: Boolean = false,
    val isPasswordConfirmVisible: Boolean = false,
    val helperMessage: String? = null,
    val errorMessage: String? = null,
    val isLoading: Boolean = false
) {
    val canProceed: Boolean
        get() = when (currentStep) {
            JoinStep.NICKNAME -> nickname.isNotBlank()
            JoinStep.EMAIL -> email.isNotBlank()
            JoinStep.VERIFICATION -> verificationCode.length >= 6
            JoinStep.PASSWORD -> password.isNotBlank() && passwordConfirm.isNotBlank()
            JoinStep.SUCCESS -> true
        } && !isLoading
}
