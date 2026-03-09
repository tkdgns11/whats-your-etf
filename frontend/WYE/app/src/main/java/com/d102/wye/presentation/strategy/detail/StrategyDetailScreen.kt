package com.d102.wye.presentation.strategy.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.d102.wye.presentation.designsystem.WyePrimaryButton
import com.d102.wye.presentation.designsystem.WyeTopBar
import com.d102.wye.presentation.model.UiState
import com.d102.wye.presentation.strategy.detail.components.SummaryMetricsRow
import com.d102.wye.presentation.theme.BackGroundLightGreen2
import com.d102.wye.presentation.theme.PrimaryGreen

@Composable
fun StrategyDetailScreen(
    onBackClick: () -> Unit,
    viewModel: StrategyDetailViewModel = hiltViewModel()
) {
    val detailState by viewModel.detailState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(detailState) {
        if (detailState is UiState.Error) {
            snackbarHostState.showSnackbar(
                message = (detailState as UiState.Error).message
            )
        }
    }

    StrategyDetailScreenContent(
        detailState = detailState,
        snackbarHostState = snackbarHostState,
        onBackClick = onBackClick
    )
}

@Composable
private fun StrategyDetailScreenContent(
    detailState: UiState<StrategyDetailData>,
    snackbarHostState: SnackbarHostState,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackGroundLightGreen2)
    ) {
        WyeTopBar(
            title = (detailState as? UiState.Success)?.data?.title ?: "",
            onBackClick = onBackClick,
            backgroundColor = BackGroundLightGreen2
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            when (val state = detailState) {
                is UiState.Success -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        SummaryMetricsRow(state.data.summaryMetrics)
                        PerformanceSection(state.data.recentPerformance, isMain = true)
                        PerformanceSection(state.data.pastPerformance, isMain = false)
                        TimelineSection(state.data.timelines)
                        NewsSection(state.data.relatedNews)
                        WyePrimaryButton(
                            text = "닫기",
                            onClick = onBackClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                        )
                    }
                }

                is UiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        CircularProgressIndicator(
                            color = PrimaryGreen,
                            modifier = Modifier.align(Alignment.Center)
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