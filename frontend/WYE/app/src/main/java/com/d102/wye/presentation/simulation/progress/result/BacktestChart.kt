package com.d102.wye.presentation.simulation.progress.result

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.d102.wye.domain.model.BacktestPoint
import com.d102.wye.domain.state.InvestmentType
import com.d102.wye.presentation.theme.PrimaryGreen
import com.d102.wye.presentation.theme.TextSecondary
import kotlin.math.abs

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
        progress.animateTo(1f, animationSpec = tween(durationMillis = 1000))
    }
    val animProgress by progress.asState()

    val lineColor = when {
        isDashed   -> Color(0xFFB0B0B0)
        isPositive -> PrimaryGreen
        else       -> Color(0xFF1565C0)
    }

    val values = remember(points) { points.map { it.value } }
    val minVal = remember(values) { values.min() }
    val maxVal = remember(values) { values.max() }
    val range = remember(minVal, maxVal) {
        if (abs(maxVal - minVal) < 0.001) 1.0 else maxVal - minVal
    }

    Column(modifier = modifier) {
        androidx.compose.foundation.Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .padding(horizontal = 4.dp)
                .drawWithCache {
                    val w = size.width
                    val h = size.height

                    fun xOf(i: Int) = (i.toFloat() / (values.size - 1)) * w
                    fun yOf(v: Double) = h - ((v - minVal) / range * h).toFloat()

                    // Path를 여기서 한 번만 계산 → 사이즈 바뀔 때만 재계산
                    val linePath = Path().apply {
                        moveTo(xOf(0), yOf(values[0]))
                        for (i in 1 until values.size) {
                            val x0 = xOf(i - 1); val y0 = yOf(values[i - 1])
                            val x1 = xOf(i);     val y1 = yOf(values[i])
                            cubicTo((x0 + x1) / 2f, y0, (x0 + x1) / 2f, y1, x1, y1)
                        }
                    }

                    val fillPath = Path().apply {
                        moveTo(xOf(0), h)
                        lineTo(xOf(0), yOf(values[0]))
                        for (i in 1 until values.size) {
                            val x0 = xOf(i - 1); val y0 = yOf(values[i - 1])
                            val x1 = xOf(i);     val y1 = yOf(values[i])
                            cubicTo((x0 + x1) / 2f, y0, (x0 + x1) / 2f, y1, x1, y1)
                        }
                        lineTo(xOf(values.size - 1), h)
                        close()
                    }

                    onDrawBehind {
                        // 그리드
                        repeat(3) { i ->
                            val y = h * ((i + 1) / 4f)
                            drawLine(
                                color = Color.Gray.copy(alpha = 0.1f),
                                start = Offset(0f, y),
                                end = Offset(w, y),
                                strokeWidth = 0.5.dp.toPx()
                            )
                        }

                        // clipRect으로 애니메이션 (Path 재계산 없음!)
                        clipRect(right = w * animProgress) {
                            if (!isDashed) {
                                drawPath(
                                    path = fillPath,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(lineColor.copy(alpha = 0.25f), Color.Transparent),
                                        startY = 0f, endY = h
                                    )
                                )
                            }
                            drawPath(
                                path = linePath,
                                color = lineColor,
                                style = Stroke(
                                    width = 2.dp.toPx(),
                                    cap = StrokeCap.Round,
                                    pathEffect = if (isDashed)
                                        PathEffect.dashPathEffect(floatArrayOf(12f, 8f))
                                    else null
                                )
                            )
                        }

                        // 마지막 점 (실선 + 완료 시)
                        if (!isDashed && animProgress >= 0.99f) {
                            val lastX = (values.size - 1).toFloat() / (values.size - 1) * w
                            val lastY = h - ((values.last() - minVal) / range * h).toFloat()
                            drawCircle(color = lineColor, radius = 4.dp.toPx(), center = Offset(lastX, lastY))
                            drawCircle(color = Color.White, radius = 2.dp.toPx(), center = Offset(lastX, lastY))
                        }
                    }
                }
        ) {}

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, start = 4.dp, end = 4.dp),
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
                    color = TextSecondary
                )
            }
        }
    }
}

private fun String.toDateLabel(): String {
    val parts = split("-")
    return if (parts.size >= 2) "${parts[0].takeLast(2)}.${parts[1]}" else this
}