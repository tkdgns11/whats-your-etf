package com.d102.wye.presentation.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
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
import com.d102.wye.presentation.home.components.HomePortfolioTab
import com.d102.wye.presentation.home.components.HomeTop10Tab
import com.d102.wye.presentation.model.UiState
import com.d102.wye.presentation.theme.PrimaryGreen

@Composable
fun HomeScreen(
    onNewsClick: (newsId: Long) -> Unit,
    onEtfClick: (ticker: String) -> Unit,
    onNewsMoreClick: () -> Unit = {},
    onPortfolioMoreClick: () -> Unit = {},
    onBookmarkClick: () -> Unit = {},
    onAlertClick: () -> Unit = {},
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
        onAlertClick = onAlertClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreenContent(
    uiState: UiState<HomeData>,
    snackbarHostState: SnackbarHostState,
    onNewsClick: (newsId: Long) -> Unit,
    onEtfClick: (ticker: String) -> Unit,
    onNewsMoreClick: () -> Unit,
    onPortfolioMoreClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    onAlertClick: () -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("거래량 TOP 10", "내 포트폴리오")

    Box(
        modifier = Modifier
            .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            CenterAlignedTopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_logo),
                            contentDescription = "로고",
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .height(48.dp)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onBookmarkClick) {
                        Icon(
                            painter = painterResource(R.drawable.ic_star),
                            contentDescription = "북마크",
                            tint = PrimaryGreen,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    IconButton(onClick = onAlertClick) {
                        Icon(
                            imageVector = Icons.Filled.Notifications,
                            contentDescription = "알림",
                            tint = PrimaryGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
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
                    AnimatedContent(
                        targetState = selectedTabIndex,
                        transitionSpec = {
                            if (targetState > initialState) {
                                slideInHorizontally(
                                    animationSpec = tween(320),
                                    initialOffsetX = { fullWidth -> fullWidth / 3 }
                                ) + fadeIn(animationSpec = tween(320)) togetherWith
                                    slideOutHorizontally(
                                        animationSpec = tween(320),
                                        targetOffsetX = { fullWidth -> -fullWidth / 4 }
                                    ) + fadeOut(animationSpec = tween(220))
                            } else {
                                slideInHorizontally(
                                    animationSpec = tween(320),
                                    initialOffsetX = { fullWidth -> -fullWidth / 3 }
                                ) + fadeIn(animationSpec = tween(320)) togetherWith
                                    slideOutHorizontally(
                                        animationSpec = tween(320),
                                        targetOffsetX = { fullWidth -> fullWidth / 4 }
                                    ) + fadeOut(animationSpec = tween(220))
                            }
                        },
                        label = "HomeTabContent"
                    ) { tabIndex ->
                        when (tabIndex) {
                            0 -> HomeTop10Tab(
                                top10Etfs = uiState.data.top10Etfs,
                                newsList = uiState.data.newsList,
                                onEtfClick = onEtfClick,
                                onNewsClick = onNewsClick,
                                onNewsMoreClick = onNewsMoreClick
                            )

                            else -> HomePortfolioTab(
                                portfolios = uiState.data.portfolios,
                                newsList = uiState.data.newsList,
                                onNewsClick = onNewsClick,
                                onNewsMoreClick = onNewsMoreClick,
                                onPortfolioMoreClick = onPortfolioMoreClick
                            )
                        }
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
