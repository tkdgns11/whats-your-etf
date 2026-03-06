package com.d102.wye.presentation.explore.detail.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.d102.wye.domain.model.*
import com.d102.wye.presentation.explore.detail.EtfDetailViewModel
import com.d102.wye.presentation.model.UiState
import com.d102.wye.presentation.theme.*

@Composable
fun EtfDetailInfoTab(
    detail: EtfDetail,
    viewModel: EtfDetailViewModel,
) {
    val chartState    by viewModel.chartState.collectAsStateWithLifecycle()
    val periodReturn  by viewModel.periodReturn.collectAsStateWithLifecycle()
    val selectedPeriod by viewModel.selectedPeriod.collectAsStateWithLifecycle()
    val showNav   by viewModel.showNav.collectAsStateWithLifecycle()
    val showPrice by viewModel.showPrice.collectAsStateWithLifecycle()
    val showKospi by viewModel.showKospi.collectAsStateWithLifecycle()
    val showSp500 by viewModel.showSp500.collectAsStateWithLifecycle()
    var productInfoExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // 가격 그리드
        PriceGrid(detail = detail)

        // 상품정보 토글
        ProductInfoSection(
            detail = detail,
            expanded = productInfoExpanded,
            onToggle = { productInfoExpanded = !productInfoExpanded },
        )

        // 수익률 그래프
        ReturnChartSection(
            chartState = chartState,
            selectedPeriod = selectedPeriod,
            showNav = showNav,
            showPrice = showPrice,
            showKospi = showKospi,
            showSp500 = showSp500,
            onPeriodSelected = viewModel::onPeriodSelected,
            onLoadChart = { viewModel.loadChart() },
            onToggleNav   = viewModel::toggleNav,
            onTogglePrice = viewModel::togglePrice,
            onToggleKospi = viewModel::toggleKospi,
            onToggleSp500 = viewModel::toggleSp500,
        )

        // 기간별 수익률 표
        if (periodReturn is UiState.Success) {
            PeriodReturnTable(data = (periodReturn as UiState.Success<EtfPeriodReturn>).data)
        }

        // 주석
        Footnotes()
    }
}

// ── 가격 그리드 ────────────────────────────────────────────────

@Composable
private fun PriceGrid(detail: EtfDetail) {
    val changeColor = if (detail.changeRate >= 0) EtfRise else EtfFall
    val sign = if (detail.changeAmount >= 0) "+" else ""
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PriceCard(
                label = "현재가",
                value = "%,d원".format(detail.currentPrice),
                sub = "$sign%,d원 (${"%.2f".format(detail.changeRate)}%)".format(detail.changeAmount),
                subColor = changeColor,
                modifier = Modifier.weight(1f),
            )
            PriceCard(
                label = "기준가(iNAV)",
                value = "%,d원".format(detail.iNav),
                sub = "${if (detail.iNavChangeAmount >= 0) "+" else ""}%,d원 (${"%.2f".format(detail.iNavChangeRate)}%)".format(detail.iNavChangeAmount),
                subColor = if (detail.iNavChangeRate >= 0) EtfRise else EtfFall,
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PriceCard(
                label = "수익률(1개월)",
                value = "${if (detail.returnRate1M >= 0) "+" else ""}${"%.2f".format(detail.returnRate1M)}%",
                sub = "최근 30일 기준",
                valueColor = if (detail.returnRate1M >= 0) EtfRise else EtfFall,
                modifier = Modifier.weight(1f),
            )
            PriceCard(
                label = "거래량",
                value = "%,d".format(detail.volume),
                sub = "주 (당일 누적)",
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun PriceCard(
    label: String,
    value: String,
    sub: String,
    modifier: Modifier = Modifier,
    valueColor: Color = TextPrimary,
    subColor: Color = TextSecondary,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceVariant)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(label, fontSize = 11.sp, color = TextSecondary)
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = valueColor)
        Text(sub, fontSize = 11.sp, color = subColor)
    }
}

// ── 상품정보 토글 ──────────────────────────────────────────────

@Composable
private fun ProductInfoSection(detail: EtfDetail, expanded: Boolean, onToggle: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceVariant),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (expanded) "상품정보 닫기" else "상품정보 자세히 보기",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
            )
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = TextSecondary,
            )
        }
        if (expanded) {
            HorizontalDivider(color = Divider, modifier = Modifier.padding(horizontal = 14.dp))
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                InfoRow("운용사", detail.manager)
                InfoRow("투자위험", "${detail.riskLevel}등급")
                InfoRow("변동성", if (detail.volatility.isBlank()) "----" else detail.volatility)
                InfoRow("총보수(수수료)", "연 ${"%.4f".format(detail.expenseRatio)}%")
                InfoRow("순자산", "%,d 억원".format(detail.netAsset / 100_000_000))
                InfoRow("상장일", detail.listedDate)
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, fontSize = 13.sp, color = PrimaryGreen, fontWeight = FontWeight.Medium)
        Text(value, fontSize = 13.sp, color = TextPrimary)
    }
}

// ── 수익률 그래프 섹션 ─────────────────────────────────────────

@Composable
private fun ReturnChartSection(
    chartState: UiState<EtfReturnChart>,
    selectedPeriod: String,
    showNav: Boolean,
    showPrice: Boolean,
    showKospi: Boolean,
    showSp500: Boolean,
    onPeriodSelected: (String) -> Unit,
    onLoadChart: () -> Unit,
    onToggleNav: () -> Unit,
    onTogglePrice: () -> Unit,
    onToggleKospi: () -> Unit,
    onToggleSp500: () -> Unit,
) {
    val periods = listOf("1주", "1개월", "3개월", "1년", "3년", "전체")
    val periodKeys = listOf("1W", "1M", "3M", "1Y", "3Y", "ALL")

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("수익률 그래프", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text("5분전 업데이트", fontSize = 11.sp, color = TextSecondary)
        }

        // 기간 탭
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            periods.forEachIndexed { idx, label ->
                val key = periodKeys[idx]
                val selected = selectedPeriod == key
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    color = if (selected) TextOnColored else TextPrimary,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (selected) PrimaryGreen else Color.Transparent)
                        .clickable { onPeriodSelected(key) }
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
        }

        // 조회 버튼
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Text(
                text = "조회",
                fontSize = 13.sp,
                color = TextOnColored,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(PrimaryGreen)
                    .clickable(onClick = onLoadChart)
                    .padding(horizontal = 16.dp, vertical = 6.dp),
            )
        }

        // 라인 체크박스
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ChartCheckbox("NAV",    showNav,   onToggleNav,   PrimaryGreen)
            ChartCheckbox("종가",   showPrice, onTogglePrice, EtfFall)
            ChartCheckbox("KOSPI",  showKospi, onToggleKospi, Color.Gray)
            ChartCheckbox("S&P 500",showSp500, onToggleSp500, Color.LightGray)
        }

        // 차트
        when (chartState) {
            is UiState.Loading -> Box(
                Modifier.fillMaxWidth().height(180.dp),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator(modifier = Modifier.size(24.dp)) }

            is UiState.Success -> LineChart(
                data = chartState.data,
                showNav = showNav,
                showPrice = showPrice,
                showKospi = showKospi,
                showSp500 = showSp500,
                modifier = Modifier.fillMaxWidth().height(180.dp),
            )

            else -> Box(
                Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceVariant),
                contentAlignment = Alignment.Center,
            ) { Text("조회 버튼을 눌러 불러오세요", fontSize = 13.sp, color = TextSecondary) }
        }
    }
}

@Composable
private fun ChartCheckbox(label: String, checked: Boolean, onToggle: () -> Unit, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.clickable(onClick = onToggle),
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(checkedColor = color),
            modifier = Modifier.size(20.dp),
        )
        Text(label, fontSize = 12.sp, color = TextSecondary)
    }
}

@Composable
private fun LineChart(
    data: EtfReturnChart,
    showNav: Boolean,
    showPrice: Boolean,
    showKospi: Boolean,
    showSp500: Boolean,
    modifier: Modifier = Modifier,
) {
    val lines = buildList {
        if (showNav   && data.navData.isNotEmpty())    add(data.navData   to PrimaryGreen)
        if (showPrice && data.priceData.isNotEmpty())  add(data.priceData to EtfFall)
        if (showKospi && data.kospiData.isNotEmpty())  add(data.kospiData to Color.Gray)
        if (showSp500 && data.sp500Data.isNotEmpty())  add(data.sp500Data to Color.LightGray)
    }
    if (lines.isEmpty()) return

    Canvas(modifier = modifier.clip(RoundedCornerShape(8.dp)).background(SurfaceVariant).padding(8.dp)) {
        val allValues = lines.flatMap { (pts, _) -> pts.map { it.value } }
        val minV = allValues.min()
        val maxV = allValues.max()
        val range = (maxV - minV).takeIf { it > 0 } ?: 1.0

        lines.forEach { (points, color) ->
            val path = Path()
            points.forEachIndexed { idx, pt ->
                val x = size.width * idx / (points.size - 1).coerceAtLeast(1)
                val y = size.height * (1 - (pt.value - minV) / range).toFloat()
                if (idx == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path, color = color, style = Stroke(width = 3f))
        }
    }
}

// ── 기간별 수익률 표 ───────────────────────────────────────────

@Composable
private fun PeriodReturnTable(data: EtfPeriodReturn) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("기간별 수익률", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text("26.03.04기준", fontSize = 11.sp, color = TextSecondary)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, Divider, RoundedCornerShape(12.dp)),
        ) {
            TableHeader()
            HorizontalDivider(color = Divider)
            TableRow("NAV (%)",    data.nav1M,   data.nav3M,   data.nav6M)
            HorizontalDivider(color = Divider)
            TableRow("기초지수 (%)", data.index1M, data.index3M, data.index6M)
            HorizontalDivider(color = Divider)
            TableRow("종가 (%)",   data.price1M, data.price3M, data.price6M)
        }
    }
}

@Composable
private fun TableHeader() {
    Row(
        modifier = Modifier.fillMaxWidth().background(SurfaceVariant).padding(vertical = 10.dp),
    ) {
        Text("구분",   fontSize = 12.sp, color = TextSecondary, modifier = Modifier.weight(2f), textAlign = TextAlign.Center)
        Text("1개월",  fontSize = 12.sp, color = TextSecondary, modifier = Modifier.weight(1.5f), textAlign = TextAlign.Center)
        Text("3개월",  fontSize = 12.sp, color = TextSecondary, modifier = Modifier.weight(1.5f), textAlign = TextAlign.Center)
        Text("6개월",  fontSize = 12.sp, color = TextSecondary, modifier = Modifier.weight(1.5f), textAlign = TextAlign.Center)
    }
}

@Composable
private fun TableRow(label: String, v1m: Double, v3m: Double, v6m: Double) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, fontSize = 12.sp, color = TextPrimary, modifier = Modifier.weight(2f), textAlign = TextAlign.Center)
        listOf(v1m, v3m, v6m).forEach { v ->
            Text(
                text = "${if (v >= 0) "" else ""}${"%.2f".format(v)}",
                fontSize = 12.sp,
                color = if (v >= 0) EtfRise else EtfFall,
                modifier = Modifier.weight(1.5f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

// ── 주석 ───────────────────────────────────────────────────────

@Composable
private fun Footnotes() {
    val notes = listOf(
        "• 기준가격(NAV)는 분배금 재투자를 가정한 세전 수익률 기준입니다.",
        "• 기초지수는 배당금재투자를 가정한 총수익지수입니다.",
        "• 시장가격(종가)는 분배금을 포함하지 않은 주식시장에서 거래되는 가격 기준입니다.",
        "• 수익률 그래프는 상장일 이후로 조회 가능합니다.",
        "• 위 기간 수익률은 누적 수익률 기준입니다.",
    )
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        notes.forEach { note ->
            Text(note, fontSize = 11.sp, color = TextSecondary, lineHeight = 16.sp)
        }
    }
}
