package com.d102.wye.presentation.strategy.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import com.d102.wye.presentation.designsystem.WyeTopBar
import com.d102.wye.presentation.model.UiState
import com.d102.wye.presentation.strategy.list.components.CompareButton
import com.d102.wye.presentation.strategy.list.components.EmptyRealAssetCard
import com.d102.wye.presentation.strategy.list.components.EmptySavedStrategyCard
import com.d102.wye.presentation.strategy.list.components.StrategyCard
import com.d102.wye.presentation.strategy.list.components.StrategyEmptyView
import com.d102.wye.presentation.theme.PrimaryGreen
import com.d102.wye.presentation.theme.TextSecondary

@Composable
fun StrategyScreen(
    onStrategyClick: (strategyId: Long) -> Unit,
    onCompareClick: () -> Unit,
    onCreateFirstStrategyClick: () -> Unit,
    viewModel: StrategyViewModel = hiltViewModel()
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

    StrategyScreenContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onStrategyClick = onStrategyClick,
        onCompareClick = onCompareClick,
        onCreateFirstStrategyClick = onCreateFirstStrategyClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StrategyScreenContent(
    uiState: UiState<StrategyListData>,
    snackbarHostState: SnackbarHostState,
    onStrategyClick: (strategyId: Long) -> Unit,
    onCompareClick: () -> Unit,
    onCreateFirstStrategyClick: () -> Unit
) {
    val listData = (uiState as? UiState.Success)?.data
    val isCompletelyEmpty = listData?.realAsset == null && listData?.strategies.isNullOrEmpty()

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            WyeTopBar(title = "나의 전략")
        },
        containerColor = Color.White,
        floatingActionButton = {
            if (uiState is UiState.Success && !isCompletelyEmpty) {
                CompareButton(onClick = { onCompareClick() })
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState) {
                is UiState.Loading -> {
                    CircularProgressIndicator(
                        color = PrimaryGreen,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is UiState.Success -> {
                    if (isCompletelyEmpty) {
                        // 1. 아예 처음 온 유저
                        StrategyEmptyView(
                            onCreateClick = onCreateFirstStrategyClick
                        )
                    } else {
                        // 2. 자산이나 전략이 있는 유저
                        StrategyListView(
                            data = uiState.data,
                            onItemClick = onStrategyClick
                        )
                    }
                }

                is UiState.Error -> {
                    // 에러 뷰 처리
                }

                UiState.Idle -> Unit
            }
        }
    }
}


// ─────────────────────────────────────────
// 2. 리스트 화면
// ─────────────────────────────────────────
@Composable
private fun StrategyListView(
    data: StrategyListData,
    onItemClick: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 60.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // --- 1. 내 실제 자산 섹션 ---
        item {
            Column {
                Text(
                    text = "나의 실제 자산",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))

                if (data.realAsset == null) {
                    EmptyRealAssetCard()
                } else {
                    // 실제 자산 카드 렌더링
                    StrategyCard(
                        strategy = data.realAsset,
                        onClick = onItemClick
                    )
                }
            }
        }

        // --- 2. 저장된 실험 전략 섹션 ---
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "저장된 실험 전략",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextSecondary
                )
                Text(
                    text = "총 ${data.strategies.size}개",
                    style = MaterialTheme.typography.labelLarge,
                    color = PrimaryGreen
                )
            }
        }

        if (data.strategies.isEmpty()) {
            item { EmptySavedStrategyCard() }
        } else {
            items(data.strategies) { strategy ->
                StrategyCard(
                    strategy = strategy,
                    onClick = onItemClick
                )
            }
        }
    }
}