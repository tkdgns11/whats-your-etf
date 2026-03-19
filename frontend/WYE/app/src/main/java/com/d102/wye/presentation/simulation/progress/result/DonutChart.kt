import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.d102.wye.presentation.theme.IconInactive
import com.d102.wye.presentation.theme.SurfaceVariant
import com.d102.wye.presentation.theme.TextPrimary

@Composable
fun DonutChart(
    items: List<SectorWeight>,
    modifier: Modifier = Modifier,
    chartSize: Dp = 130.dp,
    strokeWidth: Dp = 16.dp
) {
    val total = items.sumOf { it.ratio.toDouble() }.toFloat()
    val isEmpty = items.isEmpty() || total == 0f

    // 차트와 오버레이를 모두 감싸는 최상위 Box
    Box(
        modifier = modifier.size(chartSize),
        contentAlignment = Alignment.Center
    ) {
        // 1. 차트 그리기 (Canvas)
        Canvas(modifier = Modifier.size(chartSize)) {
            if (isEmpty) {
                // 데이터가 없을 때: 100%짜리 연한 회색 원
                drawArc(
                    color = SurfaceVariant,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Butt)
                )
            } else {
                // 데이터가 있을 때: 기존 로직대로 섹터별로 그림
                var startAngle = -90f // 12시 방향부터 시작
                items.forEach { item ->
                    val sweepAngle = (item.ratio / total) * 360f
                    drawArc(
                        color = item.color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Butt)
                    )
                    startAngle += sweepAngle
                }
            }
        }

        // 2. 데이터 유무에 따른 가운데 콘텐츠 처리
        if (isEmpty) {
            Text(
                text = "ETF를 추가하고 섹터 비중을 확인해보세요.",
                style = MaterialTheme.typography.bodyMedium,
                color = IconInactive,
                textAlign = TextAlign.Center,
                modifier = Modifier.wrapContentWidth(unbounded = true)
            )
        } else {
            // 데이터가 있을 때: 기존의 100% 텍스트
            Text(
                text = "100%",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
        }
    }
}

data class SectorWeight(
    val name: String,
    val ratio: Float,  // 비중 (%)
    val color: Color
)