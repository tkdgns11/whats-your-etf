package com.d102.wye.presentation.strategy

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.d102.wye.presentation.designsystem.WyeTopBar
import com.d102.wye.presentation.model.UiState

@Composable
fun StrategyScreen(
    onStrategyClick: (strategyId: Long) -> Unit,
    onCompareClick: () -> Unit,
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
        onCompareClick = onCompareClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StrategyScreenContent(
    uiState: UiState<StrategyListData>,
    snackbarHostState: SnackbarHostState,
    onStrategyClick: (strategyId: Long) -> Unit,
    onCompareClick: () -> Unit
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            WyeTopBar(title = "나의 전략")
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState) {
                is UiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is UiState.Success -> {
                    // TODO: 저장된 전략 목록 카드 UI 구현
                    Text(
                        text = "전략 ${uiState.data.strategies.size}개",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is UiState.Error -> Unit

                UiState.Idle -> Unit
            }
        }
    }
}