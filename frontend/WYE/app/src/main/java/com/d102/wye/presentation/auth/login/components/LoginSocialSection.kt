package com.d102.wye.presentation.auth.login.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.d102.wye.presentation.designsystem.WyeKakaoButton
import com.d102.wye.presentation.theme.Divider
import com.d102.wye.presentation.theme.TextTertiary

@Composable
fun LoginSocialSection(
    onKakaoLoginClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        DividerLine(modifier = Modifier.weight(1f))
        Text(
            text = "또는",
            color = TextTertiary,
            modifier = Modifier.padding(horizontal = 16.dp),
            style = MaterialTheme.typography.labelLarge
        )
        DividerLine(modifier = Modifier.weight(1f))
    }

    Spacer(modifier = Modifier.height(20.dp))

    WyeKakaoButton(
        onClick = onKakaoLoginClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
    )
}

@Composable
private fun DividerLine(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(1.dp)
            .background(Divider)
    )
}
