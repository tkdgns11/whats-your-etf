package com.d102.wye.presentation.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.d102.wye.domain.state.EtfFilterState
import com.d102.wye.presentation.designsystem.EtfListItem
import com.d102.wye.presentation.designsystem.WyeSearchBar
import com.d102.wye.presentation.designsystem.WyeTopBar
import com.d102.wye.presentation.model.UiState
import com.d102.wye.presentation.theme.*

@Composable
fun ExploreScreen(
    onEtfClick: (ticker: String, riskLevel: Int) -> Unit,
    viewModel: ExploreViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val filterState by viewModel.filterState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showFilterDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState is UiState.Error) {
            snackbarHostState.showSnackbar((uiState as UiState.Error).message)
        }
    }

    if (showFilterDialog) {
        val resultCount = activeFilterCount(filterState)
        FilterDialog(
            filter = filterState,
            resultCount = resultCount,
            onFilterChanged = viewModel::onFilterChanged,
            onApply = { showFilterDialog = false },
            onDismiss = { showFilterDialog = false },
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { _ ->
        Column(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            WyeTopBar(title = "탐색")
            SearchRow(
                query = filterState.query,
                searchScope = filterState.searchScope,
                onQueryChanged = viewModel::onQueryChanged,
                onSearchScopeSelected = viewModel::onSearchScopeSelected,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )
            QuickFilterRow(
                filterState = filterState,
                onFilterIconClick = { showFilterDialog = true },
                onFilterChanged = viewModel::onFilterChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 4.dp),
            )
            SortRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
            )

            when (val state = uiState) {
                is UiState.Loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator() }

                is UiState.Success -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(state.data.filteredList, key = { it.ticker }) { etf ->
                        EtfListItem(
                            name = etf.name,
                            ticker = etf.ticker,
                            currentPrice = etf.currentPrice,
                            changeRate = etf.changeRate,
                            changeAmount = etf.changeAmount,
                            riskLevel = etf.riskLevel,
                            isLiked = etf.isLiked,
                            onLikeToggled = { viewModel.onLikeToggled(etf.ticker) },
                            onClick = { onEtfClick(etf.ticker, etf.riskLevel) },
                        )
                    }
                }

                is UiState.Error -> Unit
                UiState.Idle -> Unit
            }
        }
    }
}

// ── 검색 + 검색범위 드롭다운 ────────────────────────────────────

private data class SearchScopeOption(val label: String, val value: String?)

@Composable
private fun SearchRow(
    query: String,
    searchScope: String?,
    onQueryChanged: (String) -> Unit,
    onSearchScopeSelected: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scopeOptions = listOf(
        SearchScopeOption("전체", null),
        SearchScopeOption("ETF 종목명", "etf"),
        SearchScopeOption("주식명", "stock"),
    )
    val selectedLabel = scopeOptions.firstOrNull { it.value == searchScope }?.label ?: "전체"
    val placeholder = when (searchScope) {
        "etf"   -> "ETF 종목명 검색"
        "stock" -> "주식명 검색"
        else    -> "ETF 종목명 또는 주식명 검색"
    }
    var expanded by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier,
    ) {
        val scopeActive = searchScope != null
        Box {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (scopeActive) PrimaryGreen.copy(alpha = 0.08f) else SurfaceVariant)
                    .then(if (scopeActive) Modifier.border(1.dp, PrimaryGreen.copy(alpha = 0.5f), RoundedCornerShape(20.dp)) else Modifier)
                    .clickable { expanded = true }
                    .padding(horizontal = 14.dp, vertical = 9.dp),
            ) {
                Text(
                    text = selectedLabel,
                    fontSize = 13.sp,
                    fontWeight = if (scopeActive) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (scopeActive) PrimaryGreen else TextPrimary,
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = if (scopeActive) PrimaryGreen else TextSecondary,
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                containerColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 8.dp,
            ) {
                scopeOptions.forEach { option ->
                    val isSelected = option.value == searchScope
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option.label,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) PrimaryGreen else TextPrimary,
                            )
                        },
                        trailingIcon = {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = PrimaryGreen,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        },
                        onClick = {
                            onSearchScopeSelected(option.value)
                            expanded = false
                        },
                    )
                }
            }
        }

        WyeSearchBar(
            query = query,
            onQueryChange = onQueryChanged,
            placeholder = placeholder,
            modifier = Modifier.weight(1f),
        )
    }
}

// ── 빠른 필터 칩 ───────────────────────────────────────────────

private fun activeFilterCount(f: EtfFilterState): Int =
    f.riskLevels.size +
    f.themes.size +
    listOfNotNull(f.strategy, f.dividendRateRange, f.dividendCycle,
        f.peRange, f.pbRange, f.roeRange, f.expenseRatioRange, f.netAssetRange,
        f.hasDerivative?.let { "" },
        f.hasLeverage?.let { "" },
        f.hasInverse?.let { "" }).size

private fun buildActiveChips(f: EtfFilterState): List<Pair<String, EtfFilterState>> {
    val chips = mutableListOf<Pair<String, EtfFilterState>>()

    val riskLabels = mapOf(1 to "안정형", 2 to "안정추구형", 3 to "위험중립형", 4 to "적극투자형", 5 to "공격투자형")
    f.riskLevels.forEach { level ->
        riskLabels[level]?.let { label ->
            chips += label to f.copy(riskLevels = f.riskLevels - level)
        }
    }
    f.strategy?.let { chips += it to f.copy(strategy = null, themes = emptySet()) }
    f.themes.forEach { theme -> chips += theme to f.copy(themes = f.themes - theme) }

    val dividendLabels = mapOf("0-5" to "배당률 0~5%", "5-10" to "배당률 5~10%")
    f.dividendRateRange?.let { chips += (dividendLabels[it] ?: it) to f.copy(dividendRateRange = null) }

    val cycleLabels = mapOf("월" to "배당 월", "분기" to "배당 분기", "반기" to "배당 반기", "년" to "배당 연")
    f.dividendCycle?.let { chips += (cycleLabels[it] ?: it) to f.copy(dividendCycle = null) }

    f.hasDerivative?.let { v ->
        chips += "파생상품 ${if (v) "O" else "X"}" to f.copy(
            hasDerivative = null,
            hasLeverage = null,
            hasInverse = null,
        )
    }
    f.hasLeverage?.let { v -> chips += "레버리지 ${if (v) "O" else "X"}" to f.copy(hasLeverage = null) }
    f.hasInverse?.let { v -> chips += "인버스 ${if (v) "O" else "X"}" to f.copy(hasInverse = null) }

    val peLabels = mapOf("under10" to "PER 10 미만", "10-20" to "PER 10~20", "over20" to "PER 20 초과")
    f.peRange?.let { chips += (peLabels[it] ?: it) to f.copy(peRange = null) }

    val pbLabels = mapOf("under1" to "PBR 1 미만", "1-3" to "PBR 1~3", "over3" to "PBR 3 초과")
    f.pbRange?.let { chips += (pbLabels[it] ?: it) to f.copy(pbRange = null) }

    val roeLabels = mapOf("under5" to "ROE 5% 미만", "5-15" to "ROE 5~15%", "over15" to "ROE 15% 초과")
    f.roeRange?.let { chips += (roeLabels[it] ?: it) to f.copy(roeRange = null) }

    val expLabels = mapOf("under0.05" to "보수 0.05% 미만", "0.05-0.5" to "보수 0.05~0.5%", "over0.5" to "보수 0.5% 초과")
    f.expenseRatioRange?.let { chips += (expLabels[it] ?: it) to f.copy(expenseRatioRange = null) }

    val netLabels = mapOf("under100" to "순자산 100억 미만", "100-1000" to "순자산 100~1000억", "over1000" to "순자산 1000억 초과")
    f.netAssetRange?.let { chips += (netLabels[it] ?: it) to f.copy(netAssetRange = null) }

    return chips
}

@Composable
private fun QuickFilterRow(
    filterState: EtfFilterState,
    onFilterIconClick: () -> Unit,
    onFilterChanged: (EtfFilterState) -> Unit,
    modifier: Modifier = Modifier,
) {
    val filterCount = activeFilterCount(filterState)
    val activeChips = buildActiveChips(filterState)
    val hasNoFilters = activeChips.isEmpty()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.horizontalScroll(rememberScrollState()),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(SurfaceVariant)
                .clickable(onClick = onFilterIconClick),
        ) {
            Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = "상세 필터",
                tint = TextSecondary,
                modifier = Modifier.size(20.dp),
            )
        }

        if (hasNoFilters) {
            QuickChip("전체", true, onClick = {})
        } else {
            if (filterCount > 0) {
                Text(
                    text = "초기화",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onFilterChanged(EtfFilterState()) }
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                )
            }
            activeChips.forEach { (label, nextState) ->
                ActiveFilterChip(label = label, onRemove = { onFilterChanged(nextState) })
            }
        }
    }
}

@Composable
private fun QuickChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = label,
        color = if (selected) TextOnColored else TextPrimary,
        fontSize = 14.sp,
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) PrimaryGreen else SurfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp),
    )
}

@Composable
private fun ActiveFilterChip(label: String, onRemove: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(PrimaryGreen)
            .padding(start = 12.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
    ) {
        Text(
            text = label,
            color = TextOnColored,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
        )
        Spacer(Modifier.width(2.dp))
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "필터 제거",
            tint = TextOnColored,
            modifier = Modifier
                .size(16.dp)
                .clickable(onClick = onRemove),
        )
    }
}

// ── 정렬 ───────────────────────────────────────────────────────

@Composable
private fun SortRow(modifier: Modifier = Modifier) {
    val sortOptions = listOf("거래량 순", "등락률 순", "시가총액 순")
    var expanded by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf(sortOptions[0]) }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier
                    .clickable { expanded = true }
                    .padding(horizontal = 4.dp, vertical = 7.dp),
            ) {
                Text(
                    text = selected,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = TextSecondary,
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                containerColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 8.dp,
            ) {
                sortOptions.forEach { option ->
                    val isSelected = selected == option
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) PrimaryGreen else TextPrimary,
                            )
                        },
                        trailingIcon = {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = PrimaryGreen,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        },
                        onClick = { selected = option; expanded = false },
                    )
                }
            }
        }
    }
}
