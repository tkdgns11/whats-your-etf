package com.d102.wye.presentation.auth.join.component

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.d102.wye.presentation.designsystem.WyeEmailTextField
import com.d102.wye.presentation.designsystem.WyePrimaryButton
import com.d102.wye.presentation.designsystem.WyeTextFieldStyle

@Composable
fun ColumnScope.JoinEmailStep(
    email: String,
    canProceed: Boolean,
    onEmailChanged: (String) -> Unit,
    onNextClick: () -> Unit
) {
    JoinStepHeader(title = "가입하실 이메일을\n입력해 주세요.")
    Spacer(modifier = Modifier.height(48.dp))
    WyeEmailTextField(
        value = email,
        onValueChange = onEmailChanged,
        style = WyeTextFieldStyle.Underlined
    )
    Spacer(modifier = Modifier.weight(1f))
    WyePrimaryButton(
        text = "인증번호 발송",
        onClick = onNextClick,
        enabled = canProceed
    )
}
