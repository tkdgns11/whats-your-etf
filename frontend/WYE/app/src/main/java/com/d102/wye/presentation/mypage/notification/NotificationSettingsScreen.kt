package com.d102.wye.presentation.mypage.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.d102.wye.presentation.designsystem.WyeTopBar
import com.d102.wye.presentation.model.UiState
import com.d102.wye.presentation.theme.Background
import com.d102.wye.presentation.theme.PrimaryGreen
import com.d102.wye.presentation.theme.SurfaceDivider
import com.d102.wye.presentation.theme.TextPrimary
import com.d102.wye.presentation.theme.TextSecondary

@Composable
fun NotificationSettingsScreen(
    onBackClick: () -> Unit,
    viewModel: NotificationSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        if (uiState is UiState.Error) {
            snackbarHostState.showSnackbar((uiState as UiState.Error).message)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            WyeTopBar(
                title = "알림 설정",
                onBackClick = onBackClick
            )

            when (val state = uiState) {
                is UiState.Loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }

                is UiState.Success -> NotificationSettingsContent(
                    state = state.data,
                    onAppNoticeChanged = viewModel::onAppNoticeChanged,
                    onEtfListingChanged = viewModel::onEtfListingChanged,
                    onEtfDelistingChanged = viewModel::onEtfDelistingChanged,
                    onPortfolioRebalancingChanged = viewModel::onPortfolioRebalancingChanged,
                    onPortfolioProfitChanged = viewModel::onPortfolioProfitChanged,
                    onNewsChanged = viewModel::onNewsChanged
                )

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

@Composable
private fun NotificationSettingsContent(
    state: NotificationSettingsUiState,
    onAppNoticeChanged: (Boolean) -> Unit,
    onEtfListingChanged: (Boolean) -> Unit,
    onEtfDelistingChanged: (Boolean) -> Unit,
    onPortfolioRebalancingChanged: (Boolean) -> Unit,
    onPortfolioProfitChanged: (Boolean) -> Unit,
    onNewsChanged: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp)
    ) {
        NotificationGroup(
            title = "앱",
            items = listOf(
                NotificationToggleItem(
                    title = "앱 알림",
                    description = "중요한 공지사항과 소식을 놓치지 마세요.",
                    checked = state.appNoticeEnabled,
                    onCheckedChange = onAppNoticeChanged
                )
            )
        )

        NotificationGroup(
            title = "ETF",
            items = listOf(
                NotificationToggleItem(
                    title = "ETF 상장 알림",
                    description = "관심 있는 ETF의 신규 상장 소식을 받아보세요.",
                    checked = state.etfListingEnabled,
                    onCheckedChange = onEtfListingChanged
                ),
                NotificationToggleItem(
                    title = "ETF 상장 폐지 알림",
                    description = "관심 있는 ETF의 상장 폐지 소식을 받아보세요.",
                    checked = state.etfDelistingEnabled,
                    onCheckedChange = onEtfDelistingChanged
                )
            )
        )

        NotificationGroup(
            title = "포트폴리오",
            items = listOf(
                NotificationToggleItem(
                    title = "포트폴리오 리밸런싱 알림",
                    description = "포트폴리오 리밸런싱 될 때 알려드려요.",
                    checked = state.portfolioRebalancingEnabled,
                    onCheckedChange = onPortfolioRebalancingChanged
                ),
                NotificationToggleItem(
                    title = "포트폴리오 수익률 알림",
                    description = "수익률에 큰 변화가 생기면 즉시 알려드려요.",
                    checked = state.portfolioProfitEnabled,
                    onCheckedChange = onPortfolioProfitChanged
                )
            )
        )

        NotificationGroup(
            title = "뉴스",
            items = listOf(
                NotificationToggleItem(
                    title = "뉴스 수신 알림",
                    description = "관심있는 ETF와 관련된 뉴스를 받아보세요.",
                    checked = state.newsEnabled,
                    onCheckedChange = onNewsChanged
                )
            )
        )
    }
}

@Composable
private fun NotificationGroup(
    title: String,
    items: List<NotificationToggleItem>
) {
    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Text(
            text = title,
            color = TextSecondary.copy(alpha = 0.55f),
            style = MaterialTheme.typography.labelSmall
        )

        items.forEach { item ->
            NotificationToggleRow(item = item)
        }
    }
}

@Composable
private fun NotificationToggleRow(item: NotificationToggleItem) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = item.title,
                color = TextPrimary,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp
                )
            )
            Text(
                text = item.description,
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp)
            )
        }

        Switch(
            checked = item.checked,
            onCheckedChange = item.onCheckedChange,
            modifier = Modifier.scale(0.88f),
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = PrimaryGreen,
                checkedBorderColor = PrimaryGreen,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = SurfaceDivider,
                uncheckedBorderColor = SurfaceDivider
            )
        )
    }
}

private data class NotificationToggleItem(
    val title: String,
    val description: String,
    val checked: Boolean,
    val onCheckedChange: (Boolean) -> Unit
)
