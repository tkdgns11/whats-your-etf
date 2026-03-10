package com.d102.wye.presentation.strategy.compare

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.d102.wye.presentation.designsystem.WyePrimaryButton
import com.d102.wye.presentation.designsystem.WyeTopBar
import com.d102.wye.presentation.model.UiState
import com.d102.wye.presentation.strategy.compare.components.CompareTipSection
import com.d102.wye.presentation.strategy.compare.components.EmptyChartSection
import com.d102.wye.presentation.strategy.compare.components.EmptyTableSection
import com.d102.wye.presentation.strategy.compare.components.StrategySelectionRow
import com.d102.wye.presentation.strategy.detail.components.RoundedSurface
import com.d102.wye.presentation.theme.BackGroundLightGreen2
import com.d102.wye.presentation.theme.PrimaryGreen
import com.d102.wye.presentation.theme.TextPrimary

@Composable
fun StrategyCompareScreen(
    onBackClick: () -> Unit,
    viewModel: StrategyCompareViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        if (uiState is UiState.Error) {
            snackbarHostState.showSnackbar(
                message = (uiState as UiState.Error).message
            )
        }
    }

    StrategyCompareScreenContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onBackClick = onBackClick,
        onToggleSelection = viewModel::toggleSelection
    )
}

@Composable
private fun StrategyCompareScreenContent(
    uiState: UiState<CompareData>,
    snackbarHostState: SnackbarHostState,
    onBackClick: () -> Unit,
    onToggleSelection: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        WyeTopBar(
            title = "전략 비교하기",
            backgroundColor = BackGroundLightGreen2,
            onBackClick = onBackClick
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackGroundLightGreen2)
        ) {
            when (val state = uiState) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryGreen)
                    }
                }

                is UiState.Success -> {
                    val selectedItems = state.data.strategyList.filter { it.isSelected }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp)
                    ) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "내 포트폴리오",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextPrimary,
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        RoundedSurface(horizontalPaddingValue = 0.dp) {
                            Column {
                                state.data.strategyList.forEach { item ->
                                    StrategySelectionRow(
                                        item = item,
                                        onClick = { onToggleSelection(item.id) }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Crossfade(
                            targetState = selectedItems.isEmpty(),
                            label = "ChartCrossfade"
                        ) { isEmpty ->
                            if (isEmpty) EmptyChartSection()
                            else Text("차트 활성화 UI", modifier = Modifier.padding(20.dp))
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "상세 비교",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextPrimary,
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Crossfade(
                            targetState = selectedItems.isEmpty(),
                            label = "TableCrossfade"
                        ) { isEmpty ->
                            if (isEmpty) EmptyTableSection()
                            else Text("테이블 활성화 UI", modifier = Modifier.padding(20.dp))
                        }

                        if (selectedItems.isEmpty()) {
                            Spacer(modifier = Modifier.height(24.dp))
                            CompareTipSection()
                        }

                        Spacer(modifier = Modifier.weight(1f))
                        WyePrimaryButton(
                            text = "닫기",
                            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                            onClick = { onBackClick() }
                        )
                    }
                }

                else -> Unit
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}