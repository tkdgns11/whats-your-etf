package com.d102.wye.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.d102.wye.R
import com.d102.wye.presentation.designsystem.WyeTabs
import com.d102.wye.presentation.designsystem.WyeTopBar
import com.d102.wye.presentation.home.components.HomePortfolioTab
import com.d102.wye.presentation.home.components.HomeTop10Tab
import com.d102.wye.presentation.model.UiState
import com.d102.wye.presentation.theme.PrimaryGreen
import com.d102.wye.presentation.theme.TextSecondary

@Composable
fun HomeScreen(
    onNewsClick: (newsId: Long) -> Unit,
    onEtfClick: (ticker: String) -> Unit,
    onNewsMoreClick: () -> Unit = {},
    onPortfolioMoreClick: () -> Unit = {},
    onBookmarkClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
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
        onEtfClick = onEtfClick,
        onNewsMoreClick = onNewsMoreClick,
        onPortfolioMoreClick = onPortfolioMoreClick,
        onBookmarkClick = onBookmarkClick,
        onNotificationClick = onNotificationClick
    )
}

@Composable
private fun HomeScreenContent(
    uiState: UiState<HomeData>,
    snackbarHostState: SnackbarHostState,
    onNewsClick: (newsId: Long) -> Unit,
    onEtfClick: (ticker: String) -> Unit,
    onNewsMoreClick: () -> Unit,
    onPortfolioMoreClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    onNotificationClick: () -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("거래량 TOP 10", "내 포트폴리오")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            WyeTopBar(
                title = "홈",
                actions = {
                    IconButton(onClick = onBookmarkClick) {
                        Icon(
                            painter = painterResource(R.drawable.ic_star),
                            contentDescription = "북마크",
                            tint = PrimaryGreen,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    IconButton(onClick = onNotificationClick) {
                        Icon(
                            imageVector = Icons.Filled.Notifications,
                            contentDescription = "알림",
                            tint = PrimaryGreen
                        )
                    }
                }
            )

            WyeTabs(
                titles = tabTitles,
                selectedIndex = selectedTabIndex,
                onTabSelected = { selectedTabIndex = it }
            )

            when (uiState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is UiState.Success -> {
                    when (selectedTabIndex) {
                        0 -> HomeTop10Tab(
                            top10Etfs = uiState.data.top10Etfs,
                            newsList = uiState.data.newsList,
                            onEtfClick = onEtfClick,
                            onNewsClick = onNewsClick,
                            onNewsMoreClick = onNewsMoreClick
                        )

                        1 -> HomePortfolioTab(
                            portfolio = uiState.data.portfolio,
                            newsList = uiState.data.newsList,
                            onNewsClick = onNewsClick,
                            onNewsMoreClick = onNewsMoreClick,
                            onPortfolioMoreClick = onPortfolioMoreClick
                        )
                    }
                }

                is UiState.Error -> Unit

                UiState.Idle -> Unit
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
