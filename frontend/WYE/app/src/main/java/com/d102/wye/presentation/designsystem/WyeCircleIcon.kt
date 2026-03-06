package com.d102.wye.presentation.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.d102.wye.presentation.theme.PrimaryGreen

@Composable
fun WyeCircleIcon(tag: String, iconRes: Int? = null) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(PrimaryGreen),
        contentAlignment = Alignment.Center
    ) {
        if (iconRes == null) {
            Text(
                text = tag.take(1),
                color = Color.White,
                style = MaterialTheme.typography.titleSmall,
            )
        } else {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null
            )
        }
    }
}