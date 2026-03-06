package com.d102.wye.presentation.explore.detail

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.d102.wye.domain.model.EtfDetail
import com.d102.wye.presentation.explore.detail.component.ClusterTab
import com.d102.wye.presentation.explore.detail.component.EtfDetailInfoTab
import com.d102.wye.presentation.model.UiState
import com.d102.wye.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EtfDetailScreen(
    onBack: () -> Unit,
    viewModel: EtfDetailViewModel = hiltViewModel(),
) {
    val detailState by viewModel.detailState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("클러스터", "ETF 상세보기")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (detailState is UiState.Success)
                            (detailState as UiState.Success<EtfDetail>).data.ticker
                        else "ETF 상세",
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            // 탭 바
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Background,
                contentColor = PrimaryGreen,
            ) {
                tabs.forEachIndexed { idx, title ->
                    Tab(
                        selected = selectedTab == idx,
                        onClick = { selectedTab = idx },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == idx) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 15.sp,
                            )
                        },
                    )
                }
            }

            when (val state = detailState) {
                is UiState.Loading -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator() }

                is UiState.Success -> {
                    when (selectedTab) {
                        0 -> ClusterTab(detail = state.data, viewModel = viewModel)
                        1 -> EtfDetailInfoTab(detail = state.data, viewModel = viewModel)
                    }
                }

                is UiState.Error -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) { Text(state.message) }

                UiState.Idle -> Unit
            }
        }
    }
}
