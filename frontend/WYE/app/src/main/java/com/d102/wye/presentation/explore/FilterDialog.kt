package com.d102.wye.presentation.explore

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.d102.wye.domain.state.EtfFilterState
import com.d102.wye.presentation.designsystem.WyePrimaryButton
import com.d102.wye.presentation.designsystem.WyeSelectableChip
import com.d102.wye.presentation.theme.TextPrimary
import com.d102.wye.presentation.theme.TextSecondary
import com.d102.wye.presentation.theme.WYETheme

@Composable
fun FilterDialog(
    filter: EtfFilterState,
    resultCount: Int,
    onFilterChanged: (EtfFilterState) -> Unit,
    onApply: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(horizontal = 16.dp),
            shape = MaterialTheme.shapes.large,
        ) {
            Column {
                // 헤더
                FilterDialogHeader(
                    onReset = { onFilterChanged(EtfFilterState()) },
                    onDismiss = onDismiss
                )

                // 필터 섹션들 (스크롤)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    Spacer(Modifier.height(4.dp))

                    // 위험 분류
                    FilterSection(title = "위험 분류") {
                        val riskItems = listOf(
                            Triple(1, "안정형", "예금 보호 확정\n금리 추구형"),
                            Triple(2, "안정추구형", "투자원금 손실\n최소화"),
                            Triple(3, "위험중립형", "투자에 따른\n수익·손실 인지"),
                            Triple(4, "적극투자형", "높은 수익 위해\n위험 감수"),
                            Triple(5, "공격투자형", "원금 손실 감수\n고위험 투자"),
                        )
                        // 1행: 안정형 안정추구형 위험중립형
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            riskItems.take(3).forEach { (level, label, desc) ->
                                WyeSelectableChip(
                                    label = label,
                                    description = desc,
                                    selected = level in filter.riskLevels,
                                    onClick = {
                                        onFilterChanged(
                                            filter.copy(
                                                riskLevels = filter.riskLevels.toggle(
                                                    level
                                                )
                                            )
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                        // 2행: 적극투자형 공격투자형
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            riskItems.drop(3).forEach { (level, label, desc) ->
                                WyeSelectableChip(
                                    label = label,
                                    description = desc,
                                    selected = level in filter.riskLevels,
                                    onClick = {
                                        onFilterChanged(
                                            filter.copy(
                                                riskLevels = filter.riskLevels.toggle(
                                                    level
                                                )
                                            )
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            Spacer(Modifier.weight(1f))
                        }
                    }

                    // 투자 전략
                    FilterSection(title = "투자 전략") {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("시장 대표", "테마형", "배당형", "채권형").forEach { s ->
                                WyeSelectableChip(
                                    label = s,
                                    selected = filter.strategy == s,
                                    onClick = { onFilterChanged(filter.copy(strategy = if (filter.strategy == s) null else s)) },
                                )
                            }
                        }
                    }

                    // 투자 테마 (테마형 선택 시 활성화)
                    ThemeFilterSection(
                        filter = filter,
                        onFilterChanged = onFilterChanged,
                    )

                    // 배당률
                    FilterSection(title = "배당률") {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                "0 - 5%" to "0-5",
                                "5 - 10%" to "5-10"
                            ).forEach { (label, value) ->
                                WyeSelectableChip(
                                    label = label,
                                    selected = filter.dividendRateRange == value,
                                    onClick = { onFilterChanged(filter.copy(dividendRateRange = if (filter.dividendRateRange == value) null else value)) },
                                )
                            }
                        }
                    }

                    // 배당주기
                    FilterSection(title = "배당주기") {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("월", "반기", "분기", "년").forEach { cycle ->
                                WyeSelectableChip(
                                    label = cycle,
                                    selected = filter.dividendCycle == cycle,
                                    onClick = { onFilterChanged(filter.copy(dividendCycle = if (filter.dividendCycle == cycle) null else cycle)) },
                                )
                            }
                        }
                    }

                    // 파생상품
                    FilterSection(title = "파생상품") {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            WyeSelectableChip(
                                label = "O",
                                selected = filter.hasDerivative == true,
                                onClick = { onFilterChanged(filter.copy(hasDerivative = if (filter.hasDerivative == true) null else true)) }
                            )
                            WyeSelectableChip(
                                label = "X",
                                selected = filter.hasDerivative == false,
                                onClick = { onFilterChanged(filter.copy(hasDerivative = if (filter.hasDerivative == false) null else false)) })
                        }
                    }

                    // 레버리지 (파생상품 O 선택 시 활성화)
                    LockedFilterSection(
                        title = "레버리지",
                        locked = filter.hasDerivative != true,
                        selected = filter.hasLeverage,
                        onChanged = { onFilterChanged(filter.copy(hasLeverage = it)) },
                    )

                    // 인버스 (파생상품 O 선택 시 활성화)
                    LockedFilterSection(
                        title = "인버스",
                        locked = filter.hasDerivative != true,
                        selected = filter.hasInverse,
                        onChanged = { onFilterChanged(filter.copy(hasInverse = it)) },
                    )

                    // P/E
                    FilterSection(title = "P/E") {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                "10배 이하" to "under10",
                                "10 - 20배" to "10-20",
                                "20배 이상" to "over20"
                            ).forEach { (label, value) ->
                                WyeSelectableChip(
                                    label = label,
                                    selected = filter.peRange == value,
                                    onClick = { onFilterChanged(filter.copy(peRange = if (filter.peRange == value) null else value)) })
                            }
                        }
                    }

                    // P/B
                    FilterSection(title = "P/B") {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                "1배 미만" to "under1",
                                "1 - 3배" to "1-3",
                                "3배 이상" to "over3"
                            ).forEach { (label, value) ->
                                WyeSelectableChip(
                                    label = label,
                                    selected = filter.pbRange == value,
                                    onClick = { onFilterChanged(filter.copy(pbRange = if (filter.pbRange == value) null else value)) })
                            }
                        }
                    }

                    // ROE
                    FilterSection(title = "ROE") {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                "5% 미만" to "under5",
                                "5 - 15%" to "5-15",
                                "15% 이상" to "over15"
                            ).forEach { (label, value) ->
                                WyeSelectableChip(
                                    label = label,
                                    selected = filter.roeRange == value,
                                    onClick = { onFilterChanged(filter.copy(roeRange = if (filter.roeRange == value) null else value)) })
                            }
                        }
                    }

                    // 운용보수
                    FilterSection(title = "운용보수(수수료)") {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                "0.05% 미만" to "under0.05",
                                "0.05 - 0.5%" to "0.05-0.5",
                                "0.5% 이상" to "over0.5"
                            ).forEach { (label, value) ->
                                WyeSelectableChip(
                                    label = label,
                                    selected = filter.expenseRatioRange == value,
                                    onClick = { onFilterChanged(filter.copy(expenseRatioRange = if (filter.expenseRatioRange == value) null else value)) })
                            }
                        }
                    }

                    // 순자산액
                    FilterSection(title = "순자산액") {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                "100억 미만" to "under100",
                                "100 - 1000억" to "100-1000",
                                "1000억 이상" to "over1000"
                            ).forEach { (label, value) ->
                                WyeSelectableChip(
                                    label = label,
                                    selected = filter.netAssetRange == value,
                                    onClick = { onFilterChanged(filter.copy(netAssetRange = if (filter.netAssetRange == value) null else value)) })
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                }

                // 하단 버튼
                WyePrimaryButton(
                    text = "${resultCount}개의 결과 보기",
                    onClick = onApply,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                )
            }
        }
    }
}

@Composable
private fun FilterDialogHeader(onReset: () -> Unit, onDismiss: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(onClick = onReset) {
            Text("초기화", color = TextSecondary, fontSize = 14.sp)
        }
        Text(
            text = "상세 필터",
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
        )
        IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "닫기")
        }
    }
}

@Composable
private fun ThemeFilterSection(
    filter: EtfFilterState,
    onFilterChanged: (EtfFilterState) -> Unit,
) {
    val enabled = filter.strategy == "테마형"
    var expanded by remember { mutableStateOf(false) }
    val themes = listOf(
        "전자 / IT", "바이오 / 의약", "에너지 / 유틸리티",
        "자동차", "화학 / 소재", "철강 / 금속",
        "식품 / 음료", "금융", "건설",
        "운송", "유통 / 소매", "통신 / 미디어", "기타",
    )

    LaunchedEffect(enabled) {
        if (enabled) expanded = true else expanded = false
    }

    AnimatedVisibility(
        visible = enabled,
        enter = expandVertically(),
        exit = shrinkVertically(),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                "투자 테마",
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = TextPrimary,
                modifier = Modifier.fillMaxWidth(),
            )
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    themes.forEach { theme ->
                        WyeSelectableChip(
                            label = theme,
                            selected = theme in filter.themes,
                            onClick = {
                                onFilterChanged(
                                    filter.copy(
                                        themes = filter.themes.toggle(
                                            theme
                                        )
                                    )
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextPrimary)
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (expanded) "접기" else "펼치기",
                tint = TextSecondary,
            )
        }
        if (expanded) {
            content()
        }
    }
}

@Composable
private fun LockedFilterSection(
    title: String,
    locked: Boolean,
    selected: Boolean?,
    onChanged: (Boolean?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(locked) {
        if (!locked) expanded = true else expanded = false
    }

    AnimatedVisibility(
        visible = !locked,
        enter = expandVertically(),
        exit = shrinkVertically(),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = TextPrimary,
                modifier = Modifier.fillMaxWidth(),
            )
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    WyeSelectableChip(
                        label = "O",
                        selected = selected == true,
                        onClick = { onChanged(if (selected == true) null else true) })
                    WyeSelectableChip(
                        label = "X",
                        selected = selected == false,
                        onClick = { onChanged(if (selected == false) null else false) })
                }
            }
        }
    }
}

// Set 토글 헬퍼
private fun <T> Set<T>.toggle(item: T): Set<T> = if (item in this) this - item else this + item

@Preview(showBackground = true, widthDp = 400, heightDp = 900)
@Composable
private fun FilterDialogPreview() {
    val filter = EtfFilterState(
        riskLevels = setOf(1, 3),
        strategy = "테마형",
        themes = setOf("전자 / IT", "바이오 / 의약"),
        dividendCycle = "월",
        hasDerivative = false,
    )
    val riskItems = listOf(
        Triple(1, "안정형", "예금 보호 확정\n금리 추구형"),
        Triple(2, "안정추구형", "투자원금 손실\n최소화"),
        Triple(3, "위험중립형", "투자에 따른\n수익·손실 인지"),
        Triple(4, "적극투자형", "높은 수익 위해\n위험 감수"),
        Triple(5, "공격투자형", "원금 손실 감수\n고위험 투자"),
    )
    WYETheme {
        Surface(modifier = Modifier.fillMaxSize(), shape = MaterialTheme.shapes.large) {
            Column {
                FilterDialogHeader(onReset = {}, onDismiss = {})
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    Spacer(Modifier.height(4.dp))
                    FilterSection(title = "위험 분류") {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            riskItems.take(3).forEach { (level, label, desc) ->
                                WyeSelectableChip(
                                    label = label,
                                    selected = level in filter.riskLevels,
                                    onClick = {},
                                    modifier = Modifier.weight(1f),
                                    description = desc
                                )
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            riskItems.drop(3).forEach { (level, label, desc) ->
                                WyeSelectableChip(
                                    label = label,
                                    selected = level in filter.riskLevels,
                                    onClick = {},
                                    modifier = Modifier.weight(1f),
                                    description = desc
                                )
                            }
                            Spacer(Modifier.weight(1f))
                        }
                    }
                    FilterSection(title = "투자 전략") {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("시장 대표", "테마형", "배당형", "채권형").forEach { s ->
                                WyeSelectableChip(
                                    label = s,
                                    selected = filter.strategy == s,
                                    onClick = {})
                            }
                        }
                    }
                    FilterSection(title = "투자 테마") {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                "전자 / IT",
                                "바이오 / 의약",
                                "에너지 / 유틸리티",
                                "자동차",
                                "화학 / 소재",
                                "철강 / 금속",
                                "식품 / 음료",
                                "금융",
                                "건설",
                                "운송",
                                "유통 / 소매",
                                "통신 / 미디어",
                                "기타"
                            ).forEach { t ->
                                WyeSelectableChip(
                                    label = t,
                                    selected = t in filter.themes,
                                    onClick = {})
                            }
                        }
                    }
                    FilterSection(title = "배당률") {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("0 - 5%", "5 - 10%").forEach { s ->
                                WyeSelectableChip(
                                    label = s,
                                    selected = false,
                                    onClick = {})
                            }
                        }
                    }
                    FilterSection(title = "배당주기") {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("월", "반기", "분기", "년").forEach { s ->
                                WyeSelectableChip(
                                    label = s,
                                    selected = filter.dividendCycle == s,
                                    onClick = {})
                            }
                        }
                    }
                    FilterSection(title = "파생상품") {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            WyeSelectableChip(
                                label = "O",
                                selected = filter.hasDerivative == true,
                                onClick = {})
                            WyeSelectableChip(
                                label = "X",
                                selected = filter.hasDerivative == false,
                                onClick = {})
                        }
                    }
                    LockedFilterSection(
                        title = "레버리지",
                        locked = filter.hasDerivative != true,
                        selected = filter.hasLeverage,
                        onChanged = {})
                    LockedFilterSection(
                        title = "인버스",
                        locked = filter.hasDerivative != true,
                        selected = filter.hasInverse,
                        onChanged = {})
                    FilterSection(title = "P/E") {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("10배 이하", "10 - 20배", "20배 이상").forEach { s ->
                                WyeSelectableChip(
                                    label = s,
                                    selected = false,
                                    onClick = {})
                            }
                        }
                    }
                    FilterSection(title = "P/B") {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("1배 미만", "1 - 3배", "3배 이상").forEach { s ->
                                WyeSelectableChip(
                                    label = s,
                                    selected = false,
                                    onClick = {})
                            }
                        }
                    }
                    FilterSection(title = "ROE") {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("5% 미만", "5 - 15%", "15% 이상").forEach { s ->
                                WyeSelectableChip(
                                    label = s,
                                    selected = false,
                                    onClick = {})
                            }
                        }
                    }
                    FilterSection(title = "운용보수(수수료)") {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                "0.05% 미만",
                                "0.05 - 0.5%",
                                "0.5% 이상"
                            ).forEach { s ->
                                WyeSelectableChip(
                                    label = s,
                                    selected = false,
                                    onClick = {})
                            }
                        }
                    }
                    FilterSection(title = "순자산액") {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                "100억 미만",
                                "100 - 1000억",
                                "1000억 이상"
                            ).forEach { s ->
                                WyeSelectableChip(
                                    label = s,
                                    selected = false,
                                    onClick = {})
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
                WyePrimaryButton(
                    text = "42개의 결과 보기",
                    onClick = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
        }
    }
}
