package com.d102.wye.presentation.simulation.progress.result

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.d102.wye.domain.model.BacktestPoint
import com.d102.wye.domain.state.InvestmentType
import com.d102.wye.presentation.theme.PrimaryGreen
import com.d102.wye.presentation.theme.TextPrimary
import com.d102.wye.presentation.theme.TextSecondary
import kotlin.math.abs

/**
 * 백테스트 라인 차트
 *
 * - 적립형: y축 = 월말 평가금액 (원), 월별 포인트
 * - 거치형: y축 = 일별 평가금액 (원), 일별 포인트
 * - 좌 → 우 순차 드로우 애니메이션
 *
 * - isDashed = false (기본): 실선 + 컬러 (시뮬레이션 / 최근 성과)
 * - isDashed = true: 점선 + 회색 (과거 1년 성과)
 */
@Composable
fun BacktestChart(
    points: List<BacktestPoint>,
    investmentType: InvestmentType,
    isPositive: Boolean,
    modifier: Modifier = Modifier,
    isDashed: Boolean = false
) {
    if (points.size < 2) return

    val progress = remember(points) { Animatable(0f) }
    LaunchedEffect(points) {
        progress.snapTo(0f)
        progress.animateTo(1f, animationSpec = tween(durationMillis = 1200))
    }
    val animProgress by progress.asState()

    // 점선이면 회색, 아니면 수익률에 따라 색상
    val lineColor = when {
        isDashed -> TextSecondary
        isPositive -> PrimaryGreen
        else -> TextPrimary
    }

    val values = points.map { it.value }
    val minVal = values.min()
    val maxVal = values.max()
    val range = if (abs(maxVal - minVal) < 0.001) 1.0 else maxVal - minVal

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .padding(horizontal = 4.dp)
        ) {
            val w = size.width
            val h = size.height
            val drawCount = (points.size * animProgress).toInt().coerceAtLeast(2)

            fun xOf(i: Int) = (i.toFloat() / (points.size - 1)) * w
            fun yOf(v: Double) = h - ((v - minVal) / range * h).toFloat()

            // 수평 그리드
            repeat(3) { i ->
                val y = h * ((i + 1) / 4f)
                drawLine(
                    color = Color.Gray.copy(alpha = 0.1f),
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = 0.5.dp.toPx()
                )
            }

            // 그라디언트 채우기 (실선일 때만)
            if (!isDashed) {
                val fillPath = Path().apply {
                    moveTo(xOf(0), h)
                    lineTo(xOf(0), yOf(values[0]))
                    for (i in 1 until drawCount) {
                        val x0 = xOf(i - 1);
                        val y0 = yOf(values[i - 1])
                        val x1 = xOf(i);
                        val y1 = yOf(values[i])
                        val cx = (x0 + x1) / 2f
                        cubicTo(cx, y0, cx, y1, x1, y1)
                    }
                    lineTo(xOf(drawCount - 1), h)
                    close()
                }
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(lineColor.copy(alpha = 0.25f), Color.Transparent),
                        startY = 0f, endY = h
                    )
                )
            }

            // 라인 (점선 여부에 따라 pathEffect 적용)
            val linePath = Path().apply {
                moveTo(xOf(0), yOf(values[0]))
                for (i in 1 until drawCount) {
                    val x0 = xOf(i - 1);
                    val y0 = yOf(values[i - 1])
                    val x1 = xOf(i);
                    val y1 = yOf(values[i])
                    val cx = (x0 + x1) / 2f
                    cubicTo(cx, y0, cx, y1, x1, y1)
                }
            }
            drawPath(
                path = linePath,
                color = lineColor,
                style = Stroke(
                    width = 2.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            )

            // 마지막 포인트 점 (실선일 때만)
            if (!isDashed && animProgress >= 0.99f) {
                val lastX = xOf(drawCount - 1)
                val lastY = yOf(values[drawCount - 1])
                drawCircle(color = lineColor, radius = 4.dp.toPx(), center = Offset(lastX, lastY))
                drawCircle(color = Color.White, radius = 2.dp.toPx(), center = Offset(lastX, lastY))
            }
        }

        // 기간 레이블
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf(
                points.first().date.toDateLabel(),
                points[points.size / 2].date.toDateLabel(),
                points.last().date.toDateLabel()
            ).forEach { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = TextPrimary
                )
            }
        }
    }
}

/** "2026-01-15" → "26.01" */
private fun String.toDateLabel(): String {
    val parts = split("-")
    return if (parts.size >= 2) "${parts[0].takeLast(2)}.${parts[1]}" else this
}