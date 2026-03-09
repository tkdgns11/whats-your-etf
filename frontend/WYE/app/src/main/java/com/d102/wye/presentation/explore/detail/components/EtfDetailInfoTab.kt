package com.d102.wye.presentation.explore.detail.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.d102.wye.domain.model.*
import com.d102.wye.presentation.explore.detail.EtfDetailViewModel
import com.d102.wye.presentation.model.UiState
import com.d102.wye.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EtfDetailInfoTab(
    detail: EtfDetail,
    viewModel: EtfDetailViewModel,
) {
    val chartState     by viewModel.chartState.collectAsStateWithLifecycle()
    val periodReturn   by viewModel.periodReturn.collectAsStateWithLifecycle()
    val selectedPeriod by viewModel.selectedPeriod.collectAsStateWithLifecycle()
    val startDateMs        by viewModel.startDateMs.collectAsStateWithLifecycle()
    val endDateMs          by viewModel.endDateMs.collectAsStateWithLifecycle()
    val periodDurationDays by viewModel.periodDurationDays.collectAsStateWithLifecycle()
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
            startDateMs = startDateMs,
            endDateMs = endDateMs,
            showNav = showNav,
            showPrice = showPrice,
            showKospi = showKospi,
            showSp500 = showSp500,
            periodDurationDays = periodDurationDays,
            onPeriodSelected = viewModel::onPeriodSelected,
            onDateRangeSelected = viewModel::onDateRangeSelected,
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
    val iNavSign = if (detail.iNavChangeAmount >= 0) "+" else ""
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PriceCard(
                label = "현재가",
                value = "%,d원".format(detail.currentPrice),
                sub = "$sign${"%,d".format(detail.changeAmount)}원 (${"%.2f".format(detail.changeRate)}%)",
                subColor = changeColor,
                modifier = Modifier.weight(1f),
            )
            PriceCard(
                label = "기준가(iNAV)",
                value = "%,d원".format(detail.iNav),
                sub = "$iNavSign${"%,d".format(detail.iNavChangeAmount)}원 (${"%.2f".format(detail.iNavChangeRate)}%)",
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
            .border(1.dp, Border, RoundedCornerShape(12.dp))
            .background(Background)
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
            .background(BackGroundLightGreen),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(14.dp),
            horizontalArrangement = Arrangement.Center,
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
        Text(label, fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
        Text(value, fontSize = 13.sp, color = TextPrimary)
    }
}

// ── 수익률 그래프 섹션 ─────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReturnChartSection(
    chartState: UiState<EtfReturnChart>,
    selectedPeriod: String,
    startDateMs: Long?,
    endDateMs: Long?,
    periodDurationDays: Int?,
    showNav: Boolean,
    showPrice: Boolean,
    showKospi: Boolean,
    showSp500: Boolean,
    onPeriodSelected: (String) -> Unit,
    onDateRangeSelected: (Long, Long) -> Unit,
    onLoadChart: () -> Unit,
    onToggleNav: () -> Unit,
    onTogglePrice: () -> Unit,
    onToggleKospi: () -> Unit,
    onToggleSp500: () -> Unit,
) {
    val periods    = listOf("전체", "1주", "1개월", "3개월", "1년", "3년")
    val periodKeys = listOf("ALL", "1W",  "1M",    "3M",    "1Y",  "3Y")

    var showDatePicker by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // 헤더
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("수익률 그래프", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text("5분전 업데이트", fontSize = 11.sp, color = TextSecondary)
        }

        // 기간 프리셋
        Row(modifier = Modifier.fillMaxWidth()) {
            periods.forEachIndexed { idx, label ->
                val key      = periodKeys[idx]
                val selected = selectedPeriod == key
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (selected) PrimaryGreen else Color.Transparent)
                        .clickable { onPeriodSelected(key) }
                        .padding(vertical = 6.dp),
                ) {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (selected) TextOnColored else TextSecondary,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        // 날짜 범위 입력 + 조회
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DateRangeField(
                startDateMs = startDateMs,
                endDateMs = endDateMs,
                onClick = { showDatePicker = true },
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "조회",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextOnColored,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(PrimaryGreen)
                    .clickable(onClick = onLoadChart)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            )
        }

        // 라인 체크박스
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ChartCheckbox("NAV",     showNav,   onToggleNav,   PrimaryGreen)
            ChartCheckbox("종가",    showPrice, onTogglePrice, EtfFall)
            ChartCheckbox("KOSPI",   showKospi, onToggleKospi, Color.Gray)
            ChartCheckbox("S&P 500", showSp500, onToggleSp500, Color.LightGray)
        }

        // 차트
        when (chartState) {
            is UiState.Loading -> Box(
                Modifier.fillMaxWidth().height(220.dp),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator(modifier = Modifier.size(24.dp)) }

            is UiState.Success -> LineChart(
                data = chartState.data,
                showNav = showNav,
                showPrice = showPrice,
                showKospi = showKospi,
                showSp500 = showSp500,
                modifier = Modifier.fillMaxWidth().height(220.dp),
            )

            else -> Box(
                Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceVariant),
                contentAlignment = Alignment.Center,
            ) { Text("조회 버튼을 눌러 불러오세요", fontSize = 13.sp, color = TextSecondary) }
        }
    }

    if (showDatePicker) {
        RangeCalendarBottomSheet(
            startDateMs = startDateMs,
            endDateMs = endDateMs,
            periodDurationDays = periodDurationDays,
            onRangeSelected = onDateRangeSelected,
            onDismiss = { showDatePicker = false },
        )
    }
}

@Composable
private fun DateRangeField(
    startDateMs: Long?,
    endDateMs: Long?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val text = if (startDateMs != null && endDateMs != null)
        "${startDateMs.toDateString()}  ~  ${endDateMs.toDateString()}"
    else
        "기간 선택"

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, Border, RoundedCornerShape(10.dp))
            .background(Background)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            color = if (startDateMs != null) TextPrimary else TextSecondary,
        )
        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            tint = PrimaryGreen,
            modifier = Modifier.size(18.dp),
        )
    }
}

private fun Long.toDateString(): String {
    val cal = java.util.Calendar.getInstance()
    cal.timeInMillis = this
    return "%04d.%02d.%02d".format(
        cal.get(java.util.Calendar.YEAR),
        cal.get(java.util.Calendar.MONTH) + 1,
        cal.get(java.util.Calendar.DAY_OF_MONTH),
    )
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
        if (showNav   && data.navData.isNotEmpty())   add(data.navData   to PrimaryGreen)
        if (showPrice && data.priceData.isNotEmpty()) add(data.priceData to EtfFall)
        if (showKospi && data.kospiData.isNotEmpty()) add(data.kospiData to Color.Gray)
        if (showSp500 && data.sp500Data.isNotEmpty()) add(data.sp500Data to Color.LightGray)
    }
    if (lines.isEmpty()) return

    Canvas(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Background)
            .padding(start = 8.dp, end = 8.dp, top = 20.dp, bottom = 8.dp),
    ) {
        val allValues = lines.flatMap { (pts, _) -> pts.map { it.value } }
        val minV  = allValues.min()
        val maxV  = allValues.max()
        val range = (maxV - minV).takeIf { it > 0 } ?: 1.0

        // 수평 그리드 라인 4개
        repeat(5) { i ->
            val y = size.height * i / 4f
            drawLine(
                color = Color(0xFFE2E8E4),
                start = Offset(0f, y),
                end   = Offset(size.width, y),
                strokeWidth = 0.8f,
            )
        }

        lines.forEach { (points, color) ->
            if (points.size < 2) return@forEach

            fun xOf(i: Int)    = size.width  * i / (points.size - 1).toFloat()
            fun yOf(v: Double) = size.height * (1.0 - (v - minV) / range).toFloat()

            val linePath = Path()
            val fillPath = Path()

            val x0 = xOf(0); val y0 = yOf(points[0].value)
            linePath.moveTo(x0, y0)
            fillPath.moveTo(x0, size.height)
            fillPath.lineTo(x0, y0)

            for (i in 1 until points.size) {
                val px = xOf(i - 1); val py = yOf(points[i - 1].value)
                val cx = xOf(i);     val cy = yOf(points[i].value)
                val cpX = (px + cx) / 2f
                linePath.cubicTo(cpX, py, cpX, cy, cx, cy)
                fillPath.cubicTo(cpX, py, cpX, cy, cx, cy)
            }

            // 그라디언트 채우기
            fillPath.lineTo(xOf(points.size - 1), size.height)
            fillPath.close()
            drawPath(
                fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(color.copy(alpha = 0.22f), Color.Transparent),
                    startY = 0f, endY = size.height,
                ),
            )

            // 선
            drawPath(
                linePath,
                color = color,
                style = Stroke(
                    width = 2.5f,
                    cap   = StrokeCap.Round,
                    join  = StrokeJoin.Round,
                ),
            )
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

// ── 커스텀 달력 바텀시트 ───────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RangeCalendarBottomSheet(
    startDateMs: Long?,
    endDateMs: Long?,
    periodDurationDays: Int?,   // null = 자유 선택, 값 있으면 시작일 탭 시 자동 계산
    onRangeSelected: (Long, Long) -> Unit,
    onDismiss: () -> Unit,
) {
    val todayCal = remember { java.util.Calendar.getInstance() }
    var displayYear  by remember { mutableIntStateOf(todayCal.get(java.util.Calendar.YEAR)) }
    var displayMonth by remember { mutableIntStateOf(todayCal.get(java.util.Calendar.MONTH)) }
    var localStartMs by remember { mutableStateOf(startDateMs) }
    var localEndMs   by remember { mutableStateOf(endDateMs) }
    var showYearPicker by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Background,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text("기간 선택", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)

            // 선택된 범위 표시
            val rangeLabel = when {
                localStartMs != null && localEndMs != null ->
                    "${localStartMs!!.toDateString()}  ~  ${localEndMs!!.toDateString()}"
                localStartMs != null -> "${localStartMs!!.toDateString()}  ~  종료일 선택"
                else -> "날짜를 선택하세요"
            }
            Text(
                text = rangeLabel,
                fontSize = 14.sp,
                color = if (localStartMs != null) PrimaryGreen else TextSecondary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 4.dp),
            )

            // 월 네비게이션
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = {
                        if (!showYearPicker) {
                            if (displayMonth == 0) { displayMonth = 11; displayYear-- } else displayMonth--
                        }
                    },
                ) {
                    Icon(Icons.Default.KeyboardArrowLeft, contentDescription = null, tint = if (showYearPicker) Color.Transparent else TextPrimary)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.clickable { showYearPicker = !showYearPicker },
                ) {
                    Text(
                        text = "${displayYear}년 ${displayMonth + 1}월",
                        fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary,
                    )
                    Icon(
                        imageVector = if (showYearPicker) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp),
                    )
                }
                IconButton(
                    onClick = {
                        if (!showYearPicker) {
                            if (displayMonth == 11) { displayMonth = 0; displayYear++ } else displayMonth++
                        }
                    },
                ) {
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = if (showYearPicker) Color.Transparent else TextPrimary)
                }
            }

            if (showYearPicker) {
                // 연도 선택 그리드
                val years = (2000..todayCal.get(java.util.Calendar.YEAR) + 2).toList()
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(years) { year ->
                        val selected = year == displayYear
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selected) PrimaryGreen else Color.Transparent)
                                .clickable { displayYear = year; showYearPicker = false }
                                .padding(vertical = 10.dp),
                        ) {
                            Text(
                                text = "${year}년",
                                fontSize = 13.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) TextOnColored else TextPrimary,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            } else {
                // 요일 헤더
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf("일", "월", "화", "수", "목", "금", "토").forEachIndexed { i, label ->
                        Text(
                            text = label,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            color = if (i == 0) EtfFall else TextSecondary,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }

                HorizontalDivider(color = Divider)

                // 달력 그리드
                CalendarMonthGrid(
                    year = displayYear,
                    month = displayMonth,
                    startMs = localStartMs,
                    endMs = localEndMs,
                    onDayClick = { clickedMs ->
                        if (periodDurationDays != null) {
                            localStartMs = clickedMs
                            localEndMs   = clickedMs + periodDurationDays.toLong() * 86_400_000
                        } else {
                            if (localStartMs == null || localEndMs != null) {
                                localStartMs = clickedMs; localEndMs = null
                            } else {
                                if (clickedMs >= localStartMs!!) localEndMs = clickedMs
                                else { localStartMs = clickedMs; localEndMs = null }
                            }
                        }
                    },
                )
            }

            Spacer(Modifier.height(8.dp))

            // 확인 / 취소
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(48.dp),
                    border = BorderStroke(1.dp, PrimaryGreen),
                    shape = RoundedCornerShape(12.dp),
                ) { Text("취소", color = PrimaryGreen, fontWeight = FontWeight.SemiBold) }
                Button(
                    onClick = {
                        val s = localStartMs; val e = localEndMs
                        if (s != null && e != null) onRangeSelected(s, e)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    shape = RoundedCornerShape(12.dp),
                    enabled = localStartMs != null && localEndMs != null,
                ) { Text("확인", fontWeight = FontWeight.SemiBold) }
            }
        }
    }
}

@Composable
private fun CalendarMonthGrid(
    year: Int,
    month: Int,
    startMs: Long?,
    endMs: Long?,
    onDayClick: (Long) -> Unit,
) {
    val firstDay = remember(year, month) {
        java.util.Calendar.getInstance().apply { set(year, month, 1) }
    }
    val startDow    = (firstDay.get(java.util.Calendar.DAY_OF_WEEK) - 1)
    val daysInMonth = firstDay.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
    val cells: List<Int?> = List(startDow) { null } + (1..daysInMonth).toList()
    val rows = cells.chunked(7) { w -> w + List(7 - w.size) { null } }

    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        rows.forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { day ->
                    if (day != null) {
                        CalendarDayCell(
                            day = day, year = year, month = month,
                            startMs = startMs, endMs = endMs,
                            onDayClick = onDayClick,
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        Spacer(Modifier.weight(1f).aspectRatio(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: Int, year: Int, month: Int,
    startMs: Long?, endMs: Long?,
    onDayClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val cal = remember(year, month, day) {
        java.util.Calendar.getInstance().apply {
            set(year, month, day, 0, 0, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
    }
    val dayMs     = cal.timeInMillis
    val isStart   = startMs?.let { calSameDay(dayMs, it) } ?: false
    val isEnd     = endMs?.let   { calSameDay(dayMs, it) } ?: false
    val isSingle  = isStart && isEnd
    val isInRange = startMs != null && endMs != null && dayMs > startMs && dayMs < endMs
    val isToday   = calSameDay(dayMs, System.currentTimeMillis())

    val textColor = when {
        isStart || isEnd -> Color.White
        isToday && !isInRange -> PrimaryGreen
        else -> TextPrimary
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .drawBehind {
                // 범위 밴드 (수평 띠)
                val bandH   = size.height * 0.68f
                val bandTop = (size.height - bandH) / 2f
                val rc      = PrimaryGreen.copy(alpha = 0.13f)
                when {
                    isInRange -> drawRect(rc, Offset(0f, bandTop), Size(size.width, bandH))
                    isStart && endMs != null && !isSingle ->
                        drawRect(rc, Offset(size.width / 2f, bandTop), Size(size.width / 2f, bandH))
                    isEnd && startMs != null && !isSingle ->
                        drawRect(rc, Offset(0f, bandTop), Size(size.width / 2f, bandH))
                }
            }
            .clickable { onDayClick(dayMs) },
        contentAlignment = Alignment.Center,
    ) {
        // 선택된 날 원형 배경
        if (isStart || isEnd) {
            Box(Modifier.size(36.dp).background(PrimaryGreen, CircleShape))
        }
        // 오늘 링
        if (isToday && !isStart && !isEnd) {
            Box(Modifier.size(34.dp).border(1.5.dp, PrimaryGreen, CircleShape))
        }
        Text(
            text = day.toString(),
            fontSize = 13.sp,
            color = textColor,
            fontWeight = if (isStart || isEnd || isToday) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}

private fun calSameDay(ms1: Long, ms2: Long): Boolean {
    val c1 = java.util.Calendar.getInstance().apply { timeInMillis = ms1 }
    val c2 = java.util.Calendar.getInstance().apply { timeInMillis = ms2 }
    return c1.get(java.util.Calendar.YEAR)       == c2.get(java.util.Calendar.YEAR) &&
           c1.get(java.util.Calendar.DAY_OF_YEAR) == c2.get(java.util.Calendar.DAY_OF_YEAR)
}
