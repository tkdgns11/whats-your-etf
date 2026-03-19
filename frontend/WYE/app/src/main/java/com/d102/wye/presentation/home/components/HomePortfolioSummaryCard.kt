package com.d102.wye.presentation.home.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.d102.wye.domain.model.BacktestPoint
import com.d102.wye.presentation.home.PortfolioSummaryUiModel
import com.d102.wye.presentation.theme.BackGroundLightGreen
import com.d102.wye.presentation.theme.Border
import com.d102.wye.presentation.theme.EtfFall
import com.d102.wye.presentation.theme.EtfRise
import com.d102.wye.presentation.theme.PrimaryGreen
import com.d102.wye.presentation.theme.SurfaceWhite
import com.d102.wye.presentation.theme.TextPrimary
import com.d102.wye.presentation.theme.TextSecondary
import kotlin.math.abs

@Composable
fun HomePortfolioSummaryCard(
    modifier: Modifier = Modifier,
    portfolios: List<PortfolioSummaryUiModel>
) {
    if (portfolios.isEmpty()) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "포트폴리오를 생성하면 수익률을 확인할 수 있습니다.",
                    color = TextSecondary
                )
            }
        }
        return
    }

    val pagerState = rememberPagerState(pageCount = { portfolios.size })

    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { page ->
            PortfolioSummaryPage(
                modifier = Modifier.fillMaxSize(),
                portfolio = portfolios[page]
            )
        }

        if (portfolios.size > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(portfolios.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (isSelected) 8.dp else 6.dp)
                            .clip(RoundedCornerShape(50))
                            .background(if (isSelected) PrimaryGreen else Border)
                    )
                }
            }
        }
    }
}

@Composable
private fun PortfolioSummaryPage(
    portfolio: PortfolioSummaryUiModel,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = portfolio.name,
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = portfolio.totalAmount,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = "${portfolio.profitRateText} ${portfolio.profitAmountText}",
                color = if (portfolio.isPositive) EtfRise else EtfFall,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(top = 6.dp)
            )
            Text(
                text = "과거 1년부터 현재까지의 수익률 추이",
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 10.dp)
            )
            HomePortfolioChartLegend(
                isPositive = portfolio.isPositive,
                modifier = Modifier.padding(top = 8.dp)
            )

            if (portfolio.chartPoints.size >= 2) {
                Box(
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .fillMaxWidth()
                        .background(BackGroundLightGreen, RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 16.dp)
                ) {
                    HomePortfolioChart(
                        points = portfolio.chartPoints,
                        pastPointCount = portfolio.pastPointCount,
                        isPositive = portfolio.isPositive,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                PortfolioChartPlaceholder(modifier = Modifier.padding(top = 12.dp))
            }
        }
    }
}

@Composable
private fun HomePortfolioChartLegend(
    isPositive: Boolean,
    modifier: Modifier = Modifier
) {
    val recentColor = if (isPositive) EtfRise else EtfFall

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LegendItem(color = TextSecondary, label = "과거 1년")
        LegendItem(color = recentColor, label = "저장 시점부터 현재")
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(width = 18.dp, height = 3.dp)
                .clip(RoundedCornerShape(50))
                .background(color)
        )
        Text(
            text = label,
            color = TextSecondary,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun HomePortfolioChart(
    points: List<BacktestPoint>,
    pastPointCount: Int,
    isPositive: Boolean,
    modifier: Modifier = Modifier,
) {
    if (points.size < 2) return

    val values = remember(points) { points.map { it.value } }
    val minVal = values.min()
    val maxVal = values.max()
    val range = if (abs(maxVal - minVal) < 0.001) 1.0 else maxVal - minVal
    val splitIndex = (pastPointCount - 1).coerceIn(0, points.lastIndex)
    val recentColor = if (isPositive) EtfRise else EtfFall

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            fun xOf(index: Int): Float =
                if (points.size == 1) 0f else size.width * index / (points.size - 1).toFloat()

            fun yOf(value: Double): Float =
                size.height - ((value - minVal) / range * size.height).toFloat()

            repeat(3) { i ->
                val y = size.height * ((i + 1) / 4f)
                drawLine(
                    color = Color.Gray.copy(alpha = 0.12f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            val pastPath = Path().apply {
                moveTo(xOf(0), yOf(points[0].value))
                for (i in 1..splitIndex) {
                    lineTo(xOf(i), yOf(points[i].value))
                }
            }

            drawPath(
                path = pastPath,
                color = TextSecondary,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )

            if (splitIndex < points.lastIndex) {
                val recentPath = Path().apply {
                    moveTo(xOf(splitIndex), yOf(points[splitIndex].value))
                    for (i in (splitIndex + 1)..points.lastIndex) {
                        lineTo(xOf(i), yOf(points[i].value))
                    }
                }

                drawPath(
                    path = recentPath,
                    color = recentColor,
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf(
                points.first().date.toDateLabel(),
                points[splitIndex].date.toDateLabel(),
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

private fun String.toDateLabel(): String {
    val parts = split("-")
    return if (parts.size >= 2) "${parts[0].takeLast(2)}.${parts[1]}" else this
}

@Composable
private fun PortfolioChartPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(270.dp)
            .background(BackGroundLightGreen, RoundedCornerShape(20.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "차트 데이터가 없습니다.",
                color = TextSecondary,
            )
        }
    }
}
