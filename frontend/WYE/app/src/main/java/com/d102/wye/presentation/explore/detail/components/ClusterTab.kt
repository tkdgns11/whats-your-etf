package com.d102.wye.presentation.explore.detail.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.tween
import kotlinx.coroutines.delay
import androidx.compose.foundation.Canvas
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Star
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.zIndex
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.d102.wye.domain.model.EtfDetail
import com.d102.wye.domain.model.EtfSector
import com.d102.wye.domain.model.InfluentialStock
import com.d102.wye.presentation.designsystem.CategoryBadge
import com.d102.wye.presentation.explore.detail.EtfDetailViewModel
import com.d102.wye.presentation.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ClusterTab(
    detail: EtfDetail,
    viewModel: EtfDetailViewModel,
    onStockClick: (String) -> Unit = {},
) {
    var selectedSector by remember { mutableStateOf<EtfSector?>(null) }

    if (selectedSector != null) {
        SectorBottomSheet(
            sector = selectedSector!!,
            onDismiss = { selectedSector = null },
            onStockClick = onStockClick,
        )
    }

    // 화면 높이 기준으로 차트 크기를 자동 결정
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenH = maxHeight

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            // 첫 화면: 클러스터 + 영향종목 제목·첫 항목까지 딱 맞게
            Column(modifier = Modifier.height(screenH)) {
                // 차트 영역 (spacedBy 적용)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    EtfHeader(detail = detail)

                    ClusterBubbleChart(
                        ticker = detail.ticker,
                        sectors = detail.sectors,
                        onSectorClick = { selectedSector = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    )

                    PriceVolumeRow(detail = detail)
                }

                // 영향 종목 peek — spacedBy 밖에 분리하여 다른 종목과 동일한 레이아웃 유지
                HorizontalDivider(color = Divider)
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text(
                        text = "현재 이 ETF에 영향을 많이 끼치는 종목은?",
                        style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                        color = TextPrimary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp, bottom = 4.dp),
                    )
                    if (detail.influentialStocks.isNotEmpty()) {
                        InfluentialStockItem(
                            stock = detail.influentialStocks.first(),
                            onClick = { onStockClick(detail.influentialStocks.first().ticker) },
                        )
                    }
                }
            }

            // 스크롤하면 나타나는 나머지 종목들
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 20.dp),
            ) {
                detail.influentialStocks.drop(1).forEach { stock ->
                    HorizontalDivider(color = Divider)
                    InfluentialStockItem(stock = stock, onClick = { onStockClick(stock.ticker) })
                }
            }
        }
    }
}

@Composable
private fun EtfHeader(detail: EtfDetail) {
    val (badgeBg, badgeFg, badgeLabel) = riskToBadge(detail.riskLevel)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        CategoryBadge(label = badgeLabel, backgroundColor = badgeBg, textColor = badgeFg)
        Text(detail.ticker, style = MaterialTheme.typography.titleLarge.copy(fontSize = 28.sp, fontWeight = FontWeight.ExtraBold), color = TextPrimary)
        Text(detail.englishName, style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp), color = TextSecondary)
    }
}

@Composable
private fun ClusterBubbleChart(
    ticker: String,
    sectors: List<EtfSector>,
    onSectorClick: (EtfSector) -> Unit,
    modifier: Modifier = Modifier,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val centerScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "centerBubble",
    )

    // 무한 펄스 애니메이션
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "pulseProgress",
    )

    val displaySectors = sectors.take(6)
    val angleStep = 360.0 / displaySectors.size.coerceAtLeast(1)

    // 비중 기반 버블 크기 계산
    val maxPct = displaySectors.maxOfOrNull { it.percentage } ?: 1.0
    val minPct = displaySectors.minOfOrNull { it.percentage } ?: 0.0

    BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.Center) {
        // 가용 공간에 비례한 크기 계산
        val availableSize = minOf(maxWidth, maxHeight)
        val orbitRadius = availableSize * 0.40f
        val centerBubbleSize = availableSize * 0.46f
        val minBubbleSize = availableSize * 0.24f
        val maxBubbleSize = availableSize * 0.30f

        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2
            val cy = size.height / 2

            // 펄스 링 (3개 단계 차이)
            val maxRingR = centerBubbleSize.toPx() * 0.67f
            for (i in 0..2) {
                val progress = (pulseProgress + i / 3f) % 1f
                val ringR = maxRingR * progress
                val alpha = (1f - progress) * 0.45f
                drawCircle(
                    color = PrimaryGreen,
                    radius = ringR,
                    center = Offset(cx, cy),
                    alpha = alpha,
                    style = Stroke(width = 2.5f),
                )
            }
        }

        // 중앙 ETF 버블
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .zIndex(10f)
                .size(centerBubbleSize)
                .scale(centerScale)
                .shadow(elevation = 12.dp, shape = CircleShape, clip = false)
                .clip(CircleShape)
                .background(PrimaryGreen),
        ) {
            // "KODEX200" → "KODEX\n200" 처럼 문자/숫자 경계에서 줄바꿈
            val displayTicker = ticker.replace(Regex("(?<=[A-Za-z가-힣])(?=\\d)|(?<=\\d)(?=[A-Za-z가-힣])| "), "\n")
            val tickerFontSize = (centerBubbleSize.value * 0.13f).coerceIn(12f, 18f).sp
            Text(
                text = displayTicker,
                color = Color.White,
                fontSize = tickerFontSize,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                lineHeight = tickerFontSize * 1.4f,
                modifier = Modifier.padding(horizontal = 12.dp),
            )
        }

        // 주변 섹터 버블 (순차 팝-인 + float)
        displaySectors.forEachIndexed { idx, sector ->
            val angleDeg = idx * angleStep - 90.0
            val angleRad = Math.toRadians(angleDeg)
            val x = (orbitRadius.value * cos(angleRad)).dp
            val y = (orbitRadius.value * sin(angleRad)).dp

            val normalized = if (maxPct > minPct) {
                ((sector.percentage - minPct) / (maxPct - minPct)).toFloat()
            } else 0.5f
            val bubbleSize = minBubbleSize + (maxBubbleSize - minBubbleSize) * normalized

            SectorBubble(
                sector = sector,
                index = idx,
                visible = visible,
                bubbleSize = bubbleSize,
                onClick = { onSectorClick(sector) },
                modifier = Modifier.offset(x = x, y = y),
            )
        }
    }
}

@Composable
private fun SectorBubble(
    sector: EtfSector,
    index: Int,
    visible: Boolean,
    bubbleSize: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // 순차 팝-인
    var triggered by remember { mutableStateOf(false) }
    LaunchedEffect(visible) {
        if (visible) {
            delay(index * 60L)
            triggered = true
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (triggered) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "sectorBubble$index",
    )

    // 버블마다 다른 주기/위상으로 위아래 float
    val floatTransition = rememberInfiniteTransition(label = "float$index")
    val floatY by floatTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800 + index * 200),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(index * 300),
        ),
        label = "floatY$index",
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .offset(y = floatY.dp)
            .scale(scale),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.CenterVertically),
            modifier = Modifier
                .size(bubbleSize)
                .shadow(elevation = 6.dp, shape = CircleShape, clip = false)
                .clip(CircleShape)
                .background(Color.White)
                .clickable(onClick = onClick),
        ) {
            val contentWidth = bubbleSize * 0.84f
            val iconSize = bubbleSize * 0.26f
            val nameFontSize = (bubbleSize.value * 0.155f).coerceIn(10f, 14f).sp
            val pctFontSize = (bubbleSize.value * 0.125f).coerceIn(9f, 12f).sp

            Icon(
                imageVector = sectorIcon(sector.name),
                contentDescription = sector.name,
                tint = PrimaryGreen,
                modifier = Modifier.size(iconSize),
            )
            Text(
                text = sector.name,
                fontSize = nameFontSize,
                lineHeight = nameFontSize,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier.width(contentWidth),
            )
            Text(
                text = "${"%.1f".format(sector.percentage)}%",
                fontSize = pctFontSize,
                lineHeight = pctFontSize,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(contentWidth),
            )
        }
    }
}

@Composable
private fun PriceVolumeRow(detail: EtfDetail) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        InfoCard(
            label = "현재 가격",
            value = "%,d원".format(detail.currentPrice),
            valueColor = PrimaryGreen,
            modifier = Modifier.weight(1f),
        )
        InfoCard(
            label = "거래량",
            value = formatVolume(detail.volume),
            valueColor = PrimaryGreen,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun InfoCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = TextPrimary,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(BackGroundLightGreen)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        Text(value, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), color = valueColor)
    }
}

@Composable
private fun InfluentialStocksSection(stocks: List<InfluentialStock>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "현재 이 ETF에 영향을 많이 끼치는 종목은?",
            style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
            color = TextPrimary,
        )
        stocks.forEach { stock ->
            InfluentialStockItem(stock = stock)
            HorizontalDivider(color = Divider)
        }
    }
}

@Composable
private fun InfluentialStockItem(stock: InfluentialStock, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(SurfaceVariant),
        ) {
            Text(stock.name.take(1), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = PrimaryGreen)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(stock.name, style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp, fontWeight = FontWeight.Medium), color = TextPrimary)
            Text("${"%.2f".format(stock.weight)}%", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("%,d원".format(stock.currentPrice), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
            val changeColor = if (stock.changeRate >= 0) EtfRise else EtfFall
            val sign = if (stock.changeRate >= 0) "+" else ""
            Text("$sign${"%.1f".format(stock.changeRate)}%", style = MaterialTheme.typography.bodySmall, color = changeColor)
        }
    }
}

// 섹터명 키워드 → 아이콘 매핑
private fun sectorIcon(name: String): ImageVector = when {
    name.contains("반도체") || name.contains("IT") || name.contains("기술") || name.contains("테크") -> Icons.Filled.Memory
    name.contains("화학") || name.contains("소재") || name.contains("바이오") -> Icons.Filled.Science
    name.contains("금융") || name.contains("은행") || name.contains("보험") -> Icons.Filled.AccountBalance
    name.contains("자동차") || name.contains("운송") || name.contains("모빌리티") -> Icons.Filled.DirectionsCar
    name.contains("서비스") || name.contains("통신") || name.contains("미디어") -> Icons.Filled.Language
    name.contains("의료") || name.contains("헬스") || name.contains("제약") -> Icons.Filled.MedicalServices
    else -> Icons.Filled.Star
}

private fun riskToBadge(level: Int) = when (level) {
    1    -> Triple(BadgeConservative,       BadgeConservativeFont,       "안정형")
    2    -> Triple(BadgeConservativeGrowth, BadgeConservativeGrowthFont, "안정추구형")
    3    -> Triple(BadgeNeutral,            BadgeNeutralFont,            "위험중립형")
    4    -> Triple(BadgeActive,             BadgeActiveFont,             "적극투자형")
    else -> Triple(BadgeAggressive,         BadgeAggressiveFont,         "공격투자형")
}

private fun formatVolume(volume: Long): String = when {
    volume >= 100_000_000 -> "${"%.1f".format(volume / 100_000_000.0)}억"
    volume >= 10_000      -> "${"%.1f".format(volume / 10_000.0)}만"
    else                  -> "%,d".format(volume)
}
