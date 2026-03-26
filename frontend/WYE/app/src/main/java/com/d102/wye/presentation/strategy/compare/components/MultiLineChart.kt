import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val textMeasurer = rememberTextMeasurer()
    val textStyle = MaterialTheme.typography.labelSmall.copy(
        color = Color.Gray.copy(alpha = 0.8f),
        fontSize = 10.sp
    )

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

    val firstDate = validLines.first().recentPoints.first().date
    val lastDate = validLines.first().recentPoints.last().date
    val showDays = firstDate.take(7) == lastDate.take(7) // "YYYY-MM" 비교

    fun String.toDynamicDateLabel(): String {
        val parts = this.split("-")
        if (parts.size >= 3) {
            val yy = parts[0].takeLast(2)
            val mm = parts[1]
            val dd = parts[2]
            return if (showDays) "$yy.$mm.$dd" else "$yy.$mm"
        }
        return this
    }

    Column(modifier = modifier) {
        // 차트 영역 (Canvas)
        Box(modifier = Modifier
            .weight(1f)
            .fillMaxWidth()) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
                    .drawWithCache {
                        val w = size.width
                        val h = size.height

                        // 하단 패딩 삭제! Y축(수익률)을 위한 우측/상하 패딩만 남김
                        val verticalPad = 12.dp.toPx()
                        val rightPad = 40.dp.toPx()

                        val chartW = w - rightPad
                        val chartH = h - (verticalPad * 2)

                        fun xOf(i: Int, sz: Int) = (i.toFloat() / (sz - 1)) * chartW
                        fun yOf(v: Double) =
                            verticalPad + chartH - ((v - minVal) / range * chartH).toFloat()

                        val zeroY = yOf(0.0).coerceIn(verticalPad, h - verticalPad)

                        val linePaths = allPoints.map { points ->
                            Path().apply {
                                moveTo(xOf(0, points.size), yOf(points[0].value))
                                for (i in 1 until points.size) {
                                    lineTo(xOf(i, points.size), yOf(points[i].value))
                                }
                            }
                        }

                        val maxText =
                            textMeasurer.measure("+${String.format("%.1f", maxVal)}%", textStyle)
                        val minText =
                            textMeasurer.measure("${String.format("%.1f", minVal)}%", textStyle)
                        val zeroText =
                            textMeasurer.measure("0%", textStyle.copy(color = Color.Gray))

                        onDrawBehind {
                            // 배경 가이드 라인
                            repeat(3) { i ->
                                val y = verticalPad + chartH * ((i + 1) / 4f)
                                drawLine(
                                    Color.Gray.copy(alpha = 0.1f),
                                    Offset(0f, y),
                                    Offset(chartW, y),
                                    0.5.dp.toPx()
                                )
                            }

                            // 0% 기준선
                            // 2. 0% 기준선 (점선)
                            drawLine(
                                color = Color.Gray.copy(alpha = 0.4f),
                                start = Offset(0f, zeroY),
                                end = Offset(chartW, zeroY),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
                            )

                            // Y축 라벨 그리기 (우측 정렬)
                            val textXOffset = chartW + 6.dp.toPx()
                            drawText(
                                zeroText,
                                topLeft = Offset(textXOffset, zeroY - (zeroText.size.height / 2f))
                            )
                            drawText(
                                maxText,
                                topLeft = Offset(
                                    textXOffset,
                                    verticalPad - (maxText.size.height / 2f)
                                )
                            )
                            drawText(
                                minText,
                                topLeft = Offset(
                                    textXOffset,
                                    verticalPad + chartH - (minText.size.height / 2f)
                                )
                            )

                            clipRect(right = w * animProgress) {
                                linePaths.forEachIndexed { index, path ->
                                    drawPath(
                                        path = path, color = validLines[index].color,
                                        style = Stroke(
                                            width = 1.5.dp.toPx(),
                                            cap = StrokeCap.Round,
                                            join = StrokeJoin.Round,
                                            pathEffect = PathEffect.cornerPathEffect(12.dp.toPx())
                                        )
                                    )
                                }
                            }
                        }
                    }
            ) {}
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, end = 40.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = firstDate.toDynamicDateLabel(), style = textStyle)
            Text(text = lastDate.toDynamicDateLabel(), style = textStyle)
        }
    }
}