package com.d102.wye.presentation.strategy

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.d102.wye.R
import com.d102.wye.presentation.designsystem.WyePrimaryButton
import com.d102.wye.presentation.designsystem.WyeTopBar
import com.d102.wye.presentation.model.UiState
import com.d102.wye.presentation.theme.PrimaryGreen
import com.d102.wye.presentation.theme.SurfaceVariant
import com.d102.wye.presentation.theme.TextPrimary
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            WyeTopBar(title = "나의 전략")
        },
        containerColor = Color.White,
        floatingActionButton = {
            if (uiState is UiState.Success && !isCompletelyEmpty) {
                ExtendedFloatingActionButton(
                    onClick = onCompareClick,
                    containerColor = PrimaryGreen,
                    contentColor = Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(4.dp),
                    shape = RoundedCornerShape(100.dp) // 완전히 둥글게
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_compare),
                        contentDescription = "비교",
                        tint = PrimaryGreen
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(text = "전략 비교하기", style = MaterialTheme.typography.titleSmall)
                }
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
                        // 1. 아예 처음 온 유저 (왼쪽 시안)
                        StrategyEmptyView(
                            onCreateClick = onCreateFirstStrategyClick
                        )
                    } else {
                        // 2. 자산이나 전략이 있는 유저 (가운데 & 오른쪽 시안)
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
// 2. 리스트 화면 (가운데 & 오른쪽 시안)
// ─────────────────────────────────────────
@Composable
private fun StrategyListView(
    data: StrategyListData,
    onItemClick: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 24.dp,
            end = 24.dp,
            top = 24.dp,
            bottom = 100.dp
        ), // 하단 FAB 겹침 방지 여백
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // --- 1. 내 실제 자산 섹션 ---
        item {
            Column {
                Text(
                    text = "나의 실제 자산",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (data.realAsset == null) {
                    // 자산 연결 안됨 (가운데 시안의 점선 박스)
                    EmptyRealAssetCard()
                } else {
                    // 자산 연결됨 (오른쪽 시안의 초록 카드)
                    // TODO: StrategyCard 컴포넌트에 data.realAsset 데이터를 넘겨서 그려줍니다.
                    Text("실제 자산 카드 UI")
                }
            }
        }

        // --- 2. 저장된 실험 전략 섹션 ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "저장된 실험 전략",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                Text(
                    text = "총 ${data.strategies.size}개",
                    style = MaterialTheme.typography.labelMedium,
                    color = PrimaryGreen
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (data.strategies.isEmpty()) {
            item {
                // 전략이 하나도 없을 때 (오른쪽 시안의 하단)
                EmptySavedStrategyCard()
            }
        } else {
            items(data.strategies) { strategy ->
                // TODO: 시안의 하얀색 전략 카드 컴포넌트 재사용
                Text("전략 카드 UI - ${strategy.title}")
            }
        }
    }
}

// 자산이 없을 때 점선 빈 카드
@Composable
private fun EmptyRealAssetCard() {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        modifier = Modifier.fillMaxWidth()
        // TODO: 점선 보더라인 적용 (Modifier.drawBehind 로 커스텀 점선 구현 필요)
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_shining),
                contentDescription = null,
                tint = PrimaryGreen
            ) // 링크 아이콘
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "연결된 실제 ETF 자산이 없습니다.",
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "∞ 내 자산 연결하기",
                style = MaterialTheme.typography.bodySmall,
                color = PrimaryGreen
            )
        }
    }
}

// 실험 전략이 없을 때 빈 카드
@Composable
private fun EmptySavedStrategyCard() {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color(0xFFF3F4F6), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_nav_explore),
                    contentDescription = null,
                    tint = PrimaryGreen,
                    modifier = Modifier.size(32.dp)
                ) // 상승 화살표 아이콘
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "상상 속의 전략, 현실이 될까요?",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "다양한 ETF를 조합해 미래 수익률을 미리 확인하고\n나만의 필승 전략을 설계해 보세요.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            WyePrimaryButton(
                text = "시뮬레이션 시작하기",
                onClick = { /* TODO */ },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}