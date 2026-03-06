package com.d102.wye.presentation.explore.detail.component

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.d102.wye.domain.model.EtfDetail
import com.d102.wye.domain.model.EtfSector
import com.d102.wye.domain.model.InfluentialStock
import com.d102.wye.domain.model.SectorStock
import com.d102.wye.presentation.designsystem.CategoryBadge
import com.d102.wye.presentation.explore.detail.EtfDetailViewModel
import com.d102.wye.presentation.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ClusterTab(
    detail: EtfDetail,
    viewModel: EtfDetailViewModel,
) {
    var selectedSector by remember { mutableStateOf<EtfSector?>(null) }

    if (selectedSector != null) {
        SectorBottomSheet(
            sector = selectedSector!!,
            onDismiss = { selectedSector = null },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // 위험등급 뱃지 + 이름
        EtfHeader(detail = detail)

        // 클러스터 버블 차트
        ClusterBubbleChart(
            ticker = detail.ticker,
            sectors = detail.sectors,
            onSectorClick = { selectedSector = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp),
        )

        // 현재 가격 / 거래량 카드
        PriceVolumeRow(detail = detail)

        // 영향 종목
        InfluentialStocksSection(stocks = detail.influentialStocks)
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
        Text(detail.ticker, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
        Text(detail.englishName, fontSize = 13.sp, color = TextSecondary)
    }
}

@Composable
private fun ClusterBubbleChart(
    ticker: String,
    sectors: List<EtfSector>,
    onSectorClick: (EtfSector) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // 배경 연결선 (선택사항 - 생략 가능)
        }
        // 중앙 ETF 버블
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(110.dp)
                .clip(CircleShape)
                .background(PrimaryGreen),
        ) {
            Text(
                text = ticker,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
        }
        // 주변 섹터 버블 (최대 6개)
        val displaySectors = sectors.take(6)
        val angleStep = 360.0 / displaySectors.size.coerceAtLeast(1)
        val radius = 120.dp
        displaySectors.forEachIndexed { idx, sector ->
            val angleDeg = idx * angleStep - 90.0
            val angleRad = Math.toRadians(angleDeg)
            val x = (radius.value * cos(angleRad)).dp
            val y = (radius.value * sin(angleRad)).dp
            SectorBubble(
                sector = sector,
                onClick = { onSectorClick(sector) },
                modifier = Modifier.offset(x = x, y = y),
            )
        }
    }
}

@Composable
private fun SectorBubble(
    sector: EtfSector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(SurfaceVariant)
            .clickable(onClick = onClick),
    ) {
        Text(sector.name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary, textAlign = TextAlign.Center)
        Text("${"%.1f".format(sector.percentage)}%", fontSize = 10.sp, color = TextSecondary)
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
            valueColor = if (detail.changeRate >= 0) EtfRise else EtfFall,
            modifier = Modifier.weight(1f),
        )
        InfoCard(
            label = "거래량",
            value = formatVolume(detail.volume),
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
            .background(SurfaceVariant)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(label, fontSize = 12.sp, color = TextSecondary)
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

@Composable
private fun InfluentialStocksSection(stocks: List<InfluentialStock>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "현재 이 ETF에 영향을 많이 끼치는 종목은?",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
        )
        stocks.forEach { stock ->
            InfluentialStockItem(stock = stock)
            HorizontalDivider(color = Divider)
        }
    }
}

@Composable
private fun InfluentialStockItem(stock: InfluentialStock) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
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
            Text(stock.name.take(1), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(stock.name, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
            Text("${"%.2f".format(stock.weight)}%", fontSize = 12.sp, color = TextSecondary)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("%,d원".format(stock.currentPrice), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            val changeColor = if (stock.changeRate >= 0) EtfRise else EtfFall
            val sign = if (stock.changeRate >= 0) "+" else ""
            Text("$sign${"%.1f".format(stock.changeRate)}%", fontSize = 12.sp, color = changeColor)
        }
    }
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
