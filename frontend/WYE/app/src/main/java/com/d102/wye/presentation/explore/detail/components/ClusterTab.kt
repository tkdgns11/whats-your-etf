package com.d102.wye.presentation.explore.detail.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
import androidx.compose.ui.zIndex
import com.d102.wye.domain.model.EtfCluster
import com.d102.wye.domain.model.EtfClusterData
import com.d102.wye.domain.model.EtfDetail
import com.d102.wye.domain.model.InfluentialStock
import com.d102.wye.presentation.designsystem.CategoryBadge
import com.d102.wye.presentation.explore.detail.EtfDetailViewModel
import com.d102.wye.presentation.theme.*
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun ClusterTab(
    detail: EtfDetail,
    clusterData: EtfClusterData,
    viewModel: EtfDetailViewModel,
    onStockClick: (String) -> Unit = {},
) {
    var selectedCluster by remember { mutableStateOf<EtfCluster?>(null) }

    if (selectedCluster != null) {
        SectorBottomSheet(
            cluster = selectedCluster!!,
            onDismiss = { selectedCluster = null },
            onStockClick = onStockClick,
        )
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenH = maxHeight

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Column(modifier = Modifier.height(screenH - 100.dp)) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    EtfHeader(detail = detail, englishName = clusterData.englishName)

                    ClusterBubbleChart(
                        name = detail.name,
                        clusters = clusterData.sectors,
                        onClusterClick = { selectedCluster = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    )

                    PriceVolumeRow(detail = detail)
                }
            }

            // 영향을 많이 끼치는 종목
            InfluentialStocksSection(stocks = clusterData.influentialStocks, onStockClick = onStockClick)
        }
    }
}

@Composable
private fun InfluentialStocksSection(
    stocks: List<InfluentialStock>,
    onStockClick: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            "현재 이 ETF에 영향을 많이 끼치는 종목은?",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = TextPrimary,
        )
        HorizontalDivider(color = Divider)
        if (stocks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "데이터 준비 중입니다.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
            }
        } else {
            stocks.forEach { stock ->
                InfluentialStockItem(stock = stock, onStockClick = onStockClick)
            }
        }
    }
}

@Composable
private fun InfluentialStockItem(
    stock: InfluentialStock,
    onStockClick: (String) -> Unit,
) {
    val changeColor = if (stock.changeRate >= 0) EtfRise else EtfFall
    val changeSign  = if (stock.changeRate >= 0) "+" else ""

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceVariant)
            .clickable { onStockClick(stock.ticker) }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 왼쪽: 아바타 + 종목명/티커
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(BackGroundLightGreen),
            ) {
                Text(
                    stock.name.take(1),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = PrimaryGreen,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(stock.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = TextPrimary)
                Text(stock.ticker, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
        }
        // 오른쪽: 비중 / 현재가 / 등락률
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                "비중 ${"%.1f".format(stock.weight)}%",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = PrimaryGreen,
            )
            Text(
                "%,d원".format(stock.currentPrice),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = TextPrimary,
            )
            Text(
                "$changeSign${"%.2f".format(stock.changeRate)}%",
                style = MaterialTheme.typography.bodySmall,
                color = changeColor,
            )
        }
    }
}

@Composable
private fun EtfHeader(detail: EtfDetail, englishName: String) {
    val (badgeBg, badgeFg, badgeLabel) = riskToBadge(detail.riskGrade)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        CategoryBadge(label = badgeLabel, backgroundColor = badgeBg, textColor = badgeFg, isPill = true, modifier = Modifier.scale(1.3f).padding(bottom = 4.dp))
        Text(detail.name, style = MaterialTheme.typography.titleLarge.copy(fontSize = 28.sp, fontWeight = FontWeight.ExtraBold), color = TextPrimary)
        if (englishName.isNotBlank()) {
            Text(englishName, style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp), color = TextSecondary)
        }
    }
}

@Composable
private fun ClusterBubbleChart(
    name: String,
    clusters: List<EtfCluster>,
    onClusterClick: (EtfCluster) -> Unit,
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

    // 비중 내림차순 정렬, 상대 반경 계산 (center = 1.0)
    val sorted = remember(clusters) { clusters.sortedByDescending { it.percentage } }
    val maxPct = sorted.maxOfOrNull { it.percentage } ?: 1.0
    val minPct = sorted.minOfOrNull { it.percentage } ?: 0.0

    val sectorRelRadii = remember(sorted) {
        sorted.map { cluster ->
            val pct = if (maxPct > minPct)
                ((cluster.percentage - minPct) / (maxPct - minPct)).toFloat() else 0.5f
            0.62f + 0.13f * pct   // 0.62 ~ 0.75 (relative to center r=1.0)
        }
    }

    // Circle Packing — remember로 한 번만 계산 (gap = 버블간 여백)
    val packed = remember(sectorRelRadii) { circlePackAround(1.0f, sectorRelRadii, gap = 0.32f) }

    BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.Center) {
        val extentX = packed.maxOf { abs(it.x) + it.r }.coerceAtLeast(0.1f)
        val extentY = packed.maxOf { abs(it.y) + it.r }.coerceAtLeast(0.1f)

        // 중앙 버블: 화면 너비의 26%로 고정 (섹터 수와 무관)
        val centerSizeDp = (maxWidth * 0.26f).coerceIn(80.dp, 115.dp)
        // 섹터 버블 스케일: 중앙 r=1.0 → centerSizeDp/2 가 되도록 고정
        val scale = centerSizeDp.value / 2f

        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val cr = centerSizeDp.toPx() / 2f
            for (i in 0..2) {
                val progress = (pulseProgress + i / 3f) % 1f
                drawCircle(
                    color  = PrimaryGreen,
                    radius = cr * 1.5f * progress,
                    center = Offset(cx, cy),
                    alpha  = (1f - progress) * 0.25f,
                    style  = Stroke(width = 1.6f),
                )
            }
        }

        // 중앙 버블 (고정, packed[0] = center)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .zIndex(10f)
                .size(centerSizeDp)
                .scale(centerScale)
                .shadow(elevation = 14.dp, shape = CircleShape, clip = false)
                .clip(CircleShape)
                .background(PrimaryGreen),
        ) {
            val fontSize = (centerSizeDp.value * 0.14f).coerceIn(12f, 18f).sp
            Text(
                text = name.replace(" ", "\n"),
                color = Color.White,
                fontSize = fontSize,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                lineHeight = fontSize * 1.35f,
                modifier = Modifier.padding(horizontal = 10.dp),
            )
        }

        // 섹터 버블 — packed[0]이 center이므로 [idx+1]
        sorted.forEachIndexed { idx, cluster ->
            val c = packed.getOrNull(idx + 1) ?: return@forEachIndexed
            ClusterBubble(
                cluster    = cluster,
                index      = idx,
                visible    = visible,
                bubbleSize = (c.r * 2f * scale).dp,
                onClick    = { onClusterClick(cluster) },
                modifier   = Modifier.offset(
                    x = (c.x * scale).dp,
                    y = (c.y * scale).dp,
                ),
            )
        }
    }
}

@Composable
private fun ClusterBubble(
    cluster: EtfCluster,
    index: Int,
    visible: Boolean,
    bubbleSize: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
        label = "clusterBubble$index",
    )

    val floatTransition = rememberInfiniteTransition(label = "float$index")
    val floatY by floatTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
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
                imageVector = sectorIcon(cluster.name),
                contentDescription = cluster.name,
                tint = PrimaryGreen,
                modifier = Modifier.size(iconSize),
            )
            Text(
                text = cluster.name,
                fontSize = nameFontSize,
                lineHeight = nameFontSize * 1.1f,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                textAlign = TextAlign.Center,
                maxLines = 2,
                modifier = Modifier.width(contentWidth),
            )
            Text(
                text = "${"%.1f".format(cluster.percentage)}%",
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

private fun sectorIcon(name: String): ImageVector = when {
    name.contains("반도체") -> Icons.Filled.Memory
    name.contains("금융") || name.contains("은행") || name.contains("보험") -> Icons.Filled.AccountBalance
    name.contains("헬스케어") || name.contains("바이오") || name.contains("의료") || name.contains("제약") -> Icons.Filled.LocalHospital
    name.contains("에너지") || name.contains("정유") || name.contains("신재생") -> Icons.Filled.Bolt
    name.contains("IT") || name.contains("소프트웨어") || name.contains("인터넷") || name.contains("플랫폼") || name.contains("기술") || name.contains("테크") -> Icons.Filled.Computer
    name.contains("소비재") || name.contains("유통") || name.contains("식품") || name.contains("화장품") -> Icons.Filled.ShoppingCart
    name.contains("산업재") || name.contains("기계") || name.contains("조선") || name.contains("방산") || name.contains("건설") -> Icons.Filled.Factory
    name.contains("통신") || name.contains("미디어") || name.contains("방송") -> Icons.Filled.CellTower
    name.contains("유틸리티") || name.contains("전력") || name.contains("가스") || name.contains("수도") -> Icons.Filled.WaterDrop
    name.contains("부동산") || name.contains("리츠") -> Icons.Filled.Home
    name.contains("자동차") || name.contains("전기차") || name.contains("모빌리티") -> Icons.Filled.DirectionsCar
    name.contains("화학") || name.contains("소재") || name.contains("철강") -> Icons.Filled.Science
    else -> Icons.Filled.Category
}

private fun riskToBadge(grade: Int) = when (grade) {
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

// ── Circle Packing ─────────────────────────────────────────────────────────────

private data class PackCircle(val x: Float, val y: Float, val r: Float)

/**
 * Greedy circle packing — 중앙(0,0)에 centerR 크기 원을 고정하고
 * sectorRadii 목록의 원들을 겹치지 않게 가능한 중앙 가까이 배치.
 * 반환값: [center, sector0, sector1, ...] 순서
 */
private fun circlePackAround(centerR: Float, sectorRadii: List<Float>, gap: Float = 0f): List<PackCircle> {
    val placed = mutableListOf(PackCircle(0f, 0f, centerR))

    for (r in sectorRadii) {
        var best: PackCircle? = null
        var bestDist = Float.MAX_VALUE

        for (i in placed.indices) {
            for (j in i + 1 until placed.size) {
                for (c in packTangentPositions(placed[i], placed[j], r, gap)) {
                    if (placed.all { p -> packDist(c.x, c.y, p) >= p.r + r + gap - 1e-3f }) {
                        val d = sqrt(c.x * c.x + c.y * c.y)
                        if (d < bestDist) { bestDist = d; best = c }
                    }
                }
            }
        }

        if (best == null) {
            for (p in placed.sortedBy { sqrt(it.x * it.x + it.y * it.y) }) {
                for (k in 0 until 72) {
                    val a  = k * 2.0 * PI / 72
                    val cx = p.x + (p.r + r + gap) * cos(a).toFloat()
                    val cy = p.y + (p.r + r + gap) * sin(a).toFloat()
                    if (placed.all { q -> packDist(cx, cy, q) >= q.r + r + gap - 1e-3f }) {
                        val d = sqrt(cx * cx + cy * cy)
                        if (d < bestDist) { bestDist = d; best = PackCircle(cx, cy, r) }
                    }
                }
            }
        }

        placed.add(best ?: PackCircle(placed[0].r + r + gap, 0f, r))
    }
    return placed
}

private fun packTangentPositions(a: PackCircle, b: PackCircle, r: Float, gap: Float = 0f): List<PackCircle> {
    val d1 = a.r + r + gap;  val d2 = b.r + r + gap
    val dx = b.x - a.x; val dy = b.y - a.y
    val d  = sqrt(dx * dx + dy * dy)
    if (d > d1 + d2 + 1e-3f || d < abs(d1 - d2) - 1e-3f) return emptyList()
    val aa = (d1 * d1 - d2 * d2 + d * d) / (2f * d)
    val h  = sqrt((d1 * d1 - aa * aa).coerceAtLeast(0f))
    val mx = a.x + aa * dx / d;  val my = a.y + aa * dy / d
    return listOf(
        PackCircle(mx + h * dy / d, my - h * dx / d, r),
        PackCircle(mx - h * dy / d, my + h * dx / d, r),
    )
}

private fun packDist(x: Float, y: Float, b: PackCircle) =
    sqrt((x - b.x) * (x - b.x) + (y - b.y) * (y - b.y))
