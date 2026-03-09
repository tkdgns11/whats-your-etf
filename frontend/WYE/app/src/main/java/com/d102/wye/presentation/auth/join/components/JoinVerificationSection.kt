package com.d102.wye.presentation.auth.join.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.d102.wye.presentation.designsystem.WyeTextButton
import com.d102.wye.presentation.theme.TextSecondary

@Composable
fun JoinVerificationSection(
    helperText: String?,
    onResendClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        if (helperText != null) {
            WyeTextButton(
                text = helperText,
                onClick = onResendClick
            )
        } else {
            Text(
                text = "",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}
