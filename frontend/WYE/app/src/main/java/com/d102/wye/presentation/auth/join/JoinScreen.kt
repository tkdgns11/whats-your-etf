package com.d102.wye.presentation.auth.join

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.d102.wye.presentation.auth.join.component.JoinEmailStep
import com.d102.wye.presentation.auth.join.component.JoinNicknameStep
import com.d102.wye.presentation.auth.join.component.JoinPasswordStep
import com.d102.wye.presentation.auth.join.component.JoinSuccessContent
import com.d102.wye.presentation.auth.join.component.JoinVerificationStep
import com.d102.wye.presentation.designsystem.WyePrimaryButton
import com.d102.wye.presentation.designsystem.WyeTopBar
import com.d102.wye.presentation.theme.WYETheme

@Composable
fun JoinScreen(
    onBackClick: () -> Unit,
    onStartClick: () -> Unit,
    viewModel: JoinViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    JoinScreenContent(
        uiState = uiState,
        onBackClick = {
            if (uiState.currentStep == JoinStep.NICKNAME) onBackClick() else viewModel.onBackClick()
        },
        onStartClick = onStartClick,
        onNicknameChanged = viewModel::onNicknameChanged,
        onEmailChanged = viewModel::onEmailChanged,
        onVerificationCodeChanged = viewModel::onVerificationCodeChanged,
        onPasswordChanged = viewModel::onPasswordChanged,
        onPasswordConfirmChanged = viewModel::onPasswordConfirmChanged,
        onPasswordVisibilityToggle = viewModel::onPasswordVisibilityToggle,
        onPasswordConfirmVisibilityToggle = viewModel::onPasswordConfirmVisibilityToggle,
        onNextClick = viewModel::onNextClick,
        onResendCodeClick = viewModel::onResendCodeClick
    )
}

@Composable
private fun JoinScreenContent(
    uiState: JoinUiState,
    onBackClick: () -> Unit,
    onStartClick: () -> Unit,
    onNicknameChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onVerificationCodeChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onPasswordConfirmChanged: (String) -> Unit,
    onPasswordVisibilityToggle: () -> Unit,
    onPasswordConfirmVisibilityToggle: () -> Unit,
    onNextClick: () -> Unit,
    onResendCodeClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (uiState.currentStep != JoinStep.SUCCESS) {
            WyeTopBar(
                title = "",
                onBackClick = onBackClick
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            when (uiState.currentStep) {
                JoinStep.NICKNAME -> JoinNicknameStep(
                    nickname = uiState.nickname,
                    canProceed = uiState.canProceed,
                    onNicknameChanged = onNicknameChanged,
                    onNextClick = onNextClick
                )

                JoinStep.EMAIL -> JoinEmailStep(
                    email = uiState.email,
                    canProceed = uiState.canProceed,
                    onEmailChanged = onEmailChanged,
                    onNextClick = onNextClick
                )

                JoinStep.VERIFICATION -> JoinVerificationStep(
                    verificationCode = uiState.verificationCode,
                    helperMessage = uiState.helperMessage,
                    canProceed = uiState.canProceed,
                    onVerificationCodeChanged = onVerificationCodeChanged,
                    onResendCodeClick = onResendCodeClick,
                    onNextClick = onNextClick
                )

                JoinStep.PASSWORD -> JoinPasswordStep(
                    email = uiState.email,
                    password = uiState.password,
                    passwordConfirm = uiState.passwordConfirm,
                    isPasswordVisible = uiState.isPasswordVisible,
                    isPasswordConfirmVisible = uiState.isPasswordConfirmVisible,
                    errorMessage = uiState.errorMessage,
                    canProceed = uiState.canProceed,
                    onPasswordChanged = onPasswordChanged,
                    onPasswordConfirmChanged = onPasswordConfirmChanged,
                    onPasswordVisibilityToggle = onPasswordVisibilityToggle,
                    onPasswordConfirmVisibilityToggle = onPasswordConfirmVisibilityToggle,
                    onNextClick = onNextClick
                )

                JoinStep.SUCCESS -> {
                    Spacer(modifier = Modifier.height(48.dp))
                    JoinSuccessContent(email = uiState.email)
                    Spacer(modifier = Modifier.weight(1f))
                    WyePrimaryButton(
                        text = "시작하기",
                        onClick = onStartClick
                    )
                }
            }
        }
    }
}
