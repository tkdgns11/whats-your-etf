package com.d102.wye.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.d102.wye.presentation.designsystem.WyeTopBar
import com.d102.wye.presentation.model.NewsUiModel
import com.d102.wye.presentation.model.UiState

@Composable
fun HomeScreen(
    onNewsClick: (newsId: Long) -> Unit,
    onEtfClick: (ticker: String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
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

    HomeScreenContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onNewsClick = onNewsClick,
        onEtfClick = onEtfClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreenContent(
    uiState: UiState<HomeData>,
    snackbarHostState: SnackbarHostState,
    onNewsClick: (newsId: Long) -> Unit,
    onEtfClick: (ticker: String) -> Unit
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            WyeTopBar(title = "홈")
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
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Top10ChartSection(
                                etfList = uiState.data.top10Etfs,
                                onEtfClick = onEtfClick
                            )
                        }

                        item {
                            PortfolioReturnCard(returnRate = uiState.data.portfolioReturnRate)
                        }

                        item { Text(text = "ETF 시장 뉴스") }

                        items(
                            items = uiState.data.newsList,
                            key = { it.id }
                        ) { news ->
                            NewsItem(
                                news = news,
                                onClick = { onNewsClick(news.id) }
                            )
                        }
                    }
                }

                is UiState.Error -> Unit // Snackbar로 처리

                UiState.Idle -> Unit
            }
        }
    }
}

@Composable
private fun Top10ChartSection(
    etfList: List<Top10EtfUiModel>,
    onEtfClick: (ticker: String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "일일 매매량 TOP 10")
            Spacer(modifier = Modifier.height(8.dp))
            // TODO: 차트 라이브러리 연동
            Text(text = "차트 영역 (${etfList.size}개)")
        }
    }
}

@Composable
private fun PortfolioReturnCard(returnRate: Double?) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "내 포트폴리오 수익률")
            Spacer(modifier = Modifier.height(8.dp))
            if (returnRate != null) {
                // TODO: 수익률 추이 그래프
                Text(text = "${returnRate}%")
            } else {
                Text(text = "포트폴리오를 설정해주세요")
            }
        }
    }
}

@Composable
private fun NewsItem(
    news: NewsUiModel,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = news.title)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = news.summary)
            Text(text = news.publishedAt)
        }
    }
}