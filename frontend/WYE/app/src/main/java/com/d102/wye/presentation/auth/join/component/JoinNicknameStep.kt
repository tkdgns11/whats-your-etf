package com.d102.wye.presentation.auth.join.component

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.d102.wye.presentation.designsystem.WyeNicknameTextField
import com.d102.wye.presentation.designsystem.WyePrimaryButton
import com.d102.wye.presentation.designsystem.WyeTextFieldStyle

@Composable
fun ColumnScope.JoinNicknameStep(
    nickname: String,
    canProceed: Boolean,
    onNicknameChanged: (String) -> Unit,
    onNextClick: () -> Unit
) {
    JoinStepHeader(title = "사용하실 닉네임을\n입력해 주세요.")
    Spacer(modifier = Modifier.height(48.dp))
    WyeNicknameTextField(
        value = nickname,
        onValueChange = onNicknameChanged,
        style = WyeTextFieldStyle.Underlined
    )
    Spacer(modifier = Modifier.weight(1f))
    WyePrimaryButton(
        text = "다음",
        onClick = onNextClick,
        enabled = canProceed
    )
}
