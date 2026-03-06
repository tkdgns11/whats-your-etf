package com.d102.wye.presentation.simulation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import com.d102.wye.presentation.simulation.entry.SimulationEntryData
import com.d102.wye.presentation.simulation.entry.SimulationViewModel

@Composable
fun SimulationScreen(
    onStartClick: () -> Unit,           // 시뮬레이션 설정 화면으로 이동
    onBundleClick: (bundleId: Int) -> Unit, // 꾸러미 선택
    viewModel: SimulationViewModel = hiltViewModel()
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

    SimulationScreenContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onStartClick = onStartClick,
        onBundleClick = onBundleClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SimulationScreenContent(
    uiState: UiState<SimulationEntryData>,
    snackbarHostState: SnackbarHostState,
    onStartClick: () -> Unit,
    onBundleClick: (bundleId: Int) -> Unit
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            WyeTopBar(title = "시뮬레이션")
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
                    // TODO: 안내 페이지 + 사전 구성 꾸러미 UI 구현
                    Text(
                        text = "시뮬레이션 진입 화면",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is UiState.Error -> Unit

                UiState.Idle -> Unit
            }
        }
    }
}