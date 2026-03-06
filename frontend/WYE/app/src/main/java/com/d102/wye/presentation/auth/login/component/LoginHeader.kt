package com.d102.wye.presentation.auth.login.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import com.d102.wye.presentation.theme.SurfaceWhite

@Composable
fun LoginHeader(
    lowerWaveColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
            .background(SurfaceWhite)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .align(Alignment.BottomCenter)
        ) {
            val wave = Path().apply {
                moveTo(0f, size.height * 0.82f)
                cubicTo(
                    size.width * 0.25f,
                    size.height * 0.64f,
                    size.width * 0.45f,
                    size.height * 0.96f,
                    size.width * 0.72f,
                    size.height * 0.80f
                )
                cubicTo(
                    size.width * 0.86f,
                    size.height * 0.70f,
                    size.width,
                    size.height * 0.58f,
                    size.width,
                    size.height * 0.58f
                )
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
            drawPath(wave, color = lowerWaveColor)
        }
    }
}
