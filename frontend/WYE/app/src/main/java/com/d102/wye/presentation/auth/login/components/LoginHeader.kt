package com.d102.wye.presentation.auth.login.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.d102.wye.R
import com.d102.wye.presentation.theme.PrimaryGreen
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(R.drawable.ic_logo),
                contentDescription = "로고"
            )

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "What's Your ETF",
                style = MaterialTheme.typography.titleLarge,
                color = PrimaryGreen,
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

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
