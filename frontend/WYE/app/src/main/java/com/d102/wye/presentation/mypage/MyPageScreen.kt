package com.d102.wye.presentation.mypage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import com.d102.wye.presentation.mypage.component.myPageAccountSection
import com.d102.wye.presentation.mypage.component.myPageEtfSection
import com.d102.wye.presentation.mypage.component.MyPageProfileHeader
import com.d102.wye.presentation.mypage.component.myPageSettingsSection
import com.d102.wye.presentation.mypage.component.myPageSupportSection
import com.d102.wye.presentation.model.UiState

@Composable
fun MyPageScreen(
    onLikedEtfClick: (ticker: String) -> Unit,
    onLogoutClick: () -> Unit,
    onHoldingEtfMoreClick: () -> Unit = {},
    onLikedEtfListClick: () -> Unit = {},
    onPasswordChangeClick: () -> Unit = {},
    onNotificationSettingClick: () -> Unit = {},
    onThemeModeClick: () -> Unit = {},
    onFaqClick: () -> Unit = {},
    onTermsClick: () -> Unit = {},
    viewModel: MyPageViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        if (uiState is UiState.Error) {
            snackbarHostState.showSnackbar(message = (uiState as UiState.Error).message)
        }
    }

    MyPageScreenContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onLikedEtfClick = onLikedEtfClick,
        onLogoutClick = onLogoutClick,
        onHoldingEtfMoreClick = onHoldingEtfMoreClick,
        onLikedEtfListClick = onLikedEtfListClick,
        onPasswordChangeClick = onPasswordChangeClick,
        onNotificationSettingClick = onNotificationSettingClick,
        onThemeModeClick = onThemeModeClick,
        onFaqClick = onFaqClick,
        onTermsClick = onTermsClick
    )
}

@Composable
private fun MyPageScreenContent(
    uiState: UiState<MyPageData>,
    snackbarHostState: SnackbarHostState,
    onLikedEtfClick: (ticker: String) -> Unit,
    onLogoutClick: () -> Unit,
    onHoldingEtfMoreClick: () -> Unit,
    onLikedEtfListClick: () -> Unit,
    onPasswordChangeClick: () -> Unit,
    onNotificationSettingClick: () -> Unit,
    onThemeModeClick: () -> Unit,
    onFaqClick: () -> Unit,
    onTermsClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (uiState) {
            is UiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

            is UiState.Success -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    WyeTopBar(title = "마이페이지")

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                MyPageProfileHeader(nickname = uiState.data.nickname)
                            }
                        }

                        myPageEtfSection(
                            data = uiState.data,
                            onHoldingEtfMoreClick = onHoldingEtfMoreClick,
                            onHoldingEtfClick = onLikedEtfClick,
                            onLikedEtfListClick = onLikedEtfListClick
                        )

                        myPageAccountSection(
                            onPasswordChangeClick = onPasswordChangeClick,
                            onLogoutClick = onLogoutClick
                        )

                        myPageSettingsSection(
                            onNotificationSettingClick = onNotificationSettingClick,
                            onThemeModeClick = onThemeModeClick
                        )

                        myPageSupportSection(
                            onFaqClick = onFaqClick,
                            onTermsClick = onTermsClick
                        )
                    }
                }
            }

            is UiState.Error -> Unit
            UiState.Idle -> Unit
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
