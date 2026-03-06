package com.d102.wye.presentation.auth.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.d102.wye.presentation.auth.login.component.LoginFooterLinks
import com.d102.wye.presentation.auth.login.component.LoginFormSection
import com.d102.wye.presentation.auth.login.component.LoginHeader
import com.d102.wye.presentation.auth.login.component.LoginSocialSection
import com.d102.wye.presentation.theme.SurfaceVariant

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onJoinClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.loginEvent.collect { event ->
            when (event) {
                LoginEvent.LoginSuccess -> onLoginSuccess()
            }
        }
    }

    LoginScreenContent(
        uiState = uiState,
        onEmailChanged = { viewModel.onEmailChanged(it) },
        onPasswordChanged = { viewModel.onPasswordChanged(it) },
        onPasswordVisibilityToggle = { viewModel.onPasswordVisibilityToggle() },
        onLoginClick = { viewModel.onLoginClick() },
        onJoinClick = onJoinClick,
        onForgotPasswordClick = onForgotPasswordClick
    )
}

@Composable
private fun LoginScreenContent(
    uiState: LoginUiState,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onPasswordVisibilityToggle: () -> Unit,
    onLoginClick: () -> Unit,
    onJoinClick: () -> Unit,
    onForgotPasswordClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceVariant)
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        LoginHeader(lowerWaveColor = SurfaceVariant)

        Spacer(modifier = Modifier.height(14.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            LoginFormSection(
                uiState = uiState,
                onEmailChanged = onEmailChanged,
                onPasswordChanged = onPasswordChanged,
                onPasswordVisibilityToggle = onPasswordVisibilityToggle,
                onLoginClick = onLoginClick
            )

            Spacer(modifier = Modifier.height(20.dp))

            LoginSocialSection(
                onKakaoLoginClick = {
                    // TODO: 카카오 로그인 SDK 및 백엔드 연동
                }
            )

            Spacer(modifier = Modifier.height(18.dp))

            LoginFooterLinks(
                onJoinClick = onJoinClick,
                onForgotPasswordClick = onForgotPasswordClick
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}