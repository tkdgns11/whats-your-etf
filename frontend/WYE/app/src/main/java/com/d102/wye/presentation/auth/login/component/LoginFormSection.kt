package com.d102.wye.presentation.auth.login.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.d102.wye.presentation.auth.login.LoginUiState
import com.d102.wye.presentation.designsystem.WyePrimaryButton
import com.d102.wye.presentation.designsystem.WyeEmailTextField
import com.d102.wye.presentation.designsystem.WyePasswordTextField
import com.d102.wye.presentation.theme.TextOnColored

@Composable
fun LoginFormSection(
    uiState: LoginUiState,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onPasswordVisibilityToggle: () -> Unit,
    onLoginClick: () -> Unit
) {
    WyeEmailTextField(
        value = uiState.email,
        onValueChange = onEmailChanged,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
    )

    Spacer(modifier = Modifier.height(16.dp))

    WyePasswordTextField(
        value = uiState.password,
        onValueChange = onPasswordChanged,
        isVisible = uiState.isPasswordVisible,
        onVisibilityToggle = onPasswordVisibilityToggle,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
    )

    uiState.errorMessage?.let { errorMessage ->
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.fillMaxWidth()
        )
    }

    Spacer(modifier = Modifier.height(14.dp))

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        WyePrimaryButton(
            text = if (uiState.isLoading) "" else "로그인",
            onClick = onLoginClick,
            enabled = uiState.canLogin && !uiState.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
        )

        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = TextOnColored
            )
        }
    }
}
