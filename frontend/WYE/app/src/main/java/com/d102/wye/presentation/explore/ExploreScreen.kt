package com.d102.wye.presentation.explore

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.navigation.NavHostController

/**
 * =====================================================================
 * Screen 작성 템플릿
 * =====================================================================
 *
 * 사용법:
 * 1. 이 파일을 복사해서 presentation/[feature]/[FeatureName]Screen.kt 에 붙여넣기
 * 2. Explore → 실제 기능 이름으로 전체 교체 (예: Login, Simulation 등)
 * 3. TODO 주석 위치에 실제 UI 구현
 *
 * 구조:
 * - Screen        : NavController로부터 호출되는 진입점, ViewModel 주입
 * - ScreenContent : 순수 Composable (ViewModel 의존 없음, 프리뷰 가능)
 *
 * TopBar 포함 여부:
 * - Bottom Nav 탭 화면 (Main, Explore 등) → TopBar 없거나 커스텀
 * - 상세/서브 화면 (EtfDetail, SimulationSetup 등) → 뒤로가기 TopBar 포함
 * =====================================================================
 */

// ─────────────────────────────────────────────────────────────────────
// Screen (진입점 - ViewModel 주입)
// ─────────────────────────────────────────────────────────────────────

@Composable
fun ExploreScreen(
    navController: NavHostController,
    viewModel: ExploreViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // 에러 메시지 Snackbar로 표시
    LaunchedEffect(uiState) {
        if (uiState is ExploreUiState.Error) {
            snackbarHostState.showSnackbar(
                message = (uiState as ExploreUiState.Error).message
            )
        }
    }

    ExploreScreenContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onBackClick = { navController.popBackStack() },
        // TODO: 실제 이벤트 핸들러 연결
        // onSomethingClick = { viewModel.onSomethingClick() }
    )
}

// ─────────────────────────────────────────────────────────────────────
// ScreenContent (순수 UI - 프리뷰 가능)
// ─────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExploreScreenContent(
    uiState: ExploreUiState,
    snackbarHostState: SnackbarHostState,
    onBackClick: () -> Unit,
    // TODO: 이벤트 파라미터 추가
    // onSomethingClick: () -> Unit = {}
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            // Bottom Nav 탭 화면이면 TopBar 제거 또는 커스텀
            // 뒤로가기가 필요한 서브 화면이면 아래처럼 사용
            TopAppBar(
                title = { Text("탐색") },   // TODO: 화면 타이틀 교체
                navigationIcon = {
                    // Bottom Nav 탭 화면이면 이 블록 제거
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState) {
                is ExploreUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is ExploreUiState.Success -> {
                    // TODO: 실제 UI 구현
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(text = "성공: ${uiState.data}")
                    }
                }

                is ExploreUiState.Error -> {
                    // 에러는 Snackbar로 처리하므로 여기서는 빈 화면 or 재시도 버튼
                    // TODO: 필요 시 에러 UI 추가
                }

                ExploreUiState.Idle -> {
                    // 초기 상태 — 보통 빈 화면
                }
            }
        }
    }
}