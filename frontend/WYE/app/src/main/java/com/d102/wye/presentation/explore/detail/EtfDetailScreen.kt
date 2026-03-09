package com.d102.wye.presentation.explore.detail

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.d102.wye.presentation.designsystem.WyeTabs
import com.d102.wye.presentation.designsystem.WyeTopBar
import com.d102.wye.presentation.explore.detail.components.ClusterTab
import com.d102.wye.presentation.explore.detail.components.EtfDetailInfoTab
import com.d102.wye.presentation.model.UiState
import com.d102.wye.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EtfDetailScreen(
    onBack: () -> Unit,
    onStockClick: (String) -> Unit = {},
    viewModel: EtfDetailViewModel = hiltViewModel(),
) {
    val detailState by viewModel.detailState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("클러스터", "ETF 상세보기")

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            WyeTopBar(title = "ETF 상세", onBackClick = onBack)
        },
    ) { innerPadding ->
        Column(modifier = Modifier.padding(top = innerPadding.calculateTopPadding())) {
            WyeTabs(
                titles = tabs,
                selectedIndex = selectedTab,
                onTabSelected = { selectedTab = it },
                containerColor = Background,
            )

            when (val state = detailState) {
                is UiState.Loading -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator() }

                is UiState.Success -> {
                    when (selectedTab) {
                        0 -> ClusterTab(detail = state.data, viewModel = viewModel, onStockClick = onStockClick)
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
