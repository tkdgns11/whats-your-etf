import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.unit.dp
import com.d102.wye.presentation.strategy.compare.CompareChartLine
import kotlin.math.abs

@Composable
fun MultiLineChart(
    lines: List<CompareChartLine>,
    modifier: Modifier = Modifier
) {
    val validLines = lines.filter { it.recentPoints.size >= 2 }
    if (validLines.isEmpty()) return

    val allPoints = validLines.map { it.recentPoints }
    val progress = remember(validLines) { Animatable(0f) }

    LaunchedEffect(validLines) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing)
        )
    }
    val animProgress by progress.asState()

    val allValues = allPoints.flatten().map { it.value }
    val minVal = allValues.minOrNull() ?: 0.0
    val maxVal = allValues.maxOrNull() ?: 1.0
    val range = if (abs(maxVal - minVal) < 0.001) 1.0 else maxVal - minVal

    Canvas(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .drawWithCache {
                val w = size.width
                val h = size.height
                val pad = 12.dp.toPx()
                val chartH = h - pad * 2

                fun xOf(i: Int, sz: Int) = (i.toFloat() / (sz - 1)) * w
                fun yOf(v: Double) = pad + chartH - ((v - minVal) / range * chartH).toFloat()

                val zeroY = yOf(0.0).coerceIn(pad, h - pad)

                val linePaths = allPoints.map { points ->
                    Path().apply {
                        moveTo(xOf(0, points.size), yOf(points[0].value))
                        for (i in 1 until points.size) {
                            lineTo(xOf(i, points.size), yOf(points[i].value))
                        }
                    }
                }

                onDrawBehind {
                    // 배경 가이드 라인
                    repeat(3) { i ->
                        val y = pad + chartH * ((i + 1) / 4f)
                        drawLine(
                            Color.Gray.copy(alpha = 0.1f),
                            Offset(0f, y),
                            Offset(w, y),
                            0.5.dp.toPx()
                        )
                    }

                    // 0점 기준선
                    drawLine(
                        Color.Gray.copy(alpha = 0.35f), Offset(0f, zeroY), Offset(w, zeroY),
                        1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
                    )

                    // 애니메이션 마스킹
                    clipRect(right = w * animProgress) {
                        linePaths.forEachIndexed { index, path ->
                            drawPath(
                                path = path,
                                color = validLines[index].color,
                                style = Stroke(
                                    width = 1.5.dp.toPx(),
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round, // 꺾이는 부분을 둥글게 처리
                                    pathEffect = PathEffect.cornerPathEffect(12.dp.toPx())
                                )
                            )
                        }
                    }
                }
            }
    ) {}
}